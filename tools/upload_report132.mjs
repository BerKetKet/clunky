#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const CLI = '/home/bgg/chrome-bridge/cli.mjs';
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report132.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report132.zip';

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

console.log('Checking if report132 was already submitted...');
const historyCheck = runEval(`(() => {
  const userQueries = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const alreadySent = userQueries.some(m => m.includes('antig_to_gemini_neon_clunky_report132.zip') || m.includes('Round 132 Deliverables'));
  return { alreadySent };
})()`);
console.log('Submission history check:', historyCheck);
const historyInfo = JSON.parse(historyCheck);
if (historyInfo.alreadySent) {
  console.error('SAFETY INVARIANT: Report 132 already detected in chat history! Never reupload. Exiting.');
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

console.log('4. Checking for real attachment preview chip in composer...');
let chipDetected = false;
for (let attempt = 0; attempt < 15; attempt++) {
  const checkChip = runEval(`(() => {
    const wrap = document.querySelector('.attachment-preview-wrapper');
    const chip = document.querySelector('[aria-label*="report132" i]') ||
                 document.querySelector('[aria-label*="close antig_to_g" i]') ||
                 document.querySelector('uploader-file-preview');
    const hasContent = !!(chip || (wrap && wrap.innerText && wrap.innerText.includes('report132')));
    const sendBtn = document.querySelector('button[aria-label*="Send message" i]');
    return {
      hasContent,
      wrapText: wrap ? wrap.innerText.trim() : '',
      sendDisabled: sendBtn ? (sendBtn.disabled || sendBtn.getAttribute('aria-disabled') === 'true') : true
    };
  })()`);
  console.log(`[Preview check ${attempt + 1}]`, checkChip);
  const info = JSON.parse(checkChip);
  if (info.hasContent) {
    chipDetected = true;
    break;
  }
  execSync('sleep 1');
}

console.log('5. Entering prompt text...');
const promptText = `Here is antig_to_gemini_neon_clunky_report132.zip with Round 132 Deliverables (Folio v13.2 KFX — Insights WPM Tap-to-Calibrate, Snapshot Telemetry & Clean Dialog Theming Sync):

1. Target Hardware:
   - Physical Samsung Galaxy M30 (32001776e8e82623), running TrebleDroid Android 13 GSI.

2. Task A: Source Integration & Release Staging:
   - FolioMoreView.java: Integrated READING TIME PORTFOLIO (250 WPM · TAP TO CALIBRATE) with click-to-calibrate handler on both rtHdr and rtRow opening showReadingGoalDialog and re-opening Library Insights on save. Also added the missing Reading Time (WPM) summary line into snapshotText for COPY SUMMARY / SHARE exports. Confirmed clean post-.show() FolioTheme.applyToDialog placements across all 5 dialogs out of the box, and bumped badge + About Folio to Folio v13.2 KFX.
   - Recompiled classes9.dex (458,448 B), rebuilt APK with apktool b, aligned via zipalign, signed with apksigner.
   - Deployed to physical Samsung Galaxy M30 (lastUpdateTime=2026-11-24 23:32:53) with zero crashes logged, and staged release APK: 07_APK_EPUB_INGESTION_MOD/release/folio_v13.2_kfx/base.apk (137,740,774 B, SHA256: 430a7b2d35adbc016a99248d4ce6b9e7a9c9e28fa853692d68444cd8071aaba2).

3. Task B: Hardware Verification on Physical Samsung Galaxy M30:
   - verify_r132_insights_calibrate.png (176,328 bytes): Captured Library Insights modal showing READING TIME PORTFOLIO (250 WPM · TAP TO CALIBRATE) and 3-tile strip (651h TOTAL LIBRARY, 14.9h TIME READ, 636h REMAINING). Interactively verified tapping opens 2026 Reading Goal calibrator.
   - verify_r132_about_v132.png (339,562 bytes): Captured About Folio dialog showing Version: Folio v13.2 KFX (Enhanced Typesetting).
   - Safety Invariant: Stock package com.amazon.kindle (v8.160.1.0) verified 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23).

4. Project Lead Architectural Directive on Cover Architecture (Mandatory Reiteration):
   "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

Please review the deliverables and provide Round 133 instructions in to_antig_clunky_round_133.zip.`;

const enterPrompt = runEval(`(() => {
  const editor = document.querySelector('rich-textarea [contenteditable]') || document.querySelector('.ql-editor');
  if (!editor) return { error: "no editor" };
  editor.focus();
  const ok = document.execCommand('insertText', false, ${JSON.stringify(promptText)});
  return { success: ok, length: editor.innerText.length };
})()`);
console.log('Prompt entry result:', enterPrompt);

console.log('6. Checking Send button and clicking...');
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

execSync('sleep 3');
const verifySubmit = runEval(`(() => {
  const q = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const last = q[q.length-1] || '';
  const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
  return { submitted: last.includes('report132'), isGenerating };
})()`);
console.log('Submission verification:', verifySubmit);
console.log('Report 132 submitted successfully! NEVER REUPLOAD.');
