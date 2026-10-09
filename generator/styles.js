// generator/styles.js
// Common styling and HTML utilities for the DJ Mart Technical Documentation

module.exports = {
  getStyles: () => `
    @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,600;0,700;1,400&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap');

    @page {
      size: A4;
      margin: 18mm 15mm 20mm 15mm;
      @bottom-right {
        content: counter(page);
        font-family: 'Plus Jakarta Sans', sans-serif;
        font-size: 9pt;
        color: #888;
      }
      @bottom-left {
        content: "DJ Mart Luxury Marketplace — Technical Code Documentation";
        font-family: 'Plus Jakarta Sans', sans-serif;
        font-size: 8.5pt;
        color: #888;
      }
    }

    * {
      box-sizing: border-box;
      -webkit-print-color-adjust: exact !important;
      print-color-adjust: exact !important;
    }

    body {
      font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
      color: #242424;
      background-color: #FFFFFF;
      line-height: 1.65;
      font-size: 10.5pt;
      margin: 0;
      padding: 0;
    }

    /* Typography */
    h1, h2, h3, h4, h5, h6 {
      color: #111111;
      font-weight: 700;
      line-height: 1.3;
      margin-top: 1.4em;
      margin-bottom: 0.5em;
    }

    h1 {
      font-size: 20pt;
      border-bottom: 2px solid #C5A880;
      padding-bottom: 6px;
      color: #0F172A;
    }

    h2 {
      font-size: 15pt;
      color: #1E293B;
      border-left: 4px solid #9A7B4F;
      padding-left: 10px;
      margin-top: 1.8em;
    }

    h3 {
      font-size: 12.5pt;
      color: #334155;
      margin-top: 1.3em;
    }

    h4 {
      font-size: 11pt;
      color: #475569;
    }

    p {
      margin-top: 0;
      margin-bottom: 0.9em;
      text-align: justify;
    }

    /* Page Breaks */
    .page-break {
      page-break-before: always;
      break-before: page;
    }

    .no-break {
      page-break-inside: avoid;
      break-inside: avoid;
    }

    /* Tables */
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 14px 0 20px 0;
      font-size: 9.5pt;
      page-break-inside: avoid;
    }

    th, td {
      border: 1px solid #CBD5E1;
      padding: 8px 11px;
      text-align: left;
      vertical-align: top;
    }

    th {
      background-color: #0F172A;
      color: #F8FAFC;
      font-weight: 600;
      font-size: 9.5pt;
      border-color: #0F172A;
    }

    tr:nth-child(even) {
      background-color: #F8FAFC;
    }

    /* Code Blocks */
    pre {
      background-color: #0F172A;
      color: #E2E8F0;
      padding: 12px 14px;
      border-radius: 6px;
      font-family: 'Consolas', 'Courier New', monospace;
      font-size: 8.8pt;
      line-height: 1.45;
      overflow-x: auto;
      white-space: pre-wrap;
      word-break: break-all;
      margin: 10px 0 16px 0;
      border-left: 4px solid #C5A880;
      page-break-inside: avoid;
    }

    code {
      font-family: 'Consolas', 'Courier New', monospace;
      font-size: 9pt;
      background-color: #F1F5F9;
      color: #0F172A;
      padding: 2px 5px;
      border-radius: 4px;
      border: 1px solid #E2E8F0;
    }

    pre code {
      background-color: transparent;
      color: inherit;
      padding: 0;
      border: none;
      font-size: inherit;
    }

    /* Callout Boxes */
    .callout {
      padding: 12px 15px;
      border-radius: 6px;
      margin: 14px 0;
      font-size: 9.8pt;
      page-break-inside: avoid;
    }

    .callout-info {
      background-color: #F0F9FF;
      border-left: 4px solid #0284C7;
      color: #0C4A6E;
    }

    .callout-success {
      background-color: #F0FDF4;
      border-left: 4px solid #16A34A;
      color: #14532D;
    }

    .callout-warning {
      background-color: #FFFBEB;
      border-left: 4px solid #D97706;
      color: #78350F;
    }

    .callout-danger {
      background-color: #FEF2F2;
      border-left: 4px solid #DC2626;
      color: #7F1D1D;
    }

    .callout-title {
      font-weight: 700;
      margin-bottom: 4px;
      display: flex;
      align-items: center;
      gap: 6px;
    }

    /* Badges */
    .badge {
      display: inline-block;
      padding: 2px 7px;
      border-radius: 4px;
      font-size: 8pt;
      font-weight: 600;
      text-transform: uppercase;
    }
    .badge-get { background-color: #E0F2FE; color: #0369A1; }
    .badge-post { background-color: #DCFCE7; color: #15803D; }
    .badge-put { background-color: #FEF3C7; color: #B45309; }
    .badge-delete { background-color: #FEE2E2; color: #B91C1C; }
    .badge-role { background-color: #F1F5F9; color: #475569; border: 1px solid #CBD5E1; }

    /* Diagrams & Flowcharts */
    .flowchart-container {
      margin: 16px 0;
      padding: 16px;
      background: #F8FAFC;
      border: 1px solid #E2E8F0;
      border-radius: 8px;
      page-break-inside: avoid;
      text-align: center;
    }

    .flow-node {
      display: inline-block;
      background: #FFFFFF;
      border: 1.5px solid #0F172A;
      border-radius: 6px;
      padding: 8px 14px;
      font-weight: 600;
      font-size: 9.5pt;
      color: #0F172A;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
      margin: 4px;
    }

    .flow-arrow {
      display: inline-block;
      color: #9A7B4F;
      font-weight: bold;
      font-size: 14pt;
      margin: 0 6px;
      vertical-align: middle;
    }

    .flow-down-arrow {
      display: block;
      color: #9A7B4F;
      font-weight: bold;
      font-size: 13pt;
      margin: 4px 0;
      text-align: center;
    }

    /* Cover Page Styles */
    .cover-container {
      height: 96vh;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      border: 3px double #C5A880;
      padding: 40px;
      background: linear-gradient(180deg, #FFFFFF 0%, #FAFAF8 100%);
      box-sizing: border-box;
      text-align: center;
      position: relative;
    }

    .cover-header {
      margin-top: 20px;
    }

    .cover-logo-badge {
      display: inline-block;
      border: 2px solid #C5A880;
      padding: 10px 24px;
      letter-spacing: 3px;
      font-weight: 700;
      font-size: 18pt;
      color: #0F172A;
      text-transform: uppercase;
      background: #FFFFFF;
      margin-bottom: 20px;
    }

    .cover-title-box {
      margin: 40px 0;
    }

    .cover-main-title {
      font-size: 26pt;
      font-weight: 800;
      color: #0F172A;
      line-height: 1.3;
      margin-bottom: 15px;
    }

    .cover-sub-title {
      font-size: 16pt;
      color: #9A7B4F;
      font-weight: 600;
      margin-bottom: 25px;
    }

    .cover-description {
      font-size: 11pt;
      color: #475569;
      max-width: 600px;
      margin: 0 auto;
      line-height: 1.7;
    }

    .cover-footer {
      border-top: 1px solid #E2E8F0;
      padding-top: 20px;
      font-size: 9.5pt;
      color: #64748B;
      display: flex;
      justify-content: space-between;
    }

    /* Table of Contents */
    .toc-item {
      display: flex;
      justify-content: space-between;
      border-bottom: 1px dotted #CBD5E1;
      padding: 7px 0;
      font-size: 10pt;
    }
    .toc-title {
      font-weight: 600;
      color: #1E293B;
    }
    .toc-chap-num {
      color: #9A7B4F;
      margin-right: 8px;
      font-weight: 700;
    }
  `
};
