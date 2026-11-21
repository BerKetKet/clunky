#!/usr/bin/env node
import { execSync } from 'child_process';

const TAB_ID = 837017938;

function runEval(code) {
  return execSync(`node /home/bgg/chrome-bridge/cli.mjs eval id:${TAB_ID} -`, {
    input: code,
    encoding: 'utf-8'
  }).trim();
}

console.log('1. Verifying attachment chip in composer...');
const chipCheck = runEval(`(() => {
  const chip = document.querySelector('[aria-label*="close antig_to_g" i]') || document.querySelector('.gem-attachment-content');
  const sendBtn = document.querySelector('button[aria-label*="Send message" i]');
  return {
    hasChip: !!chip,
    chipLabel: chip ? (chip.getAttribute('aria-label') || chip.innerText) : null,
    sendDisabled: sendBtn ? (sendBtn.disabled || sendBtn.getAttribute('aria-disabled') === 'true') : true
  };
})()`);
console.log('Chip check:', chipCheck);
const chipInfo = JSON.parse(chipCheck);

if (!chipInfo.hasChip) {
  console.error('ERROR: Attachment chip not present! Aborting.');
  process.exit(1);
}

console.log('2. Entering prompt text...');
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

console.log('3. Clicking Send button...');
let sent = false;
for (let attempt = 0; attempt < 5; attempt++) {
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
