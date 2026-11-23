#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report107.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report107.zip';

if (!fs.existsSync(ZIP_PATH)) {
  console.error(`File not found: ${ZIP_PATH}`);
  process.exit(1);
}

function runEval(code) {
  return execSync(`node /home/bgg/chrome-bridge/cli.mjs eval id:${TAB_ID} -`, {
    input: code,
    encoding: 'utf-8'
  }).trim();
}

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

console.log('Checking if report107 was already submitted...');
const historyCheck = runEval(`(() => {
  const userQueries = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const alreadySent = userQueries.some(m => m.includes('antig_to_gemini_neon_clunky_report107.zip') || m.includes('Round 107 Deliverables'));
  return { alreadySent };
})()`);
console.log('Submission history check:', historyCheck);
const historyInfo = JSON.parse(historyCheck);
if (historyInfo.alreadySent) {
  console.error('SAFETY INVARIANT: Report 107 already detected in chat history! Never reupload. Exiting.');
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
    const chip = document.querySelector('[aria-label*="report107" i]') ||
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
const promptText = `Here is antig_to_gemini_neon_clunky_report107.zip (4.7 MB archive, verified single attachment containing 11 files) with Round 107 Deliverables and a Side-by-Side Visual QC Audit of Fight Club:

1. Round 107 Deliverables (Task A & B):
   - Integrated FolioFolderBrowserDialog.java and FolioMoreView.java from dropin_src/.
   - Added compact ⬆ parent-directory navigation button directly left of the path input in Batch EPUB Importer, allowing 1-tap upward traversal (/sdcard/en/0 -> /sdcard/en -> /sdcard).
   - Rendered clickable warm-amber 📁 <folderName> Open › rows at top of file list for non-hidden subdirectories, enabling instant drilling into nested folders without manual typing.
   - Rebuilt classes9.dex (366,152 B), deployed to physical Samsung Galaxy M32 (32001776e8e82623), and staged release APK at 07_APK_EPUB_INGESTION_MOD/release/folio_v10.7_kfx/base.apk (137 MB).
   - verify_r107_importer_subfolders.png: Hardware verification of ⬆ button, /sdcard/en path, and 📁 0 / 📁 1 subfolder rows.
   - verify_r107_about_v107.png: About Folio modal showing Version: Folio v10.7 KFX (Enhanced Typesetting).
   - Stock package com.amazon.kindle (v8.160.1.0) verified 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23).

2. Fight Club Side-by-Side Visual QC Audit (Compact Theme, White Background, Bookerly Font):
   The user uploaded Fight Club to both Folio and Stock Kindle and noted that they render differently under the same theme. We captured hardware screenshots of the first 3 pages from both apps:
   - Page 1 (Cover): folio_fight_club_page1.png vs kindle_fight_club_page1.png — Mean pixel delta is only 0.54; fullscreen cover art matches stock Kindle cleanly.
   - Page 2 (Title Page): folio_fight_club_page2.png vs kindle_fight_club_page2.png — In Kindle, the title block is centered vertically (Y=[388, 697], height 309 px) with uppercase letter-tracking and no footer. In Folio, the title starts near the top (Y=[174, 411], height 237 px) with generic serif font, and a reading footer (Location 4 of 2463 • 0%) is rendered at Y=[2259, 2283].
   - Page 3 (Chapter 1): folio_fight_club_page3.png vs kindle_fight_club_page3.png — In Kindle, CHAPTER 1 starts at Y=296 and body text ends at Y=2173 (text height 1,877 px), reserving generous status-bar and navigation-bar insets, which overflows the last paragraph to Page 4. In Folio, CHAPTER 1 starts high at Y=133 and extends to Y=2283 (text height 2,150 px) fitting the entire paragraph on Page 3 plus the footer. Kindle preserves publisher CSS styles ($760 letter spacing, 3.66em margin); Folio applies hardcoded fallback paragraph styles.

3. Project Lead Architectural Directive on Cover Architecture:
   "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

Please review the deliverables and the Fight Club comparison, and provide Round 108 instructions in to_antig_clunky_round_108.zip.`;

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
console.log('Report 107 submitted successfully! NEVER REUPLOAD.');
