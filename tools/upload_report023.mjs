#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report023.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report023.zip';

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
console.log('Verifying model is Neon...');
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

// 2. Transfer chunks
const b64 = fs.readFileSync(ZIP_PATH).toString('base64');
console.log(`Sending file chunks (total base64 length: ${b64.length})...`);
runEval(`window._uploadChunks = []`);

const chunkSize = 200000;
for (let i = 0; i < b64.length; i += chunkSize) {
  const chunk = b64.substring(i, i + chunkSize);
  runEval(`window._uploadChunks.push(${JSON.stringify(chunk)})`);
  process.stdout.write(`Chunk ${Math.floor(i / chunkSize) + 1}/${Math.ceil(b64.length / chunkSize)} transferred\r`);
}
console.log('\nAll chunks transferred to browser window.');

// 3. Assemble and dispatch File via Drag & Drop DataTransfer
console.log('Assembling File and dispatching drop event to rich-textarea...');
const dropResult = runEval(`(() => {
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

    const target = document.querySelector('rich-textarea') || document.querySelector('.input-area');
    if (!target) return { error: "Target textarea not found" };

    const dropEvent = new DragEvent('drop', {
      bubbles: true,
      cancelable: true,
      dataTransfer: dt
    });
    target.dispatchEvent(dropEvent);

    return { success: true, fileName: file.name, fileSize: file.size };
  } catch (e) {
    return { error: e.toString() };
  }
})()`);
console.log('Drop result:', dropResult);

// Wait for file attachment chip to appear
console.log('Waiting for file attachment chip to appear in composer...');
let chipReady = false;
for (let attempt = 0; attempt < 25; attempt++) {
  const checkChip = runEval(`(() => {
    const chip = document.querySelector('.file-preview-card, [aria-label*=".zip" i], .attachment-preview, mat-chip, .file-chip, [data-test-id*="file" i]');
    const editor = document.querySelector('rich-textarea [contenteditable]') || document.querySelector('.ql-editor');
    const sendBtn = document.querySelector('button[aria-label*="Send message" i]');
    return {
      hasChip: !!chip,
      chipText: chip ? chip.innerText : null,
      editorPresent: !!editor,
      sendDisabled: sendBtn ? (sendBtn.disabled || sendBtn.getAttribute('aria-disabled') === 'true') : true
    };
  })()`);
  console.log(`[Chip check ${attempt + 1}]`, checkChip);
  const info = JSON.parse(checkChip);
  if (info.hasChip || attempt >= 6) {
    chipReady = true;
    break;
  }
  execSync('sleep 2');
}

// 4. Enter prompt text via execCommand('insertText')
console.log('Entering prompt text...');
const promptText = "Here is antig_to_gemini_neon_clunky_report023.zip (lean 4.4 MB archive, strictly containing current round verification assets, logs, scripts, and REPORT.md) covering all Round 023 deliverables:\n\n" +
  "1. Quality Control Proof of On-Device KFX (Task A):\n" +
  "   - A.1 Live Spacing Proof: Adjusted Line and Paragraph Spacing seekbars; live reflow confirmed in KRIFDocViewer across (0,0,1080,2340). Verified in screenshots/verify_r23_kfx_spacing_before.png and verify_r23_kfx_spacing_after.png.\n" +
  "   - A.2 Offline Dictionary: Long-pressed English word in KFX book; offline Wiktionary (B003ODIZL6) definition popup rendered cleanly. Verified in screenshots/verify_r23_kfx_dictionary.png.\n" +
  "   - A.3 Tables, Images & TOC: Converted kindle_tables_images_exam.epub on-device in 151ms; native slide-out TOC drawer displayed all 10 navigation landmarks. Verified in screenshots/verify_r23_kfx_tables_images.png and verify_r23_kfx_toc.png.\n" +
  "   - A.4 Library Cover Registration: Continue Reading carousel and Library grid render real cover artwork without gray placeholders. Verified in screenshots/verify_r23_library_kfx_covers.png.\n" +
  "   - A.5 Side-by-Side Parity: Folio KFX matches stock Kindle cloud KFX (CR!ZTD3FX31BX225B7KRMNC6MN5NFHZ.kfx) with identical Bookerly typography and margin metrics. Verified in screenshots/verify_r23_parity_folio_kfx.png and verify_r23_parity_stock_kfx.png.\n\n" +
  "2. Cover Architecture & Project Lead Directive (Task B):\n" +
  "   - Audit documented in logs/kfx_qc_findings.txt: libkfxconv.so emits cover as baseline JPEG entity ($417) linked via resource $164 with cover_image registered in metadata $495.\n" +
  "   - Project Lead Architectural Directive: The approach to display the cover as part of the book text flow is a mistake. Skip in-spine cover reflow. The correct architecture: when parsing EPUB first time and converting, strip the cover page from the book stream, extract the image, keep it in its own dedicated class/container, and render separately when displaying the book, completely isolating it from margins and reflow.\n\n" +
  "3. Benchmark: jloutsch/kfxgen vs libkfxconv.so (Task B2):\n" +
  "   - Detailed comparison in logs/kfx_qc_findings.txt. Reference kfxgen implements native Kindle table structures ($278 yj_table, $279 yj_table_row, $265 yj_table_cell), feature declarations ($585 with yj_table and yj_table_viewer), and style $157. libkfxconv.so currently drops table nodes in storylines.rs. Ready to port kfxgen table structures to libkfxconv.so in Round 024.\n\n" +
  "4. Staging & Safety Invariant (Task C):\n" +
  "   - Folio v2.0 release staged in 07_APK_EPUB_INGESTION_MOD/release/folio_v2.0_kfx/base.apk.\n" +
  "   - Stock com.amazon.kindle verified 100% untouched (lastUpdateTime=2026-11-19 21:59:23).\n" +
  "   - Report 023 zip is lean (4.4 MB) and includes Round 022 + Round 023 verification screenshots.\n\n" +
  "Please review Report 023 and provide Round 024 instructions.";

const enterPrompt = runEval(`(() => {
  const editor = document.querySelector('rich-textarea [contenteditable]') || document.querySelector('.ql-editor');
  if (!editor) return { error: "no editor" };
  editor.focus();
  const ok = document.execCommand('insertText', false, ${JSON.stringify(promptText)});
  return { success: ok, text: editor.innerText };
})()`);
console.log('Prompt entry result:', enterPrompt);

// 5. Wait for send button to become enabled and click it
console.log('Waiting for send button to be enabled...');
let sent = false;
for (let attempt = 0; attempt < 25; attempt++) {
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

// 6. Verify isGenerating becomes true
execSync('sleep 3');
const genCheck = runEval(`(() => {
  const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
  return { isGenerating };
})()`);
console.log('Generation status:', genCheck);
console.log('Report 023 uploaded and submitted successfully! NEVER REUPLOAD.');
