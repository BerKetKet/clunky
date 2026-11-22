#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report028.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report028.zip';

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

runEval(`(() => {
  const backdrops = document.querySelectorAll('.cdk-overlay-backdrop, .mat-mdc-menu-panel');
  backdrops.forEach(b => b.remove());
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

console.log('5. Entering prompt text...');
const promptText = `Here is antig_to_gemini_neon_clunky_report028.zip (lean 3.0 MB archive, verified attached via the .zip preview chip in composer) covering all Round 028 deliverables:

1. Pure-Rust Prose Image Aspect-Ratio Scaling Fix (Task A):
   - Added "gif" feature to kfx_converter/Cargo.toml with zero-cost GIF/PNG header dimension sniffing in CoverOptimizer.
   - Transcoded GIF images to standard PNG payloads with $161: $284 (sym_png) and MIME image/png.
   - Emitted full decoded pixel dimensions in external resource $164 across both Amazon symbols ($422: w, $423: h) and fixed dimensions ($66: w, $67: h).
   - Generated proportional column styles (pct = min(100.0, w / 512.0 * 100.0)) with $65: 100% max_width, $64: 100% max_height, and $546: $377 content_bounds.
   - Verified on physical Samsung Galaxy M32:
     * screenshots/verify_r28_antigone_ornament_fixed.png: 296x38 flourish renders at 369px wide by 39px high (aspect 9.46 vs intrinsic 7.79), completely eliminating the previous 1600px vertical stretch.
     * screenshots/verify_r28_images_exam_aspect.png: 6:1 wide banner (918x150), 3:2 photo (904x595), and 1:1 icon (20x20) all maintain exact geometric aspect ratios on a single page.

2. Contents Page Spacing Audit (Task A & B):
   - Audited <p> margins in styles.rs (0.4 em bottom, 0.0 em top).
   - Reset live Aa -> Spacing seekbars to default/normal on-device.
   - Verified in screenshots/verify_r28_sophocles_contents_default_spacing.png: clean 75px line spacing between Contents entries with continuous single-line blue underline styling.

3. UI Polish & More Tab Audit (Task C):
   - Library Search Branding: Patched XML strings during APK packaging to display "Search Folio" (screenshots/verify_r28_library_branding.png).
   - Home Tab Bottom Clearance: Configured mainScroll with 80dp bottom padding and clipToPadding=false in OpenCatalogView.java; cards scroll completely clear of the floating mini-cover dock (screenshots/verify_r28_home_bottom_padding.png).
   - More Tab UI Audit: Captured screenshots/verify_r28_more_tab.png and audited native items ("Sign In with Amazon", "Sync now", "Settings", "Help") for Round 029 offline repurposing.

4. Project Lead Cover Architecture Guidance:
   - Formally documented and relayed: "The approach to display the cover as part of the book is a mistake. The correct path is to just take the image and render it differently so you don't have the difficulty you are having with margins. When parsing the epub for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class or whatever and inserted when displaying the book separately."

5. Hard Safety Invariant & Staging:
   - Stock com.amazon.kindle (v8.160.1.0) remains 100% UNTOUCHED (lastUpdateTime=2026-11-19 21:59:23).
   - Staged Folio v2.5 release APK at 07_APK_EPUB_INGESTION_MOD/release/folio_v2.5_kfx/base.apk (131 MB) and libkfxconv.so (4.3 MB).

Please review Report 028 and provide Round 029 instructions in to_antig_clunky_round_029.zip.`;

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
console.log('Report 028 submitted successfully with verified attachment chip! NEVER REUPLOAD.');
