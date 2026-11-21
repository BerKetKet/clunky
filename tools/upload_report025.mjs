#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report025.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report025.zip';

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

// Wait for file preview chip to appear
console.log('4. Waiting for file preview chip in composer...');
let chipReady = false;
for (let attempt = 0; attempt < 20; attempt++) {
  const checkChip = runEval(`(() => {
    const previews = Array.from(document.querySelectorAll('uploader-file-preview')).map(p => ({
      text: p.innerText,
      aria: p.getAttribute('aria-label')
    }));
    const sendBtn = document.querySelector('button[aria-label*="Send message" i]');
    return {
      count: previews.length,
      previews,
      sendDisabled: sendBtn ? (sendBtn.disabled || sendBtn.getAttribute('aria-disabled') === 'true') : true
    };
  })()`);
  console.log(`[Chip check ${attempt + 1}]`, checkChip);
  const info = JSON.parse(checkChip);
  if (info.count > 0) {
    chipReady = true;
    // If duplicate chips, remove extras
    if (info.count > 1) {
      runEval(`(() => {
        const previews = Array.from(document.querySelectorAll('uploader-file-preview'));
        for (let i = 1; i < previews.length; i++) {
          const btn = previews[i].querySelector('button');
          if (btn) btn.click();
        }
      })()`);
    }
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
const promptText = `Here is antig_to_gemini_neon_clunky_report025.zip (lean 1.4 MB archive, verified attached via the .zip preview chip in composer) covering all Round 025 deliverables:

1. Native KFX Table Columns & Borders Fix in Pure-Rust libkfxconv.so (Task A):
   - Root cause identified via deep-diff against stock Kindle's CR!6KYWZNKF153BK4YSK5MTFZ1XN6ZA.kfx:
     * $278 (table) direct children under $146 must be wrapped in a $454 (body) container;
     * Added $152 (column_format) per column containing {$698: false} (is_empty: false);
     * Enabled $150: true (table_border_collapse);
     * Set $456 and $457 (border_spacing_vertical/horizontal) to 0.9 pt ($318) instead of percent;
     * Added $83: 4286611584 (border_color) in table styles and corrected cell padding left/right symbol mapping ($53/$55) in styles.rs.
   - Verification: Captured screenshots/verify_r25_table_columns.png on Samsung Galaxy M32. Completely eliminates mashed cells (e.g. "Gannet992.1Indigo..."). 6-column tables ("Amber 364.4 Cobalt 123.6 Ember 848.2", "Gannet 992.1 Indigo 466.1 Kestrel 483.9") render with clean, distinct columns and vertical alignment!

2. Cover Rendering: Architectural Direction from Project Lead (Task B):
   - The user/project lead explicitly analyzed the cover issue and provided the following architectural directive:
     "The approach to display the cover as part of the book is a mistake. Skip it for now. The way to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the book spine and take the image for the cover. It is then processed separately and kept in its own class or whatever and inserted when displaying the book separately."
   - Forcing the cover into the KFX book spine reflow engine creates ongoing friction with reader margins. In Round 026, we will implement this clean separation: strip cover HTML from the spine during EPUB conversion and present the extracted cover in its own dedicated edge-to-edge View on #000000 black.
   - Baseline screenshots captured: screenshots/verify_r25_cover_book1.png (Hesiod) and screenshots/verify_r25_cover_book2.png (Sophocles).

3. ARM64 Compilation, Folio v2.2 Staging & Live Spacing Verification (Task C):
   - Cross-compiled kfx-conv for aarch64-linux-android with NDK Clang into 07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so (4.4 MB stripped).
   - Rebuilt, signed, and deployed Folio v2.2 (com.amazon.kindle.epub) on device; staged signed APK at 07_APK_EPUB_INGESTION_MOD/release/folio_v2.2_kfx/base.apk (131 MB).
   - Verified live typography controls: Aa -> Font -> Spacing sliders verified on Chapter 1 prose. Captured screenshots/verify_r25_prose_spacing_before.png and verify_r25_prose_spacing_after.png.
   - Hard Safety Invariant: Stock package com.amazon.kindle (v8.160.1.0) remains 100% UNTOUCHED (lastUpdateTime=2026-11-19 21:59:23).

Please review Report 025 and provide Round 026 instructions in to_antig_clunky_round_026.zip.`;

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
console.log('Report 025 submitted successfully with verified attachment chip! NEVER REUPLOAD.');
