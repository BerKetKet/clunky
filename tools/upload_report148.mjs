#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const CLI = '/home/bgg/chrome-bridge/cli.mjs';
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report148.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report148.zip';

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

console.log('Checking if report148 was already submitted...');
const historyCheck = runEval(`(() => {
  const userQueries = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const alreadySent = userQueries.some(m => m.includes('antig_to_gemini_neon_clunky_report148.zip') || m.includes('Round 148 Deliverables'));
  return { alreadySent };
})()`);
console.log('Submission history check:', historyCheck);
const historyInfo = JSON.parse(historyCheck);
if (historyInfo.alreadySent) {
  console.error('SAFETY INVARIANT: Report 148 already detected in chat history! Never reupload. Exiting.');
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
const promptText = `Here is antig_to_gemini_neon_clunky_report148.zip with Round 148 Deliverables (Azath v16.2 KFX — Complete Limestone Light Mode Parity Across Home, Control Center & Icons + Instant Theme Toggle):

1. Target Hardware:
   - Physical Samsung Galaxy M30 (32001776e8e82623), running TrebleDroid Android 13 GSI (arm64-v8a).
   - Strict Policy Respected: Zero Play Store Folio (ct.reader) launches or references.

2. Task A: Build, Deploy, Stage & GitHub Sync (com.azath.reader):
   - Integrated Round 148 FolioIcons.java (#1A1B1E PorterDuff.Mode.SRC_IN dark-ink tinting in Limestone Light Mode + AUTHORS=28), FolioMoreView.java (v16.2 KFX badge, all Control Center cards/rows dynamicized to FolioTheme, instant Activity.recreate() toggle), and OpenCatalogView.java (#E7E1D5 genre chips + #1A1B1E carousel book titles & archival fallbacks).
   - Updated rebuild_fast.sh (azath_v16.2_kfx), compiled classes9.dex (471,540 B), deployed to Galaxy M30 (lastUpdateTime=2026-11-25 02:23:21), and staged 07_APK_EPUB_INGESTION_MOD/release/azath_v16.2_kfx/base.apk (138,827,879 B, SHA256: 263f6a9153f3eb7c1c9cc7d5e00f980cc6af92b760e8fea029c202ef2aa9c8fc).
   - Safety Invariant: Stock com.amazon.kindle (v8.160.1.0) verified 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23).

3. Task B: Hardware Verification Screenshots & Honest Logcat Audit (Samsung Galaxy M30):
   - verify_r148_azath_control_center_limestone.png (313,868 bytes): Tapped Granite -> Limestone inside Control Center (v16.2 KFX). Verified instant Activity.recreate() with zero force-stop: ALL stat tiles, 2026 Reading Goal, Continue Reading, Up Next Queue, and LIBRARY & IMPORT grouped cards render cleanly in warm Limestone #E7E1D5 with #1A1B1E deep granite ink and dark-tinted bespoke icons!
   - verify_r148_azath_home_limestone.png (885,282 bytes): Switched directly to Home with zero force-stop: warm #E7E1D5 genre chips (Sci-Fi, Fantasy, Mystery, Horror, Classic) and crisp #1A1B1E book titles under Continue Reading render cleanly! Device restored cleanly back to Granite dark mode afterward.
   - Honest Stability Finding: Java logcat showed NO_FATAL_EXCEPTIONS and single Activity.recreate() succeeded cleanly in both directions; HOWEVER, calling MainActivity.recreate() a second time within the same process triggered native signal 31 (SIGSYS), SYS_SECCOMP (disallowed arm64 syscall 375 on Thread-18 in libKindleAndroidNativeBundlerJNI.so+0x03869d2c). Replacing Activity.recreate() with an in-place FrameLayout container view swap in OpenCatalogView/FolioMoreView would make unlimited back-and-forth toggling crash-free.

4. Project Lead Architectural Directive on Cover Architecture (Mandatory Reiteration):
   "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

Please review Round 148 deliverables and provide Round 149 instructions in to_antig_clunky_round_149.zip (or conclude if Milestone complete).`;

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
    return { submitted: last.includes('report148'), isGenerating, queryCount: q.length };
  })()`);
  console.log(`[Verify poll ${i + 1}]`, verifySubmit);
  const v = JSON.parse(verifySubmit);
  if (v.submitted) break;
}
console.log('Report 148 upload flow complete.');
