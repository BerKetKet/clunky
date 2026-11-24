#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const CLI = '/home/bgg/chrome-bridge/cli.mjs';
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report117.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report117.zip';

function getTabId() {
  try {
    const raw = execSync(`node ${CLI} tabs`, { encoding: 'utf-8' });
    const tabs = JSON.parse(raw);
    const geminiTab = tabs.find(t => t.title && t.title.includes('Gemini'));
    if (geminiTab) return geminiTab.id;
  } catch (e) {}
  return 837018552;
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

console.log(`Targeting Gemini tab ${TAB_ID}...`);
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

console.log('Checking if report117 was already submitted...');
const historyCheck = runEval(`(() => {
  const userQueries = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const alreadySent = userQueries.some(m => m.includes('antig_to_gemini_neon_clunky_report117.zip') || m.includes('Round 117 Deliverables'));
  return { alreadySent };
})()`);
console.log('Submission history check:', historyCheck);
const historyInfo = JSON.parse(historyCheck);
if (historyInfo.alreadySent) {
  console.error('SAFETY INVARIANT: Report 117 already detected in chat history! Never reupload. Exiting.');
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

console.log('4. Checking for attachment preview in composer...');
let chipDetected = false;
for (let attempt = 0; attempt < 12; attempt++) {
  const checkChip = runEval(`(() => {
    const chip = document.querySelector('[aria-label*="report117" i]') ||
                 document.querySelector('[aria-label*="close antig_to_g" i]') || 
                 document.querySelector('.with-file-preview') ||
                 document.querySelector('uploader-file-preview') ||
                 document.querySelector('.attachment-preview-wrapper');
    const sendBtn = document.querySelector('button[aria-label*="Send message" i]');
    return {
      hasChip: !!chip,
      chipTag: chip ? chip.tagName : null,
      chipClass: chip ? chip.className : null,
      sendDisabled: sendBtn ? (sendBtn.disabled || sendBtn.getAttribute('aria-disabled') === 'true') : true
    };
  })()`);
  console.log(`[Preview check ${attempt + 1}]`, checkChip);
  const info = JSON.parse(checkChip);
  if (info.hasChip) {
    chipDetected = true;
    break;
  }
  execSync('sleep 1');
}

console.log('5. Entering prompt text...');
const promptText = `Here is antig_to_gemini_neon_clunky_report117.zip with Round 117 Deliverables (Folio v11.7 KFX — Reading Goal Pace Telemetry & Share Challenge):

1. Task A: Source Integration & Release Staging:
   - Integrated FolioMoreView.java from dropin_src/ into 07_APK_EPUB_INGESTION_MOD/mod_src/real/.
   - 3-Tile Reading Pace Telemetry Strip (showReadingGoalDialog): Added live pace dashboard beneath annual progress bar displaying DAYS LEFT (sky-blue), TO GO (amber/emerald), and NEEDED PACE (violet, e.g. 1 / 9d or 🏆 Met!), computed dynamically from device calendar.
   - ↗ Share Reading Challenge Pill: Added sky-blue ↗ Share button beside 🎯 2026 Reading Goal dispatching Android Intent.ACTION_SEND with progress %, pace summary, and completed books list.
   - Unified Trophy Shelf Covers: Upgraded Completed Books Trophy Shelf to use makeMiniCoverView(...) with rounded #0D1017 border + 📖 fallback.
   - Compiler Variable Shadowing Fix: Resolved prLp duplicate declaration in showReadingGoalDialog by renaming preset layout params to presetLp.
   - Version Bump: Updated Control Center badge and About Folio dialog to Folio v11.7 KFX.
   - Preserved MetadataEnricher.java and OpenCatalogView.java.
   - Recompiled classes9.dex (404,120 B), repacked base APK as classes4.dex, aligned via zipalign, and signed with apksigner.
   - Installed on physical Samsung Galaxy M32 (lastUpdateTime=2026-11-24 03:54:57) and staged release APK: 07_APK_EPUB_INGESTION_MOD/release/folio_v11.7_kfx/base.apk (132 MB).

2. Task B: Hardware Verification on Physical Galaxy M32:
   - verify_r117_reading_goal_pace.png (235,328 bytes): Captured 🎯 2026 Reading Goal modal showing ↗ Share header pill, 3-tile Reading Pace Telemetry strip (DAYS LEFT, TO GO, NEEDED PACE), and 🏆 COMPLETED BOOKS (1) Trophy Shelf with cover thumbnail.
   - verify_r117_about_v117.png (353,990 bytes): About Folio dialog confirming Version: Folio v11.7 KFX (Enhanced Typesetting), Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a), and live library stats.
   - Stock package com.amazon.kindle (v8.160.1.0) verified 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23). Zero crashes logged.

3. Project Lead Architectural Directive on Cover Architecture:
   "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

Please review the deliverables and provide Round 118 instructions in to_antig_clunky_round_118.zip.`;

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
const genCheck = runEval(`(() => {
  const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
  return { isGenerating };
})()`);
console.log('Generation status:', genCheck);
console.log('Report 117 submitted successfully! NEVER REUPLOAD.');
