// presentation/capture_all.js
const puppeteer = require('./node_modules/puppeteer-core');
const path = require('path');
const fs = require('fs');

async function capture() {
  const edgePath = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
  const outDir = path.join(__dirname, 'screenshots');
  if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
  }

  console.log('Launching Edge for live DJ Mart screenshots...');
  const browser = await puppeteer.launch({
    executablePath: edgePath,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-gpu']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900, deviceScaleFactor: 1 });

  try {
    // 1. Home Page
    console.log('Capturing Home Page...');
    await page.goto('http://localhost:8081/Dj%20Mart/', { waitUntil: 'networkidle2', timeout: 15000 });
    await new Promise(r => setTimeout(r, 1500));
    await page.screenshot({ path: path.join(outDir, 'slide7_home.png'), fullPage: false });
    console.log('Saved slide7_home.png');

    // 2. Products Browsing
    console.log('Capturing Products Catalog...');
    await page.goto('http://localhost:8081/Dj%20Mart/products', { waitUntil: 'networkidle2', timeout: 15000 });
    await new Promise(r => setTimeout(r, 1500));
    await page.screenshot({ path: path.join(outDir, 'slide8_products.png'), fullPage: false });
    console.log('Saved slide8_products.png');

    // 3. Product Details
    console.log('Capturing Product Details...');
    await page.goto('http://localhost:8081/Dj%20Mart/products/1', { waitUntil: 'networkidle2', timeout: 15000 });
    await new Promise(r => setTimeout(r, 1500));
    await page.screenshot({ path: path.join(outDir, 'slide8_product_details.png'), fullPage: false });
    console.log('Saved slide8_product_details.png');

    // 4. Authenticate for Cart & Checkout
    console.log('Authenticating as Buyer...');
    await page.goto('http://localhost:8081/Dj%20Mart/auth/login', { waitUntil: 'networkidle2', timeout: 15000 });
    await page.type('#email', 'buyer.john@DJ Mart.com');
    await page.type('#password', 'Password@123');
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 15000 }),
      page.click('button[type="submit"]')
    ]);
    console.log('Logged in successfully. Current URL:', page.url());

    // 5. Add a product to cart via API to ensure cart has items
    await page.evaluate(async () => {
      try {
        const meta = document.querySelector('meta[name="csrf-token"]');
        const token = meta ? meta.getAttribute('content') : '';
        await fetch('/Dj%20Mart/api/v1/cart/items', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': token },
          body: JSON.stringify({ productId: 1, quantity: 1 })
        });
      } catch (e) {
        console.error(e);
      }
    });
    await new Promise(r => setTimeout(r, 1000));

    // 6. Shopping Cart Page
    console.log('Capturing Cart Page...');
    await page.goto('http://localhost:8081/Dj%20Mart/cart', { waitUntil: 'networkidle2', timeout: 15000 });
    await new Promise(r => setTimeout(r, 1500));
    await page.screenshot({ path: path.join(outDir, 'slide9_cart.png'), fullPage: false });
    console.log('Saved slide9_cart.png');

    // 7. Checkout Page
    console.log('Capturing Checkout Page...');
    await page.goto('http://localhost:8081/Dj%20Mart/checkout', { waitUntil: 'networkidle2', timeout: 15000 });
    await new Promise(r => setTimeout(r, 1500));
    await page.screenshot({ path: path.join(outDir, 'slide10_checkout.png'), fullPage: false });
    console.log('Saved slide10_checkout.png');

    // 8. Responsive Views (Home Page)
    console.log('Capturing Responsive Desktop (1440x900)...');
    await page.setViewport({ width: 1440, height: 900 });
    await page.goto('http://localhost:8081/Dj%20Mart/', { waitUntil: 'networkidle2', timeout: 15000 });
    await new Promise(r => setTimeout(r, 1000));
    await page.screenshot({ path: path.join(outDir, 'slide12_desktop.png'), fullPage: false });

    console.log('Capturing Responsive Tablet (768x1024)...');
    await page.setViewport({ width: 768, height: 1024 });
    await page.goto('http://localhost:8081/Dj%20Mart/', { waitUntil: 'networkidle2', timeout: 15000 });
    await new Promise(r => setTimeout(r, 1000));
    await page.screenshot({ path: path.join(outDir, 'slide12_tablet.png'), fullPage: false });

    console.log('Capturing Responsive Mobile (390x844)...');
    await page.setViewport({ width: 390, height: 844 });
    await page.goto('http://localhost:8081/Dj%20Mart/', { waitUntil: 'networkidle2', timeout: 15000 });
    await new Promise(r => setTimeout(r, 1000));
    await page.screenshot({ path: path.join(outDir, 'slide12_mobile.png'), fullPage: false });

    console.log('All live screenshots captured successfully!');
  } catch (err) {
    console.error('Error during screenshot capture:', err);
  } finally {
    await browser.close();
  }
}

capture();
