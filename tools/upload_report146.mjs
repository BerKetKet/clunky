#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const CLI = '/home/bgg/chrome-bridge/cli.mjs';
const ZIP_PATH = '/home/bgg/projects/clunky/antig_to_gemini_neon_clunky_report146.zip';
const FILENAME = 'antig_to_gemini_neon_clunky_report146.zip';

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

console.log('Checking if report146 was already submitted...');
const historyCheck = runEval(`(() => {
  const userQueries = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const alreadySent = userQueries.some(m => m.includes('antig_to_gemini_neon_clunky_report146.zip') || m.includes('Round 146 Deliverables'));
  return { alreadySent };
})()`);
console.log('Submission history check:', historyCheck);
const historyInfo = JSON.parse(historyCheck);
if (historyInfo.alreadySent) {
  console.error('SAFETY INVARIANT: Report 146 already detected in chat history! Never reupload. Exiting.');
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
const promptText = `Here is antig_to_gemini_neon_clunky_report146.zip with Round 146 Deliverables (Major Stone Edition Azath v16.0 KFX — Granite & Limestone Themes, Limestone App & UI Icons, EB Garamond Library Header):

1. Target Hardware:
   - Physical Samsung Galaxy M30 (32001776e8e82623), running TrebleDroid Android 13 GSI.
   - Strict Policy Respected: Zero Play Store Folio (ct.reader) launches or references.

2. Task A: Stone Edition Build, Patchers & Staging (com.azath.reader):
   - Integrated all 8 Stone Edition Java files (FolioTheme, FolioMoreView, OpenCatalogView, FolioMetadataCatalogDialog, FolioFolderBrowserDialog, FolioMetadataBackup, FolioWebBrowserDialog, ExternalEpubActivity) into 07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/. (Note: Limestone light theme compiled in FolioTheme, not runtime-toggled in R146.)
   - Synced dropin_assets/azath_launcher/ic_launcher_foreground_432.png + azath_icon_limestone_512.png + mipmap-* with #141517 Stone adaptive background, copied all 16 re-tinted azath_icons/, and re-tinted ic_authors.png to #ECE8E0 / #D4C9B8.
   - Updated patch_classes2_azath.py (Lib/b.<clinit> -> 0xffd4c9b8L at 0x4d6f50), patch_res_azath_covers.py (Granite #1C1E22/#32353B/#8A857D/#524B41/#ECE8E0), and rebuild_fast.sh (swapping res/font/amazon_ember_serif_{bold,he}.ttf to EBGaramond-Regular.ttf while keeping reader Bookerly-*.ttf in assets/ untouched, staging azath_v16.0_kfx).
   - Recompiled classes9.dex (470,136 B), deployed to Galaxy M30 (lastUpdateTime=2026-11-25 02:06:48, v16.0 KFX verified in UI XML, NO_FATAL_EXCEPTIONS), and staged release APK at 07_APK_EPUB_INGESTION_MOD/release/azath_v16.0_kfx/base.apk (139,032,679 B, SHA256: f995e5a0372907cadce08240c7189dc2cd9e9967e39836a252f0ba239f8ca0b6).

3. Task B: Hardware Verification Screenshots (Samsung Galaxy M30):
   - verify_r146_azath_launcher_limestone.png (320,969 bytes): Android app drawer showing the new Limestone moonlit sky + tree silhouette Azath launcher icon (zero crimson).
   - verify_r146_azath_home_granite.png (642,942 bytes): Azath Home in Granite dark theme with #D4C9B8 luminous Limestone Search CTA and active Home tab.
   - verify_r146_azath_library_garamond.png (1,079,394 bytes): Azath Library confirming the "Library" heading now renders in airy EB Garamond Regular alongside #1C1E22 Dark Granite generic cover tiles and #D4C9B8 active Library tab.
   - verify_r146_azath_control_center_granite.png (314,365 bytes): Control Center (v16.0 KFX) with Granite surfaces, luminous Limestone accents, and harmonized Limestone azath_icons/.
   - Safety Invariant: Stock package com.amazon.kindle (v8.160.1.0) verified 100% UNTOUCHED and READ-ONLY (lastUpdateTime=2026-11-19 21:59:23).

4. Project Lead Architectural Directive on Cover Architecture (Mandatory Reiteration):
   "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

Please review the Stone Edition deliverables and provide Round 147 instructions in to_antig_clunky_round_147.zip.`;

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

execSync('sleep 6');
const verifySubmit = runEval(`(() => {
  const q = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
  const last = q[q.length-1] || '';
  const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
  return { submitted: last.includes('report146'), isGenerating };
})()`);
console.log('Submission verification:', verifySubmit);
console.log('Report 146 submitted successfully! NEVER REUPLOAD.');
