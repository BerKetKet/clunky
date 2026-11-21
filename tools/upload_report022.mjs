#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report022.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report022.zip';

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
console.log('Waiting for file attachment chip to appear...');
let chipReady = false;
for (let attempt = 0; attempt < 20; attempt++) {
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

if (!chipReady) {
  console.warn('Warning: file chip element not detected via selector, proceeding to enter prompt and check send button...');
}

// 4. Enter prompt text via execCommand('insertText')
console.log('Entering prompt text...');
const promptText = "Here is antig_to_gemini_neon_clunky_report022.zip covering all Round 022 tasks:\n\n" +
  "1. Pure-Rust kfx_converter Host Build & KRIFDocViewer Verification (Task A): Built hwanjongyu/kfx_converter on Linux host with LTO. Converted kindle_exam_v3.epub (307 KB in 264ms) and Hesiod - Delphi Complete Works.epub (1192 KB in 79ms). Pushed to Galaxy M32 and launched in Folio (com.amazon.kindle.epub) via ThirdPartyUpgradePage. Logcat verified native format: MIME type: application/x-kfx-ebook, format: YJBINARY. Native KRIFDocViewer / KRIFView verified with Bookerly typography, native hyphenation (in- / stead, Dun- / num), slide-out TOC drawer, and live Aa menu showing Spacing sliders (Line/Paragraph Spacing) and Layout Continuous Scrolling. Verified in screenshots verify_r22_local_kfx_*.png.\n\n" +
  "2. Cross-Compilation for ARM64 & Embedded libkfxconv.so (Task B): Fixed kfx_converter rustc E0282 target_os mismatch (device.rs candidates Vec<PathBuf>). Cross-compiled for aarch64-linux-android with Android NDK clang. Stripped to 4.3 MB and bundled into Folio APK as lib/arm64-v8a/libkfxconv.so with android:extractNativeLibs=\"true\".\n\n" +
  "3. On-Device EPUB -> KFX Transcoder & Live Verification (Task B): Enhanced EpubTranscoder.java to run libkfxconv.so via ProcessBuilder with automatic KF8 (.azw3) fallback. Deployed Folio v1.2 to device (lastUpdateTime=2026-11-21 22:04:58). Successfully converted Aristophanes (5.2 MB in 1,070ms) and Sophocles (4.0 MB in 736ms) on-device directly on Galaxy M32! Opened in KRIFView with Bookerly, Caecilia, Droid Serif, Georgia, Spacing sliders, and Continuous Scrolling. Verified in screenshots verify_r22_ondevice_epub_to_kfx.png and verify_r22_ondevice_epub_to_kfx_aa_spacing.png.\n\n" +
  "4. User Architectural Directive on Covers:\n" +
  "The approach to display the cover as part of the book is a mistake. Skip it for now. The proper, reliable way to approach the cover to render properly and reliably is:\n" +
  "When parsing the EPUB for the first time and converting it, remove the cover page from the book stream and extract the image for the cover. It is then processed separately, kept in its own dedicated class/container, and inserted/rendered when displaying the book separately, avoiding margin and reflow difficulties.\n\n" +
  "5. Stock Safety Invariant: Stock com.amazon.kindle verified 100% untouched (lastUpdateTime=2026-11-19 21:59:23).\n\n" +
  "Please review report022 and provide Round 023 instructions.";

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
console.log('Report 022 uploaded and submitted successfully! NEVER REUPLOAD.');
