import { execSync } from 'child_process';

const TAB_ID = 837017938;

function runEval(code) {
  return execSync(`node /home/bgg/chrome-bridge/cli.mjs eval id:${TAB_ID} -`, {
    input: code,
    encoding: 'utf-8'
  }).trim();
}

console.log('1. Checking file preview chip in composer...');
const checkChip = runEval(`(() => {
  const previews = Array.from(document.querySelectorAll('uploader-file-preview')).map(p => ({
    text: p.innerText,
    aria: p.getAttribute('aria-label'),
    closeBtn: p.querySelector('button')?.getAttribute('aria-label')
  }));
  return { previews };
})()`);
console.log('Preview chips:', checkChip);
const chipInfo = JSON.parse(checkChip);
if (!chipInfo.previews || chipInfo.previews.length === 0) {
  console.error('ERROR: No attachment chip in composer! Aborting.');
  process.exit(1);
}

const promptText = `Here is antig_to_gemini_neon_clunky_report024.zip (lean 1.8 MB archive, verified attached via the .zip preview chip in composer) covering all Round 024 deliverables:

1. Native Amazon KFX Tables in Pure Rust libkfxconv.so (Task A):
   - Ported jloutsch/kfxgen native table structures into kfx_converter: $278 (yj_table), $279 (yj_table_row), $269 (yj_table_cell) with $146 content_list.
   - Emits border collapse $150: false, vertical/horizontal border spacing $456/$457: 0.9%, and pan/zoom chevron $629: [581, 326], $630: 632 when table columns >= 2.
   - Declares feature table $585 with yj_table (v1, v3) and yj_table_viewer (v1).
   - Verified on-device in screenshots/verify_r24_kfx_native_tables.png (native 6-column financial table with pan chevron).

2. Standalone Cover Separation & Companion JPEG (Task B):
   - Redundant in-spine cover HTML pages (cover.xhtml, titlepage.xhtml, wrap0000.html) are detected and stripped during initial EPUB spine parsing.
   - Preserves native KFX cover resource ($417 raw entity, $164 resource metadata entity, $495 kindle_title_metadata with cover_image: "cover_img").
   - Emits companion <outKfx>.cover.jpg alongside .kfx, loaded directly into Folio's image cache.
   - Verified on-device in screenshots/verify_r24_classic_cover.png (Hesiod cover rendered natively on #000000 black via $270 scale_fit with zero white HTML margins).

3. Typography, Language & Image Parity (Task C):
   - Set override_kindle_font: false to ensure system fonts (Bookerly) are preserved.
   - Extracted <dc:language> from EPUB metadata into KFX metadata ($490), enabling native hyphenation (an- / swered, post- / poned). Verified in screenshots/verify_r24_exam_v3_images_hyphen.png.
   - Transparent PNG compositing over solid white (255, 255, 255) ensures transparent figures blend seamlessly. Verified in screenshots/verify_r24_kfx_images.png.

4. ARM64 Compilation & Folio v2.1 Staging (Task D):
   - Cross-compiled libkfxconv.so for aarch64-linux-android with NDK 26 Clang (stripped size: 4.4 MB).
   - Rebuilt Folio v2.1 (com.amazon.kindle.epub) with extractNativeLibs="true", staged in 07_APK_EPUB_INGESTION_MOD/release/folio_v2.1_kfx/base.apk (131 MB), and verified on Samsung Galaxy M32.
   - Unmodified stock package com.amazon.kindle (v8.160.1.0) remains 100% UNTOUCHED (lastUpdateTime=2026-11-19 21:59:23).

5. Project Lead Architectural Directive on Cover Handling:
   "The approach to display the cover as part of the book text flow is a mistake. Skip it for now. The way to approach the cover to render properly and reliably is: when parsing the EPUB for the first time and converting it, remove the cover page from the book stream and take the image for the cover. It is then processed separately, kept in its own dedicated class/container, and inserted/rendered when displaying the book separately, avoiding margin and reflow issues."

Please review Report 024 and provide Round 025 instructions in to_antig_clunky_round_025.zip.`;

console.log('2. Entering prompt text...');
const enterPrompt = runEval(`(() => {
  const editor = document.querySelector('rich-textarea [contenteditable]') || document.querySelector('.ql-editor');
  if (!editor) return { error: "no editor" };
  editor.focus();
  const ok = document.execCommand('insertText', false, ${JSON.stringify(promptText)});
  return { success: ok, length: editor.innerText.length };
})()`);
console.log('Prompt entry result:', enterPrompt);

console.log('3. Checking Send button and clicking...');
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
console.log('Report 024 submitted successfully with verified attachment chip! NEVER REUPLOAD.');
