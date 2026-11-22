#!/usr/bin/env node
import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';

const TAB_ID = 837017938;
const WORKDIR = '/home/bgg/projects/clunky';
const DOWNLOAD_DIR = '/home/bgg/Downloads';
const CLI = '/home/bgg/chrome-bridge/cli.mjs';

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function runCli(cmd, input) {
  try {
    const opts = { encoding: 'utf-8', timeout: 30000 };
    if (input) opts.input = input;
    return execSync(`node ${CLI} ${cmd}`, opts).trim();
  } catch (err) {
    return `ERROR: ${err.message}`;
  }
}

async function waitForRound(roundNum, maxWaitSec = 2000) {
  const roundTag = String(roundNum).padStart(3, '0');
  const targetLabel = `to_antig_clunky_round_${roundTag}`;
  console.log(`[Monitor] Watching for Round ${roundTag} (target: ${targetLabel}, max wait: ${maxWaitSec}s, polling: every 60s)...`);

  const startTime = Date.now();
  let checkCount = 0;

  // First check if target zip is ALREADY in ~/Downloads from user or previous click
  const existingMatches = fs.readdirSync(DOWNLOAD_DIR)
    .filter(f => f.startsWith(targetLabel) && f.endsWith('.zip'))
    .sort((a, b) => fs.statSync(path.join(DOWNLOAD_DIR, b)).mtimeMs - fs.statSync(path.join(DOWNLOAD_DIR, a)).mtimeMs);
  if (existingMatches.length > 0) {
    const mtime = fs.statSync(path.join(DOWNLOAD_DIR, existingMatches[0])).mtimeMs;
    // If downloaded within the last 30 minutes, accept it
    if ((Date.now() - mtime) < 1800000) {
      const targetZip = path.join(DOWNLOAD_DIR, existingMatches[0]);
      console.log(`[Monitor] Found existing recently downloaded target: ${targetZip}!`);
      const destZip = path.join(WORKDIR, `${targetLabel}.zip`);
      fs.copyFileSync(targetZip, destZip);
      execSync(`unzip -o ${destZip} -d ${WORKDIR}`, { stdio: 'inherit' });
      console.log(`[Monitor] Staged and extracted ${destZip}!`);
      return { success: true, round: roundNum, zip: destZip };
    }
  }

  while ((Date.now() - startTime) < (maxWaitSec * 1000)) {
    checkCount++;
    const elapsedSec = Math.round((Date.now() - startTime) / 1000);

    const evalScript = `(() => {
      const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
      const scroller = document.querySelector('infinite-scroller.chat-history');
      if (scroller) scroller.scrollTop = scroller.scrollHeight;

      // 1. Check direct download button
      const dlBtn = document.querySelector('button[aria-label*="Download ${targetLabel}" i]') ||
                    document.querySelector('button[aria-label*="${targetLabel}" i]');

      // 2. Check candidate containers
      const candidates = Array.from(document.querySelectorAll('button, a, div.chip-lr, generated-file, [aria-label*="round_${roundTag}" i]')).map(b => ({
        aria: b.getAttribute('aria-label') || '',
        text: b.innerText || ''
      }));

      const hasTarget = !!dlBtn || candidates.some(c => 
        (c.aria && c.aria.toLowerCase().includes("${targetLabel}".toLowerCase())) ||
        (c.text && c.text.toLowerCase().includes("${targetLabel}".toLowerCase())) ||
        (c.aria && c.aria.toLowerCase().includes("round_${roundTag}".toLowerCase())) ||
        (c.text && c.text.toLowerCase().includes("round_${roundTag}".toLowerCase()))
      );

      const msgEls = document.querySelectorAll('message-content, model-response, .model-response-text');
      const lastMsg = msgEls.length ? msgEls[msgEls.length - 1].innerText : '';
      return { isGenerating, hasTarget, dlBtnFound: !!dlBtn, lastMsg: lastMsg.slice(-300) };
    })()`;

    const raw = runCli(`eval id:${TAB_ID} -`, evalScript);
    let state = {};
    try {
      state = JSON.parse(raw);
    } catch (e) {
      console.log(`[Monitor @ ${elapsedSec}s] Warning: eval parse failed (${raw}). Waiting 60s...`);
      await sleep(60000);
      continue;
    }

    if (state.hasTarget) {
      console.log(`\n[Monitor @ ${elapsedSec}s] Found target ${targetLabel}! Triggering download...`);
      const clickScript = `(() => {
        // Try exact download button first
        const dlBtn = document.querySelector('button[aria-label*="Download ${targetLabel}" i]') ||
                      document.querySelector('button[aria-label*="${targetLabel}" i]');
        if (dlBtn) {
          dlBtn.click();
          return 'clicked_dl_button';
        }

        // Fallback to container or inner button
        let el = Array.from(document.querySelectorAll('button, a, div.chip-lr, generated-file, [aria-label*="round_${roundTag}" i]')).find(b => {
          const aria = b.getAttribute('aria-label') || '';
          const text = b.innerText || '';
          return aria.toLowerCase().includes("${targetLabel}".toLowerCase()) ||
                 text.toLowerCase().includes("${targetLabel}".toLowerCase()) ||
                 aria.toLowerCase().includes("round_${roundTag}".toLowerCase()) ||
                 text.toLowerCase().includes("round_${roundTag}".toLowerCase());
        });
        if (el) {
          const innerBtn = el.tagName === 'BUTTON' ? el : el.querySelector('button');
          const target = innerBtn || el;
          target.click();
          return 'clicked_fallback';
        }
        return 'not_found';
      })()`;
      const clickRes = runCli(`eval id:${TAB_ID} -`, clickScript);
      console.log(`[Monitor] Click result: ${clickRes}`);

      // Wait for file in ~/Downloads
      console.log(`[Monitor] Waiting for file matching ${targetLabel}*.zip in ${DOWNLOAD_DIR}...`);
      for (let w = 0; w < 30; w++) {
        await sleep(1000);
        const matches = fs.readdirSync(DOWNLOAD_DIR)
          .filter(f => f.startsWith(targetLabel) && f.endsWith('.zip'))
          .sort((a, b) => fs.statSync(path.join(DOWNLOAD_DIR, b)).mtimeMs - fs.statSync(path.join(DOWNLOAD_DIR, a)).mtimeMs);
        if (matches.length > 0) {
          const targetZip = path.join(DOWNLOAD_DIR, matches[0]);
          console.log(`[Monitor] Downloaded ${targetZip} (${fs.statSync(targetZip).size} bytes)!`);
          const destZip = path.join(WORKDIR, `${targetLabel}.zip`);
          fs.copyFileSync(targetZip, destZip);
          execSync(`unzip -o ${destZip} -d ${WORKDIR}`, { stdio: 'inherit' });
          console.log(`[Monitor] Successfully staged and extracted ${destZip}!`);
          return { success: true, round: roundNum, zip: destZip };
        }
      }
      console.log(`[Monitor] Download timed out after 30s. Will re-poll.`);
    }

    if (state.isGenerating) {
      console.log(`[Monitor @ ${elapsedSec}s] Gemini is still generating... checking again in 60s.`);
    } else {
      console.log(`[Monitor @ ${elapsedSec}s] Gemini idle. Target not yet available (last preview: ${state.lastMsg.replace(/\\n/g, ' ').substring(0, 80)}...). Waiting 60s.`);
    }

    // Wait 60 seconds between checks (user rule: monitor less frequently, every minute, no spam)
    await sleep(60000);
  }

  console.log(`[Monitor] Max wait time (${maxWaitSec}s) reached.`);
  return { success: false, reason: 'timeout' };
}

async function main() {
  const round = parseInt(process.argv[2] || '32', 10);
  const maxWait = parseInt(process.argv[3] || '2000', 10);
  const res = await waitForRound(round, maxWait);
  console.log('[Monitor Output]:', JSON.stringify(res));
  if (!res.success) process.exit(1);
}

main().catch(err => {
  console.error('[Monitor Error]:', err);
  process.exit(1);
});
