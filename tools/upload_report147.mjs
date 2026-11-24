#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const CLI = '/home/bgg/chrome-bridge/cli.mjs';
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report147.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report147.zip';

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

console.log('Checking if report147 was already submitted...');
const historyCheck = runEval(`(() => {
  const userQueries = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const alreadySent = userQueries.some(m => m.includes('antig_to_gemini_neon_clunky_report147.zip') || m.includes('Round 147 Deliverables'));
  return { alreadySent };
})()`);
console.log('Submission history check:', historyCheck);
const historyInfo = JSON.parse(historyCheck);
if (historyInfo.alreadySent) {
  console.error('SAFETY INVARIANT: Report 147 already detected in chat history! Never reupload. Exiting.');
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
const promptText = `Here is antig_to_gemini_neon_clunky_report147.zip with Round 147 Deliverables (Azath v16.1 KFX — Adaptive Launcher Icon Safe-Zone Framing + Live Granite/Limestone Theme Switcher):

1. Target Hardware:
   - Physical Samsung Galaxy M30 (32001776e8e82623), running TrebleDroid Android 13 GSI (arm64-v8a).
   - Strict Policy Respected: Zero Play Store Folio (ct.reader) launches or references.

2. Task A: Launcher Icon Update, Build, Deploy, Stage & GitHub Push (com.azath.reader):
   - Integrated Round 147 FolioMoreView.java (v16.1 KFX badge + Granite/Limestone toggle pill) and OpenCatalogView.java (dynamic FolioTheme getters) into 07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/.
   - Synced safe-zone-framed dropin_assets/azath_launcher/ic_launcher_foreground_432.png -> dropin_res/ic_launcher_foreground.png, azath_icon_limestone_512.png -> dropin_res/ic_launcher_full_512.png, and dropin_res/mipmap-*.
   - Updated rebuild_fast.sh to stage azath_v16.1_kfx, compiled classes9.dex (471,184 B), deployed to Galaxy M30 (lastUpdateTime=2026-11-25 02:14:27, NO_FATAL_EXCEPTIONS), and staged 07_APK_EPUB_INGESTION_MOD/release/azath_v16.1_kfx/base.apk (138,827,879 B, SHA256: e92c910b32732eb6d4d8723f0acd929e2858accbbd594c2c5a93184d7f9ded31).
   - Safety Invariant: Stock com.amazon.kindle (v8.160.1.0) verified 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23).

3. Task B: Hardware Verification Screenshots & Honest Findings (Samsung Galaxy M30):
   - verify_r147_azath_launcher_safezone.png (325,542 bytes): Circular app drawer icon cleanly frames the entire crescent moon, overhanging tree branches, and reader silhouette with open book inside the 69% safe zone.
   - verify_r147_azath_home_limestone_light.png (874,452 bytes): Home screen in warm Limestone Light Mode (#F4F0E8 canvas, #E7E1D5 cards, #2D3138 pills, EB Garamond Azath). Honest finding #1: Continue Reading book titles below covers still render in light #ECE8E0 against #F4F0E8 (nearly invisible) and need FolioTheme.textPrimary(ctx).
   - verify_r147_azath_control_center_limestone.png (314,002 bytes): Control Center in Limestone Light Mode before toggling back to Granite dark mode. Honest finding #2: inner Control Center cards (stats tiles, 2026 Reading Goal, Continue Reading, Up Next Queue, Library & Import list) still render with static #1C1E22 dark surfaces instead of FolioTheme.bgSurface/bgControl/textPrimary. Honest finding #3: toggling Granite <-> Limestone in More does not rebuild the cached Home view on in-app tab switch; a reopen (force-stop + launch) was required for Home to reflect Limestone.

4. Project Lead Architectural Directive on Cover Architecture (Mandatory Reiteration):
   "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

Please review the Round 147 hardware screenshots & findings and provide Round 148 instructions in to_antig_clunky_round_148.zip.`;

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
    return { submitted: last.includes('report147'), isGenerating, queryCount: q.length };
  })()`);
  console.log(`[Verify poll ${i + 1}]`, verifySubmit);
  const v = JSON.parse(verifySubmit);
  if (v.submitted) break;
}
console.log('Report 147 upload flow complete.');
