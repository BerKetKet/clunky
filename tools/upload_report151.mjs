#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const CLI = '/home/bgg/chrome-bridge/cli.mjs';
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report151.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report151.zip';

function getTabId() {
  try {
    const raw = execSync(`node ${CLI} tabs`, { encoding: 'utf-8' });
    const tabs = JSON.parse(raw);
    const geminiTab = tabs.find(t => t.title && t.title.includes('Gemini'));
    if (geminiTab) return geminiTab.id;
  } catch (e) {}
  return 837018788;
}

const TAB_ID = getTabId();

if (!fs.existsSync(ZIP_PATH)) {
  console.error(`File not found: ${ZIP_PATH}`);
  process.exit(1);
}

function runEval(code) {
  return execSync(`node ${CLI} eval id:${TAB_ID} -`, {
    input: code,
    encoding: 'utf-8'
  }).trim();
}

console.log(`Activating & targeting Gemini tab ${TAB_ID}...`);
try {
  execSync(`node ${CLI} activate id:${TAB_ID}`, { stdio: 'inherit' });
} catch (e) {}

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

console.log('Checking if report151 was already submitted...');
const historyCheck = runEval(`(() => {
  const userQueries = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const alreadySent = userQueries.some(m => m.includes('antig_to_gemini_neon_clunky_report151.zip') || m.includes('Round 151 Deliverables'));
  return { alreadySent };
})()`);
console.log('Submission history check:', historyCheck);
const historyInfo = JSON.parse(historyCheck);
if (historyInfo.alreadySent) {
  console.error('SAFETY INVARIANT: Report 151 already detected in chat history! Never reupload. Exiting.');
  process.exit(0);
}

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
    target.click();

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

execSync('sleep 2');
console.log('4. Entering prompt text...');
const promptText = `Here is antig_to_gemini_neon_clunky_report151.zip with Round 151 Deliverables (Azath v17.1 KFX — Uniform 26sp Headers, Limestone Heading/Footer Fix & Round 152 Data Dump):

1. Target Hardware:
   - Physical Samsung Galaxy M30 (32001776e8e82623), running TrebleDroid Android 13 GSI (arm64-v8a).
   - Strict Policy Respected: Zero Play Store Folio (ct.reader) launches or references.

2. Task A: Verbatim Drop-In Build, Deploy & Stage (com.azath.reader):
   - Copied all 4 Round 151 files (FolioMoreView, FolioTheme, OpenCatalogView, FolioMetadataCatalogDialog) verbatim into 07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/.
   - Updated rebuild_fast.sh (azath_v17.1_kfx), compiled classes9.dex (474,212 B), deployed to Galaxy M30 (lastUpdateTime=2026-11-25 03:00:15), and staged 07_APK_EPUB_INGESTION_MOD/release/azath_v17.1_kfx/base.apk (138,831,975 B, SHA256: eed25c1e152126c288185163127802d8502386db88b53d997a9daf0b668655fc).
   - Safety Invariant: Stock com.amazon.kindle (v8.160.1.0) verified 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23).

3. Task B: Screenshot + Requested Raw Data for Round 152:
   - verify_r151_azath_catalog_limestone.png (368,135 bytes): Confirmed "Library Metadata & Genres (24)" heading is crisp #1A1B1E on #EFEAE0 and AlertDialog CLOSE footer bar is warm #EFEAE0 with dark #1A1B1E CLOSE button! Toggled cleanly back to Granite dark mode; ZERO_CRASHES_AND_NO_SIGSYS in logcat.
   - "Library" header node bounds on Library tab: bounds="[43,135][178,198]" (height 63px; Search Azath="[158,251][378,356]", bottom bar Library="[630,2193][719,2246]").
   - Raw Lib/b.<clinit> smali surface/container/outline/accent tokens extracted from classes2.dex and included verbatim in REPORT.md (0xffd4c9b8L functionalCorePrimary, 0xff101112L functionalNeutralSurface, 0xff1a1b1cL SurfaceContainerLowest/Low, 0xff242526L SurfaceContainer, 0xff2f3133L SurfaceContainerHigh, 0xffa8abadL OnSurfaceVariant, 0xff8e9194L Outline, 0xff3b3c3dL OutlineVariant).

4. Project Lead Architectural Directive on Cover Architecture (Mandatory Reiteration):
   "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

Please review Round 151 results & raw data dumps and provide Round 152 instructions in to_antig_clunky_round_152.zip.`;

const enterPrompt = runEval(`(() => {
  const editor = document.querySelector('rich-textarea [contenteditable]') || document.querySelector('.ql-editor');
  if (!editor) return { error: "no editor" };
  editor.focus();
  const ok = document.execCommand('insertText', false, ${JSON.stringify(promptText)});
  return { success: ok, length: editor.innerText.length };
})()`);
console.log('Prompt entry result:', enterPrompt);

console.log('5. Checking Send button and clicking...');
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

for (let i = 0; i < 6; i++) {
  execSync('sleep 3');
  const verifySubmit = runEval(`(() => {
    const q = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
    const last = q[q.length-1] || '';
    const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
    return { submitted: last.includes('report151'), isGenerating, queryCount: q.length };
  })()`);
  console.log(`[Verify poll ${i + 1}]`, verifySubmit);
  const v = JSON.parse(verifySubmit);
  if (v.submitted) break;
}
console.log('Report 151 upload flow complete.');
