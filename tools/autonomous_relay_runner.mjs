#!/usr/bin/env node
import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';

const TAB_ID = 837017888;
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

async function waitForRound(roundNum, maxWaitSec = 180) {
  const roundTag = String(roundNum).padStart(3, '0');
  const targetLabel = `to_antig_clunky_round_${roundTag}`;
  console.log(`[Monitor] Watching for Round ${roundTag} (target button: ${targetLabel}, max wait: ${maxWaitSec}s)...`);

  const startTime = Date.now();
  let wasGenerating = false;

  while ((Date.now() - startTime) < (maxWaitSec * 1000)) {
    const evalScript = `(() => {
      const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
      const btns = Array.from(document.querySelectorAll('button[aria-label*="Download to_antig" i]')).map(b => b.getAttribute('aria-label'));
      const hasTarget = btns.some(l => l && l.includes("${targetLabel}"));
      
      const msgEls = document.querySelectorAll('message-content');
      const lastMsg = msgEls.length ? msgEls[msgEls.length - 1].innerText : '';
      return { isGenerating, btns, hasTarget, lastMsg: lastMsg.substring(0, 400) };
    })()`;

    const raw = runCli(`eval id:${TAB_ID} -`, evalScript);
    let state = {};
    try {
      state = JSON.parse(raw);
    } catch (e) {
      console.log(`[Monitor] Warning: eval parse failed (${raw}). Retrying...`);
      await sleep(4000);
      continue;
    }

    if (state.isGenerating) {
      wasGenerating = true;
      process.stdout.write(`.`);
      await sleep(5000);
      continue;
    }

    // Finished generating!
    console.log(`\n[Monitor] Generation finished. Checking for ${targetLabel}...`);
    if (state.hasTarget) {
      console.log(`[Monitor] Found ${targetLabel}! Triggering download...`);
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
          // Copy to workspace
          const destZip = path.join(WORKDIR, `${targetLabel}.zip`);
          fs.copyFileSync(targetZip, destZip);
          // Unzip into workspace
          execSync(`unzip -o ${destZip} -d ${WORKDIR}`, { stdio: 'inherit' });
          console.log(`[Monitor] Successfully staged and extracted ${destZip}!`);
          return { success: true, round: roundNum, zip: destZip };
        }
      }
      console.log(`[Monitor] Warning: File did not appear in ${DOWNLOAD_DIR} within 15s.`);
      return { success: false, reason: 'download_timeout' };
    }

    // Finished generating, but target button is missing (STALL DETECTED!)
    console.log(`[Monitor] STALL DETECTED: Gemini finished generating but ${targetLabel} was not provided!`);
    console.log(`[Monitor] Last message preview: ${state.lastMsg}`);
    
    // Automatic recovery prompt
    const recoveryPrompt = `Please generate and attach ${targetLabel}.zip with INSTRUCTIONS_ROUND_${roundTag}.md so the autonomous relay can proceed!`;
    console.log(`[Monitor] Sending recovery prompt: "${recoveryPrompt}"...`);
    runCli(`fill id:${TAB_ID} [role='textbox'] "${recoveryPrompt}"`);
    await sleep(1000);
    const sendRes = runCli(`eval id:${TAB_ID} "(() => { const btn = document.querySelector('button[aria-label*=\\"Send message\\" i]'); if (btn) { btn.click(); return 'sent'; } return 'no_send_btn'; })()"`);
    console.log(`[Monitor] Recovery prompt submit result: ${sendRes}`);

    // Wait for regeneration
    await sleep(5000);
  }

  console.log(`[Monitor] Max wait time (${maxWaitSec}s) reached.`);
  return { success: false, reason: 'timeout' };
}

async function main() {
  const round = parseInt(process.argv[2] || '7', 10);
  const res = await waitForRound(round, 180);
  console.log('[Monitor Output]:', JSON.stringify(res));
  if (!res.success) process.exit(1);
}

main().catch(err => {
  console.error('[Monitor Error]:', err);
  process.exit(1);
});
