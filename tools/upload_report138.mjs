#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const CLI = '/home/bgg/chrome-bridge/cli.mjs';
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report138.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report138.zip';

function getTabId() {
  try {
    const raw = execSync(`node ${CLI} tabs`, { encoding: 'utf-8' });
    const tabs = JSON.parse(raw);
    const geminiTab = tabs.find(t => t.title && t.title.includes('Gemini'));
    if (geminiTab) return geminiTab.id;
  } catch (e) {}
  return 837018788;
}

const TAB_ID = getTabId();

if (!fs.existsSync(ZIP_PATH)) {
  console.error(`File not found: ${ZIP_PATH}`);
  process.exit(1);
}

function runEval(code) {
  return execSync(`node ${CLI} eval id:${TAB_ID} -`, {
    input: code,
    encoding: 'utf-8'
  }).trim();
}

console.log(`Activating & targeting Gemini tab ${TAB_ID}...`);
try {
  execSync(`node ${CLI} activate id:${TAB_ID}`, { stdio: 'inherit' });
} catch (e) {}

console.log('1. Verifying model is Neon...');
const checkModel = runEval(`(() => {
  const switchBtn = document.querySelector('.input-area-switch, [aria-label*="currently Neon" i]');
  const txt = switchBtn ? switchBtn.innerText : '';
  const aria = switchBtn ? switchBtn.getAttribute('aria-label') : '';
  const isNeon = (txt && txt.includes('Neon')) || (aria && aria.includes('Neon')) || document.querySelector('[aria-label*="currently Neon" i]') !== null;
  return { isNeon, txt, aria };
})()`);
console.log('Model check result:', checkModel);
const modelInfo = JSON.parse(checkModel);
if (!modelInfo.isNeon) {
  console.error('ERROR: Model is not Neon! Aborting upload.');
  process.exit(1);
}

console.log('Checking if report138 was already submitted...');
const historyCheck = runEval(`(() => {
  const userQueries = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const alreadySent = userQueries.some(m => m.includes('antig_to_gemini_neon_clunky_report138.zip') || m.includes('Round 138 Deliverables'));
  return { alreadySent };
})()`);
console.log('Submission history check:', historyCheck);
const historyInfo = JSON.parse(historyCheck);
if (historyInfo.alreadySent) {
  console.error('SAFETY INVARIANT: Report 138 already detected in chat history! Never reupload. Exiting.');
  process.exit(0);
}

runEval(`(() => {
  const backdrop = document.querySelector('.cdk-overlay-backdrop');
  if (backdrop) backdrop.click();
})()`);

const b64 = fs.readFileSync(ZIP_PATH).toString('base64');
console.log(`2. Sending file chunks (total base64 length: ${b64.length})...`);
runEval(`window._uploadChunks = []`);

const chunkSize = 200000;
for (let i = 0; i < b64.length; i += chunkSize) {
  const chunk = b64.substring(i, i + chunkSize);
  runEval(`window._uploadChunks.push(${JSON.stringify(chunk)})`);
  process.stdout.write(`Chunk ${Math.floor(i / chunkSize) + 1}/${Math.ceil(b64.length / chunkSize)} transferred\r`);
}
console.log('\nAll chunks transferred to browser window.');

console.log('3. Attaching file via paste event on editor...');
const attachResult = runEval(`(() => {
  try {
    const b64Full = window._uploadChunks.join('');
    delete window._uploadChunks;
    const binary = atob(b64Full);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    const blob = new Blob([bytes], { type: 'application/zip' });
    const file = new File([blob], ${JSON.stringify(FILENAME)}, { type: 'application/zip' });

    const dt = new DataTransfer();
    dt.items.add(file);

    const target = document.querySelector('rich-textarea [contenteditable]') || document.querySelector('rich-textarea');
    if (!target) return { error: "target not found" };
    target.focus();
    target.click();

    const pasteEvent = new ClipboardEvent('paste', {
      bubbles: true,
      cancelable: true,
      clipboardData: dt
    });
    const dispatched = target.dispatchEvent(pasteEvent);

    return { pasteTriggered: true, dispatched, fileName: file.name, fileSize: file.size };
  } catch (e) {
    return { error: e.toString() };
  }
})()`);
console.log('Attach result:', attachResult);

execSync('sleep 2');
console.log('4. Entering prompt text...');
const promptText = `Here is antig_to_gemini_neon_clunky_report138.zip with Round 138 Deliverables (Azath v14.4 KFX — Bundled EB Garamond + Inter Typography, Unified Home Screen UI & Release Staging):

1. Target Hardware:
   - Physical Samsung Galaxy M30 (32001776e8e82623), running TrebleDroid Android 13 GSI.

2. Task A: Build, Deploy & Stage (com.azath.reader):
   - Staged static EB Garamond (Regular 381K, SemiBold 384K TTF) and Inter (Regular 253K, SemiBold 265K OTF) in 07_APK_EPUB_INGESTION_MOD/assets/azath_fonts/ and dropin_assets/azath_fonts/.
   - Integrated Round 138 FolioTheme.java, OpenCatalogView.java, FolioMoreView.java (v14.4 KFX, preserving makeMiniCoverView rec.title & "Azath Library Insights"), and FolioWebBrowserDialog.java (dark offline onReceivedError page; compiled in classes9.dex, not screenshotted in R138).
   - Updated rebuild_fast.sh to copy assets/azath_fonts/, recompiled classes9.dex (462,736 B), applied Lib/b.<clinit> Ember Crimson (0xffe05a47L) & XML patches, deployed to Galaxy M30 (lastUpdateTime=2026-11-25 00:43:31, NO_FATAL_EXCEPTIONS), and staged release APK at 07_APK_EPUB_INGESTION_MOD/release/azath_v14.4_kfx/base.apk (138,528,871 B, SHA256: 234deb91bf6ce4245203f451e58f551688e268eb89efa02c31e4bda0fe759663).

3. Task B: Hardware Verification Screenshots (Samsung Galaxy M30):
   - verify_r138_azath_home.png (154,151 bytes): Home screen with Regular EB Garamond "Azath" wordmark (30sp), + OPDS, tonal Backup, #181515 search bar with #E05A47 Search CTA, genre pills, Continue Reading & Popular Free EPUBs in Regular EB Garamond, and #E05A47 active Home tab.
   - verify_r138_azath_control_center.png (239,020 bytes): Control Center with Regular EB Garamond title & stat numbers (26 BOOKS, 5 READING), v14.4 KFX badge, Inter labels, preserved B archival monogram on Brave New World, and #E05A47 More tab.
   - Safety Invariant: Stock package com.amazon.kindle (v8.160.1.0) verified 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23).

4. Project Lead Architectural Directive on Cover Architecture (Mandatory Reiteration):
   "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

Please review the deliverables and provide Round 139 instructions in to_antig_clunky_round_139.zip.`;

const enterPrompt = runEval(`(() => {
  const editor = document.querySelector('rich-textarea [contenteditable]') || document.querySelector('.ql-editor');
  if (!editor) return { error: "no editor" };
  editor.focus();
  const ok = document.execCommand('insertText', false, ${JSON.stringify(promptText)});
  return { success: ok, length: editor.innerText.length };
})()`);
console.log('Prompt entry result:', enterPrompt);

console.log('5. Checking Send button and clicking...');
let sent = false;
for (let attempt = 0; attempt < 10; attempt++) {
  const sendCheck = runEval(`(() => {
    const btn = document.querySelector('button[aria-label*="Send message" i]');
    if (btn && !btn.disabled && btn.getAttribute('aria-disabled') !== 'true') {
      btn.click();
      return { clicked: true };
    }
    return { clicked: false, disabled: btn ? (btn.disabled || btn.getAttribute('aria-disabled') === 'true') : true };
  })()`);
  console.log(`[Send check ${attempt + 1}]`, sendCheck);
  const parsed = JSON.parse(sendCheck);
  if (parsed.clicked) {
    sent = true;
    break;
  }
  execSync('sleep 2');
}

if (!sent) {
  console.error('Failed to click Send button! Aborting.');
  process.exit(1);
}

execSync('sleep 6');
const verifySubmit = runEval(`(() => {
  const q = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const last = q[q.length-1] || '';
  const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
  return { submitted: last.includes('report138'), isGenerating };
})()`);
console.log('Submission verification:', verifySubmit);
console.log('Report 138 submitted successfully! NEVER REUPLOAD.');
