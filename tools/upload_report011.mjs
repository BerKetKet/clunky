#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017888;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report011.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report011.zip';

if (!fs.existsSync(ZIP_PATH)) {
  console.error(`File not found: ${ZIP_PATH}`);
  process.exit(1);
}

const b64 = fs.readFileSync(ZIP_PATH).toString('base64');
console.log(`Total base64 length: ${b64.length}`);

// Reset in browser
execSync(`node /home/bgg/chrome-bridge/cli.mjs eval id:${TAB_ID} "window._uploadChunks = []"`);

// Send in 200KB chunks
const chunkSize = 200000;
for (let i = 0; i < b64.length; i += chunkSize) {
  const chunk = b64.substring(i, i + chunkSize);
  const script = `window._uploadChunks.push("${chunk}"); "ok"`;
  execSync(`node /home/bgg/chrome-bridge/cli.mjs eval id:${TAB_ID} -`, {
    input: script,
    encoding: 'utf-8'
  });
  process.stdout.write('.');
}
console.log('\nAll chunks sent! Assembling in browser...');

const assembleScript = `(() => {
  const b64 = window._uploadChunks.join("");
  const bin = atob(b64);
  const u8 = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) {
    u8[i] = bin.charCodeAt(i);
  }
  const file = new File([u8], "${FILENAME}", {type: "application/zip"});
  const dt = new DataTransfer();
  dt.items.add(file);
  const pasteEvent = new ClipboardEvent("paste", {
    clipboardData: dt,
    bubbles: true,
    cancelable: true
  });
  const target = document.querySelector("rich-textarea [contenteditable]") || document.querySelector("rich-textarea");
  target.focus();
  const res = target.dispatchEvent(pasteEvent);
  delete window._uploadChunks;
  return res;
})()`;

const res = execSync(`node /home/bgg/chrome-bridge/cli.mjs eval id:${TAB_ID} -`, {
  input: assembleScript,
  encoding: 'utf-8'
});
console.log('Assembly result:', res.trim());
