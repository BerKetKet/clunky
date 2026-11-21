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

  while ((Date.now() - startTime) < (maxWaitSec * 1000)) {
    checkCount++;
    const elapsedSec = Math.round((Date.now() - startTime) / 1000);

    const evalScript = `(() => {
      const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
      const btns = Array.from(document.querySelectorAll('button[aria-label*="Download to_antig" i]')).map(b => b.getAttribute('aria-label'));
      const hasTarget = btns.some(l => l && l.includes("${targetLabel}"));
      
      const msgEls = document.querySelectorAll('message-content');
      const lastMsg = msgEls.length ? msgEls[msgEls.length - 1].innerText : '';
      return { isGenerating, btns, hasTarget, lastMsg: lastMsg.substring(0, 300) };
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
        const btn = Array.from(document.querySelectorAll('button[aria-label*="Download to_antig" i]')).find(b => b.getAttribute('aria-label')?.includes("${targetLabel}"));
        if (btn) {
          btn.click();
          return 'clicked';
        }
        return 'not_found';
      })()`;
      const clickRes = runCli(`eval id:${TAB_ID} -`, clickScript);
      console.log(`[Monitor] Click result: ${clickRes}`);

      // Wait for file in ~/Downloads
      const targetZip = path.join(DOWNLOAD_DIR, `${targetLabel}.zip`);
      console.log(`[Monitor] Waiting for file: ${targetZip}...`);
      for (let w = 0; w < 15; w++) {
        await sleep(1000);
        if (fs.existsSync(targetZip)) {
          console.log(`[Monitor] Downloaded ${targetZip} (${fs.statSync(targetZip).size} bytes)!`);
          const destZip = path.join(WORKDIR, `${targetLabel}.zip`);
          fs.copyFileSync(targetZip, destZip);
          execSync(`unzip -o ${destZip} -d ${WORKDIR}`, { stdio: 'inherit' });
          console.log(`[Monitor] Successfully staged and extracted ${destZip}!`);
          return { success: true, round: roundNum, zip: destZip };
        }
      }
      return { success: false, reason: 'download_timeout' };
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
  const round = parseInt(process.argv[2] || '14', 10);
  const maxWait = parseInt(process.argv[3] || '2000', 10);
  const res = await waitForRound(round, maxWait);
  console.log('[Monitor Output]:', JSON.stringify(res));
  if (!res.success) process.exit(1);
}

main().catch(err => {
  console.error('[Monitor Error]:', err);
  process.exit(1);
});
