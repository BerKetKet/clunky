#!/usr/bin/env node
import fs from 'fs';
import { execSync } from 'child_process';

const CLI = '/home/bgg/chrome-bridge/cli.mjs';
const REPORT_PATH = '/home/bgg/projects/clunky/REPORT.md';

function getTabId() {
  try {
    const raw = execSync(`node ${CLI} tabs`, { encoding: 'utf-8' });
    const tabs = JSON.parse(raw);
    const geminiTab = tabs.find(t => t.title && t.title.includes('Gemini'));
    if (geminiTab) return geminiTab.id;
  } catch (e) {}
  return 837019305;
}

const TAB_ID = getTabId();
const reportText = fs.readFileSync(REPORT_PATH, 'utf-8');

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

const checkModel = runEval(`(() => {
  const switchBtn = document.querySelector('.input-area-switch, [aria-label*="currently Neon" i]');
  const txt = switchBtn ? switchBtn.innerText : '';
  const aria = switchBtn ? switchBtn.getAttribute('aria-label') : '';
  const isNeon = (txt && txt.includes('Neon')) || (aria && aria.includes('Neon')) || document.querySelector('[aria-label*="currently Neon" i]') !== null;
  return { isNeon, txt, aria };
})()`);
console.log('Model check result:', checkModel);
if (!JSON.parse(checkModel).isNeon) {
  console.error('ERROR: Model is not Neon! Aborting.');
  process.exit(1);
}

const promptText = `Here is the Round 163 QC Remediation Report (Azath v18.4 KFX, branch round-163-qc-fixes, pushed to origin/round-163-qc-fixes with one commit per task A1-A4 & B1-B4 — no source zips):\n\n${reportText}`;

const enterPrompt = runEval(`(() => {
  const editor = document.querySelector('rich-textarea [contenteditable]') || document.querySelector('.ql-editor');
  if (!editor) return { error: "no editor" };
  editor.focus();
  const ok = document.execCommand('insertText', false, ${JSON.stringify(promptText)});
  return { success: ok, length: editor.innerText.length };
})()`);
console.log('Prompt entry result:', enterPrompt);

execSync(`node ${CLI} click id:${TAB_ID} --trusted "button[aria-label*='Send message' i]"`, { stdio: 'inherit' });

for (let i = 0; i < 6; i++) {
  execSync('sleep 3');
  const verifySubmit = runEval(`(() => {
    const q = Array.from(document.querySelectorAll('.user-query-container, user-query')).map(m => m.innerText || '');
    const last = q[q.length-1] || '';
    const isGenerating = !!document.querySelector('button[aria-label*="Stop" i]');
    return { submitted: last.includes('round-163-qc-fixes'), isGenerating, queryCount: q.length };
  })()`);
  console.log(`[Verify poll ${i + 1}]`, verifySubmit);
  if (JSON.parse(verifySubmit).submitted) break;
}
console.log('Round 163 QC Remediation report submitted.');
