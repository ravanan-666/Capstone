// generator/build_docs.js
// Master build script to generate DJ Mart Technical Documentation HTML and compile to PDF via headless browser

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const styles = require('./styles');
const ch1_3 = require('./ch1_to_3');
const ch4_5 = require('./ch4_to_5');
const ch6_8 = require('./ch6_to_8');
const ch9_12 = require('./ch9_to_12');
const ch13_17 = require('./ch13_to_17');
const ch18_21 = require('./ch18_to_21');

console.log('Generating complete DJ Mart Technical Documentation HTML...');

const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>DJ Mart - Website / Web App – Comprehensive Code Explanation (Technical Documentation)</title>
  <style>
    ${styles.getStyles()}
  </style>
</head>
<body>

  <!-- Chapter 1: Cover Page -->
  ${ch1_3.getChapter1()}

  <!-- Table of Contents -->
  ${ch1_3.getTableOfContents()}

  <!-- Chapter 2: Project Overview -->
  ${ch1_3.getChapter2()}

  <!-- Chapter 3: Architecture Overview -->
  ${ch1_3.getChapter3()}

  <!-- Chapter 4: Folder & File Structure -->
  ${ch4_5.getChapter4()}

  <!-- Chapter 5: File-by-File Detailed Explanation -->
  ${ch4_5.getChapter5()}

  <!-- Chapter 6: Functions & Methods Deep-Dive -->
  ${ch6_8.getChapter6()}

  <!-- Chapter 7: Variables, State, Models & DTOs -->
  ${ch6_8.getChapter7()}

  <!-- Chapter 8: UI Components & Layouts -->
  ${ch6_8.getChapter8()}

  <!-- Chapter 9: Button & User Interaction Flows -->
  ${ch9_12.getChapter9()}

  <!-- Chapter 10: RESTful API Documentation -->
  ${ch9_12.getChapter10()}

  <!-- Chapter 11: Database Schema & CRUD Analysis -->
  ${ch9_12.getChapter11()}

  <!-- Chapter 12: Libraries & Dependencies (pom.xml) -->
  ${ch9_12.getChapter12()}

  <!-- Chapter 13: CSS & Luxury Design System -->
  ${ch13_17.getChapter13()}

  <!-- Chapter 14: Mobile & Responsive Adaptation -->
  ${ch13_17.getChapter14()}

  <!-- Chapter 15: Security Architecture (OWASP) -->
  ${ch13_17.getChapter15()}

  <!-- Chapter 16: Error Handling Architecture -->
  ${ch13_17.getChapter16()}

  <!-- Chapter 17: Code Issues & Future Roadmap -->
  ${ch13_17.getChapter17()}

  <!-- Chapter 18: Where to Make Which Change (Where to Edit) -->
  ${ch18_21.getChapter18()}

  <!-- Chapter 19: End-to-End User Journeys -->
  ${ch18_21.getChapter19()}

  <!-- Chapter 20: Component Connection Architecture -->
  ${ch18_21.getChapter20()}

  <!-- Chapter 21: Beginner Mental Model -->
  ${ch18_21.getChapter21()}

</body>
</html>
`;

const htmlFilePath = path.join(__dirname, '..', 'DJ Mart_Tamil_Documentation.html');
fs.writeFileSync(htmlFilePath, htmlContent, 'utf8');
console.log(`HTML successfully written to: ${htmlFilePath} (${(htmlContent.length / 1024).toFixed(1)} KB)`);

// Find Microsoft Edge executable
const edgePaths = [
  'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
  'C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe'
];

let edgePath = edgePaths.find(p => fs.existsSync(p));

if (!edgePath) {
  console.error('Microsoft Edge executable not found in standard paths.');
  process.exit(1);
}

const targetPdfPath = path.join(__dirname, '..', 'DJ Mart_Complete_Code_Explanation_Tamil.pdf');
const brainArtifactPath = 'C:\\Users\\djnir\\.gemini\\antigravity\\brain\\6bc14fa7-10f4-4a85-94d1-594581264fbb\\Complete_Website_Code_Explanation_Tamil.pdf';

console.log(`Found Edge at: ${edgePath}`);
console.log('Compiling HTML to PDF using Edge headless print...');

const cmd = `"${edgePath}" --headless --disable-gpu --run-all-compositor-stages-before-draw --print-to-pdf="${targetPdfPath}" "${htmlFilePath}"`;
console.log(`Executing: ${cmd}`);

try {
  execSync(cmd, { stdio: 'inherit' });
  console.log(`PDF compiled successfully! Output: ${targetPdfPath}`);

  if (fs.existsSync(targetPdfPath)) {
    const stats = fs.statSync(targetPdfPath);
    console.log(`Target PDF Size: ${(stats.size / 1024).toFixed(1)} KB`);

    // Copy to brain artifact directory as well
    fs.copyFileSync(targetPdfPath, brainArtifactPath);
    console.log(`Copied PDF to Brain Artifact: ${brainArtifactPath}`);
  } else {
    console.error('PDF file was not created!');
    process.exit(1);
  }
} catch (err) {
  console.error('Error generating PDF:', err);
  process.exit(1);
}
