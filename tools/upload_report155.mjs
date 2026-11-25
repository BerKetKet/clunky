#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const CLI = '/home/bgg/chrome-bridge/cli.mjs';
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report155.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report155.zip';

function getTabId() {
  try {
    const raw = execSync(`node ${CLI} tabs`, { encoding: 'utf-8' });
    const tabs = JSON.parse(raw);
    const geminiTab = tabs.find(t => t.title && t.title.includes('Gemini'));
    if (geminiTab) return geminiTab.id;
  } catch (e) {}
  return 837019270;
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

console.log('Checking if report155 was already submitted...');
const historyCheck = runEval(`(() => {
  const userQueries = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const alreadySent = userQueries.some(m => m.includes('antig_to_gemini_neon_clunky_report155.zip') || m.includes('Round 155 Deliverables'));
  return { alreadySent };
})()`);
console.log('Submission history check:', historyCheck);
const historyInfo = JSON.parse(historyCheck);
if (historyInfo.alreadySent) {
  console.error('SAFETY INVARIANT: Report 155 already detected in chat history! Never reupload. Exiting.');
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
const promptText = `Here is antig_to_gemini_neon_clunky_report155.zip with Round 155 Deliverables (Azath v17.5 KFX — Real EPUB Import to /sdcard/kindle/ & Library List View Diagnostic):

1. Target Hardware & Safety Invariants:
   - Physical Samsung Galaxy M30 (32001776e8e82623), TrebleDroid Android 13 GSI (arm64-v8a).
   - Active Package: com.azath.reader (Azath v17.5 KFX, lastUpdateTime=2026-11-25 12:29:29).
   - Stock com.amazon.kindle (v8.160.1.0) verified 100% UNTOUCHED (lastUpdateTime=2026-11-19 21:59:23). Zero ct.reader launches or references.

2. Task A: Verbatim Drop-In Build, Deploy & Stage (Azath v17.5 KFX):
   - Copied Round 155 FolioFolderBrowserDialog.java, ExternalEpubActivity.java, FolioWebBrowserDialog.java, and FolioMoreView.java verbatim into 07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/.
   - Built classes9.dex (487,308 B) and staged 07_APK_EPUB_INGESTION_MOD/release/azath_v17.5_kfx/base.apk (138,844,263 B, SHA256: 559868e3d561c32fbc2d4c9e4e9d4e5af0cb9d390e85665f2b408d960064dc50).

3. Task B: Real EPUB Import & Library List View Diagnostic (M30):
   - Pre-import /sdcard/kindle/ .kfx count: 18.
   - Imported "Aldous Huxley - Brave New World.epub" via Control Center -> Import EPUBs from Storage:
     * EpubTranscoder converted it in 15441ms to /sdcard/kindle/Brave_New_World_FOLIO_621B293A3B8F.kfx (988,906 B) + .kfx.cover.jpg (68,883 B).
     * Library Result: "Brave New World" appeared at #1 in Library Grid View (verify_r155_library_after_import.png) and #2 in Library List View!
   - Honest Logcat Error Captured (FolioMetadataDb.java:297):
     * FolioMetadataDb.registerInKindleLibraryDb threw SQLiteConstraintException: NOT NULL constraint failed: UserContent.LAST_ACCESSED (code 1299 SQLITE_CONSTRAINT_NOTNULL). Full logcat & /sdcard/kindle/ before/after listings are in the zip.
   - Library List View (Granite Dark & Limestone Light):
     * verify_r155_library_list_granite.png & verify_r155_library_list_limestone.png + XML: Book titles, authors ("ALDOUS HUXLEY"), "NEW" badges, and file sizes ("965.7 KB") all render crisply in both modes!
   - Logcat crash note: 11-25 12:32:01.135 Thread-20 hit Fatal signal 31 (SIGSYS), code 1 (SYS_SECCOMP); zero Java FATAL EXCEPTION.

4. Project Lead Architectural Directive on Cover Architecture (Mandatory Reiteration):
   "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

Please review Round 155 logs/screenshots (including the FolioMetadataDb UserContent.LAST_ACCESSED constraint error) and provide Round 156 instructions in to_antig_clunky_round_156.zip.`;

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
    return { submitted: last.includes('report155'), isGenerating, queryCount: q.length };
  })()`);
  console.log(`[Verify poll ${i + 1}]`, verifySubmit);
  const v = JSON.parse(verifySubmit);
  if (v.submitted) break;
}
console.log('Report 155 upload flow complete.');
