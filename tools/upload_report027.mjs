#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report027.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report027.zip';

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

// 1. Verify model is Neon
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

// Clean any open overlays
runEval(`(() => {
  const backdrops = document.querySelectorAll('.cdk-overlay-backdrop, .mat-mdc-menu-panel');
  backdrops.forEach(b => b.remove());
})()`);

// 2. Transfer file chunks
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

// 3. Assemble and attach File via paste event on rich-textarea
console.log('3. Attaching file via paste event...');
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

    const target = document.querySelector('rich-textarea') || document.querySelector('.input-area') || document.querySelector('[contenteditable]');
    if (!target) return { error: "target not found" };

    const pasteEvent = new ClipboardEvent('paste', {
      bubbles: true,
      cancelable: true,
      clipboardData: dt
    });
    target.dispatchEvent(pasteEvent);

    return { pasteTriggered: true, fileName: file.name, fileSize: file.size };
  } catch (e) {
    return { error: e.toString() };
  }
})()`);
console.log('Attach result:', attachResult);

// 4. Wait for file preview chip to appear in composer
console.log('4. Waiting for file preview chip in composer...');
let chipReady = false;
for (let attempt = 0; attempt < 20; attempt++) {
  const checkChip = runEval(`(() => {
    const chip = document.querySelector('[aria-label*="close antig_to_g" i]') || 
                 document.querySelector('.gem-attachment-content') || 
                 document.querySelector('uploader-file-preview') ||
                 document.querySelector('mat-chip');
    const sendBtn = document.querySelector('button[aria-label*="Send message" i]');
    return {
      hasChip: !!chip,
      chipLabel: chip ? (chip.getAttribute('aria-label') || chip.innerText) : null,
      sendDisabled: sendBtn ? (sendBtn.disabled || sendBtn.getAttribute('aria-disabled') === 'true') : true
    };
  })()`);
  console.log(`[Chip check ${attempt + 1}]`, checkChip);
  const info = JSON.parse(checkChip);
  if (info.hasChip) {
    chipReady = true;
    break;
  }
  execSync('sleep 2');
}

if (!chipReady) {
  console.error('ERROR: Attachment chip failed to appear in composer! Aborting.');
  process.exit(1);
}

// 5. Enter prompt text
console.log('5. Entering prompt text...');
const promptText = `Here is antig_to_gemini_neon_clunky_report027.zip (lean 3.0 MB archive, verified attached via the .zip preview chip in composer) covering all Round 027 deliverables:

1. Pure-Rust HTML Whitespace Normalization (Task A & B.1):
   - Collapsed ASCII whitespace (\\r, \\n, \\t, consecutive spaces) into a single ' ' inside text nodes (preserving <br/> and <pre>) and trimmed span boundary spaces in src/epub/xhtml.rs.
   - Completely resolved stray line breaks in Contents: verified on screenshots/verify_r27_sophocles_contents_fixed.png ("THE WOMEN OF TRACHIS" and "OEDIPUS THE KING" now render cleanly on single continuous lines).

2. Multi-Byte UTF-16 Code Unit Style Offsets (Task A & B.3):
   - Implemented .encode_utf16().count() in src/packager/storylines.rs matching YellowJersey Java/ICU/Skia UTF-16 code unit offset requirements.
   - Verified dramatic verse dialogue and speaker cues in screenshots/verify_r27_prose_emphasis_verse.png.

3. Native YellowJersey Internal Hyperlinks ($266 & $179) (Task A & B.2):
   - Emitted native $266 anchor entities ($180 anchor_sym, $183: {$155: target_eid, $143: offset}) and $179 link_to style events in $142.
   - Verified live in-book jump from Location 10 ("ANTIGONE") to Location 508 in screenshots/verify_r27_internal_link_jump.png with "Back to 10" safety net breadcrumb.

4. Systematic UI/UX Audit Sweep (Task C):
   - Reader Black/Dark mode: screenshots/verify_r27_reader_dark_theme.png (pure #000000 background, crisp white text, clean footer).
   - Library Tab: screenshots/verify_r27_library_tab.png (local KFX books with real covers).
   - Home Tab: screenshots/verify_r27_home_tab.png (unified search and Continue Reading carousel).
   - Documented UI/UX Audit Findings list and User Architectural Directive on Cover Rendering in REPORT.md.

5. Project Lead Architectural Directive on Cover Rendering:
   - "The approach to display the cover as part of the book is a mistake. The correct path is to just take the image and render it differently so you don't have the difficulty you are having with margins. Pass that along to the manager AI. When parsing the EPUB for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class or whatever and inserted when displaying the book separately."
   - Detailed roadmap for Folio v2.5 documented in REPORT.md.

6. Hard Safety Invariant & Staging:
   - Stock com.amazon.kindle (v8.160.1.0) remains 100% UNTOUCHED (lastUpdateTime=2026-11-19 21:59:23).
   - Staged signed Folio v2.4 in 07_APK_EPUB_INGESTION_MOD/release/folio_v2.4_kfx/base.apk (131 MB).

Please review Report 027 and provide Round 028 instructions in to_antig_clunky_round_028.zip.`;

const enterPrompt = runEval(`(() => {
  const editor = document.querySelector('rich-textarea [contenteditable]') || document.querySelector('.ql-editor');
  if (!editor) return { error: "no editor" };
  editor.focus();
  const ok = document.execCommand('insertText', false, ${JSON.stringify(promptText)});
  return { success: ok, length: editor.innerText.length };
})()`);
console.log('Prompt entry result:', enterPrompt);

// 6. Click Send button
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
console.log('Report 027 submitted successfully with verified attachment chip! NEVER REUPLOAD.');
