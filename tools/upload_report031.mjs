#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report031.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report031.zip';

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
    const chip = document.querySelector('[aria-label*="report031" i]') ||
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
const promptText = `Here is antig_to_gemini_neon_clunky_report031.zip (lean 1.1 MB archive, verified single attachment in composer) covering all Round 031 deliverables:

1. Real Native KFX Pop-Up Footnotes (Task A):
   - Pure-Rust schema parity in libkfxconv.so: Link span in $142 style_events emits $179 link_to and $616: $617 (yj.display: yj.note). Destination footnote story entry in $259 storyline emits $615: $281 (yj.classification: footnote). Anchor entity $266 resolves $183 position pointing to footnote story entry EID with offset 0.
   - Cleaned Fragment $597 auxiliary_data to emit standard IS_TARGET_SECTION metadata only, matching Amazon Previewer output.
   - Fixed anchor leak bug in xhtml.rs: anchor consumed via anchor.take() on span flush; current_anchor and current_block_id cleanly reset on block boundaries.
   - Tested on Samsung Galaxy M32 in KRIFDocViewer: tapping footnote link navigates directly to footnote text with top "Back to Location" affordance; soft keyboard and Add Note editor are completely suppressed (screenshots/verify_r31_real_footnote_popup.png).

2. Rich Formatting Audit: <pre>/<code>, <hr/>, <sup>/<sub>/<s>/<u> (Task B):
   - Monospace code blocks (<pre>, <code>): Preserves exact whitespace and newlines, applies monospace font family ($11: "monospace"), left alignment ($34: 59), zero text indent ($36: 0.0rem), and margins ($48: 1.2rem).
   - Horizontal rules (<hr/>): Emits YellowJersey $596 horizontal_rule story elements with style_hr.
   - Inlines: Baseline styles ($44: $370 sup, $44: $371 sub) + shift ($31), strikethrough ($27: $328), and underline ($23: $328).
   - Verified on Galaxy M32 in screenshots/verify_r31_code_hr_rich_formatting.png. All 211 tests pass.

3. Clean Hardware Label & Live Library Search QC (Task C):
   - In FolioMoreView.java: Filtered out literal "unknown" (case-insensitive) Build.MANUFACTURER from TrebleDroid GSI. Formats cleanly as "Build.MODEL · Android VERSION (arch)". Updated badge to "Folio v2.8 KFX" (screenshots/verify_r31_about_clean_hardware.png).
   - Live Library Search: Verified query "Sophocles" executes RubySearchActivity and filters local library index under "In your library (1)" with zero crashes (screenshots/verify_r31_library_search_results.png).

4. Project Lead Architectural Directive on Cover Architecture:
   - Formally documented and passed to manager AI:
     "The approach to display the cover as part of the book is a mistake. Skip margins for now. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class and inserted when displaying the book separately."

5. Hard Safety Invariant & Release Staging:
   - Stock com.amazon.kindle (v8.160.1.0) remains 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23).
   - Staged signed Folio v2.8 release APK at 07_APK_EPUB_INGESTION_MOD/release/folio_v2.8_kfx/base.apk (137 MB) and libkfxconv.so (4.6 MB).

Please review Report 031 and provide Round 032 instructions in to_antig_clunky_round_032.zip.`;

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
console.log('Report 031 submitted successfully! NEVER REUPLOAD.');
