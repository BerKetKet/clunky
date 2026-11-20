#!/usr/bin/env node
/**
 * Autonomous Relay Stall Detector & Redundancy Monitor
 * Monitors Chrome Gemini tab (id: 837017888) and ADB connection.
 * Detects stalls, downloads new incoming round zips, and triggers prompt continuity.
 */
import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';

const TAB_ID = 837017888;
const WORKDIR = '/home/bgg/projects/clunky';
const DOWNLOAD_DIR = '/home/bgg/Downloads';
const CLI = '/home/bgg/chrome-bridge/cli.mjs';

function runCli(cmd, input) {
  try {
    const opts = { encoding: 'utf-8', timeout: 30000 };
    if (input) opts.input = input;
    return execSync(`node ${CLI} ${cmd}`, opts).trim();
  } catch (err) {
    return `ERROR: ${err.message}`;
  }
}

export function checkStatus() {
  // 1. Check ADB connection
  let adbOk = false;
  try {
    const devOut = execSync('adb devices', { encoding: 'utf-8' });
    adbOk = devOut.includes('32001776e8e82623\tdevice');
  } catch (e) {
    adbOk = false;
  }

  // 2. Check tab state
  const evalScript = `(() => {
    const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
    const downloadBtns = Array.from(document.querySelectorAll('button[aria-label*="Download to_antig" i]')).map(b => ({
      label: b.getAttribute('aria-label'),
      text: b.innerText
    }));
    const modeBtn = document.querySelector('button[aria-label*="mode picker" i]');
    const mode = modeBtn ? modeBtn.getAttribute('aria-label') : 'unknown';
    
    // Check if there is a pending error or message
    const msgEls = document.querySelectorAll('message-content');
    const lastMsg = msgEls.length ? msgEls[msgEls.length - 1].innerText.substring(0, 300) : '';

    return {
      isGenerating,
      mode,
      downloadCount: downloadBtns.length,
      downloads: downloadBtns.map(d => d.label),
      lastMsg
    };
  })()`;

  const raw = runCli(`eval id:${TAB_ID} -`, evalScript);
  let pageState = {};
  try {
    pageState = JSON.parse(raw);
  } catch (e) {
    pageState = { error: raw };
  }

  return {
    timestamp: new Date().toISOString(),
    adbOk,
    pageState
  };
}

if (process.argv[1] && process.argv[1].endsWith('stall_detector.mjs')) {
  console.log(JSON.stringify(checkStatus(), null, 2));
}
