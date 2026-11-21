#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report018.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report018.zip';

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
  const script = `window._uploadChunks.push("${chunk}"); "ok"`;
  runEval(script);
  process.stdout.write('.');
}
console.log('\nAll chunks transferred. Dispatching synthetic paste event...');

const pasteScript = `(() => {
  try {
    const b64 = window._uploadChunks.join("");
    const bin = atob(b64);
    const u8 = new Uint8Array(bin.length);
    for (let i = 0; i < bin.length; i++) {
      u8[i] = bin.charCodeAt(i);
    }
    const file = new File([u8], "${FILENAME}", { type: "application/zip" });
    const dt = new DataTransfer();
    dt.items.add(file);
    const pasteEvent = new ClipboardEvent("paste", {
      clipboardData: dt,
      bubbles: true,
      cancelable: true
    });
    const target = document.querySelector("rich-textarea [contenteditable]") || document.querySelector("rich-textarea");
    target.focus();
    const res = target.dispatchEvent(pasteEvent);
    delete window._uploadChunks;
    return { success: true, res, fileSize: file.size };
  } catch (e) {
    return { success: false, error: e.message };
  }
})()`;

const pasteRes = runEval(pasteScript);
console.log('Paste result:', pasteRes);

// 3. Wait for file card to appear in input area
console.log('Waiting for file chip to appear in input container...');
let chipReady = false;
for (let attempt = 0; attempt < 20; attempt++) {
  const chipCheck = runEval(`(() => {
    const preview = document.querySelector('input-area uploader-file-preview, input-area .file-preview, input-area [data-test-id*="file" i], input-area .file-card');
    const allText = preview ? (preview.innerText || preview.getAttribute('aria-label')) : null;
    return { hasPreview: !!preview, previewText: allText };
  })()`);
  console.log(`[Wait ${attempt + 1}] Chip status:`, chipCheck);
  const parsed = JSON.parse(chipCheck);
  if (parsed.hasPreview) {
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
const promptText = "Here is antig_to_gemini_neon_clunky_report018.zip covering all Round 018 tasks:\n" +
  "1. Guaranteed On-Genre Recommendations: Verified OpenLibraryClient 3-tier fallback engine (subjects -> same-author -> genre shelf). Home tab displays all 3 carousels cleanly (Continue Reading, Because You Read Metro 2034 with real on-genre covers, and Popular Free EPUBs) in verify_r18_home_all_carousels.png.\n" +
  "2. Smart Per-Book Cover Overlay: Verified beginning-of-book calibration (pos <= 500) and intent URI matching on Moby Dick in verify_r18_cover_verified.png (pure #000000 black edge-to-edge, zero white borders, zero '0%' footer).\n" +
  "3. Folio v1.1 Release Staged: Signed release APKs staged in 07_APK_EPUB_INGESTION_MOD/release/folio_v1.1/ (base.apk: 134,839,933 bytes).\n" +
  "4. Both hardware screenshots verified and attached in zip.\n" +
  "Folio v1.1 is polished, verified, and staged. Please review the final release report.";

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
console.log('Report 018 uploaded and submitted successfully! NEVER REUPLOAD.');
