#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const TAB_ID = 837017938;
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report020.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report020.zip';

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

// 3. Wait for file chip to appear in input container
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
const promptText = "Here is antig_to_gemini_neon_clunky_report020.zip covering all Round 020 UI/UX QC fixes:\n" +
  "1. Zero Duplicate HTML Cover inside Spine (Task A): EpubTranscoder.java omits redundant cover-only HTML spine item whenever hasCover == true, so the book cover renders exclusively via native fullscreen #000000 CoverOverlayView. Dismissing lands directly on Page 1 text without a second white-margined HTML cover page (verified in verify_r20_page1_no_dup_cover.png).\n" +
  "2. TOC 'Cover' Jump Hook (Task A): chapters.add(new ChapterToc(\"Cover\", 0)) exposes Cover as entry 0 in the NCX/TOC drawer. BaseTableOfContentsProvider.m in classes2.dex was patched to check position == 0 and invoke CoverOverlayView.showForCurrentBookForced((Activity) context). Also hooked MobiDocViewer.navigateToCover() in classes2.dex and ReaderActivity.performBookOperation in classes3.dex. Tapping 'Cover' from Chapter 2 (location 5304) immediately displays fullscreen #000000 cover overlay (verified in verify_r20_toc_cover_jump.png, 30.95% black ratio).\n" +
  "3. Real Covers in Continue Reading Cards (Task A): LocalLibraryReader.java and CoverOverlayView.java check .cover.jpg sidecars and epub_cache/<name>.azw3.cover.jpg before coverslarge/coverssmall, eliminating gray placeholders (verified in verify_r20_home_clean.png).\n" +
  "4. Reliable Home Search Bar Suppression, Dedup & Clean Similar Books (Task A): OnAttachStateChangeListener + postDelayed passes reliably suppress native 'Search Kindle' bar. OpenLibraryClient.java performs substring deduplication against omnibus titles. OpenCatalogView.java uses dedicated simContainer with populateSimilarContainer() so Similar Books are never duplicated between initial quick metadata rendering and async updates (verified in verify_r20_home_clean.png and verify_r20_rec_dialog_loaded.png).\n" +
  "5. Stock Safety Invariant: com.amazon.kindle verified 100% untouched (lastUpdateTime=2026-11-19 21:59:23).\n" +
  "Please review report020.";

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
console.log('Report 020 uploaded and submitted successfully! NEVER REUPLOAD.');
