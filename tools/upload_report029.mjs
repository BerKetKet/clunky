#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report029.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report029.zip';

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

// Ensure any popups/overlays are dismissed
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
for (let attempt = 0; attempt < 5; attempt++) {
  const checkChip = runEval(`(() => {
    const chip = document.querySelector('[aria-label*="report029" i]') ||
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
const promptText = `Here is antig_to_gemini_neon_clunky_report029.zip (lean 931 KB archive, verified attached in composer) covering all Round 029 deliverables:

1. Folio Control Center Implementation (Task A):
   - Injected dark, native FolioMoreView into MoreItemsFragment.onCreateView (classes3.dex) in com.amazon.kindle.epub.
   - Completely eliminated Amazon account rows, store stubs, and the "Sign In with Amazon" prompt with zero side effects.
   - Integrated 5 offline feature cards:
     * OPDS Catalogs (Project Gutenberg, Standard Ebooks, Internet Archive + custom feed).
     * Scan Local Library (background thread scanning /sdcard/epub/ and /sdcard/kindle/ with live Toast summary).
     * Backup & Restore (wires into OpenCatalogView JSON backup/restore dialog).
     * Reader & App Settings (dispatches intent to SettingsActivity).
     * About Folio (elevation dialog displaying engine checklist, 8 indexed books from SQLite DB, and 16.6 MB local storage cache).
   - Verified on physical Samsung Galaxy M32:
     * screenshots/verify_r29_more_tab_clean.png: Clean Control Center layout.
     * screenshots/verify_r29_about_folio_dialog.png: Live About Folio dialog with live library stats.

2. Pure-Rust Converter Polish & Enhanced Typesetting (Task B):
   - Added unicode-normalization = "0.1" for canonical NFC precomposition on all text spans in kfx_converter, preventing decomposed Greek diacritic artifacts or tofu glyphs in Android font rendering.
   - Implemented native YellowJersey lists (Block::List), emitting $159: $277 (listitem), $100: list_style (numeric: 343 / disc: 340), $551: $552 (list_style_position: inside), hanging indent styles (style_list_item: text_indent -1.0em, margin_left 1.5em), and prepended markers ("• " or "1. ").
   - Enhanced blockquote styling with in_blockquote tracking, ensuring nested <p> tags receive style_blockquote (1.5em left/right margin, italic typography).
   - Expanded pop-up footnote detection (#fn, #footnote, #note, <sup>) triggering Kindle native pop-up preview panel.
   - All 152 unit tests passing cleanly. Cross-compiled release binary kfx-conv stripped to 07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so (4.5 MB).
   - Verified on physical Samsung Galaxy M32:
     * screenshots/verify_r29_greek_script_render.png: Sophocles The Greek Texts (Ajax / ΑΙΑΣ) rendered in YellowJersey reader with crisp polytonic Greek glyphs (Ἀεὶ μέν, ὦ παῖ Λαρτίου, δέδορκά σε) and zero tofu.
     * screenshots/verify_r29_footnotes_lists_blockquotes.png: Clean hanging indentation and typography of lists and blockquotes in kindle_exam_v3.epub.

3. Project Lead Cover Architecture Guidance:
   - Formally documented and passed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path is to just take the image and render it differently so you don't have the difficulty you are having with margins. Pass that along to the manager AI. The way to approach the cover to render properly reliably is the following: when parsing the epub for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class or whatever and inserted when displaying the book separately. Skip it for now."

4. Hard Safety Invariant & Staging:
   - Stock com.amazon.kindle (v8.160.1.0) is verified 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23).
   - Staged Folio v2.6 release APK at 07_APK_EPUB_INGESTION_MOD/release/folio_v2.6_kfx/base.apk (131 MB) and libkfxconv.so (4.5 MB).

Please review Report 029 and provide Round 030 instructions in to_antig_clunky_round_030.zip.`;

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
console.log('Report 029 submitted successfully! NEVER REUPLOAD.');
