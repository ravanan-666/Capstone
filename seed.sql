-- seed.sql (DJ Mart: 24 Curated Products with Real Indian Rupee [INR] Prices, Categories, and Multi-turn Seed Data)
-- Passwords for all accounts are 'Password@123' (hashed using jBCrypt with 10 salt rounds)

-- =============================================================================
-- 1. USERS (Admin, Verified Sellers, Buyers)
-- =============================================================================
MERGE INTO users (id, name, email, password_hash, role, created_at) KEY (id) VALUES
(1, 'System Administrator', 'admin@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'ADMIN', CURRENT_TIMESTAMP),
(2, 'Tech Trends Official', 'seller.tech@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'SELLER', CURRENT_TIMESTAMP),
(3, 'Urban Style Studio', 'seller.style@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'SELLER', CURRENT_TIMESTAMP),
(4, 'John Doe', 'buyer.john@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'BUYER', CURRENT_TIMESTAMP),
(5, 'Sarah Connor', 'buyer.sarah@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'BUYER', CURRENT_TIMESTAMP);

-- =============================================================================
-- 2. CATEGORIES
-- =============================================================================
MERGE INTO categories (id, name, slug, description, created_at) KEY (id) VALUES
(1, 'Electronics', 'electronics', 'High-fidelity audio, mechanical keyboards, monitors, and smart wearables.', CURRENT_TIMESTAMP),
(2, 'Fashion', 'fashion', 'Tailored apparel, classic footwear, and handcrafted leather accessories.', CURRENT_TIMESTAMP),
(3, 'Home & Kitchen', 'home-kitchen', 'Specialty coffee gear, Japanese cutlery, and kitchen appliances.', CURRENT_TIMESTAMP),
(4, 'Books & Stationery', 'books-stationery', 'Archival bullet journals, luxury fountain pens, and writing essentials.', CURRENT_TIMESTAMP),
(5, 'Wellness & Fitness', 'wellness-fitness', 'Ergonomic yoga equipment, insulation flasks, and health trackers.', CURRENT_TIMESTAMP);

-- =============================================================================
-- 3. PRODUCTS (24 Distinct Products with Verified INR Market Prices & Authentic Images)
-- =============================================================================
MERGE INTO products (id, seller_id, name, description, price, stock_qty, category, image_url, brand, sku, original_price, currency, is_active, price_verified_at, created_at) KEY (id) VALUES
(1, 2, 'Sony WH-1000XM5 Wireless Noise-Cancelling Headphones', 'Industry-leading active noise cancellation with Auto NC Optimizer, 30-hour battery life, 8 microphones, and crystal-clear hands-free calling.', 29990.00, 25, 'Electronics', '/static/images/products/prod-1-sony-headphones.jpg', 'Sony', 'SONY-WH1000XM5-BLK', 34990.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 'Keychron K2 V2 Wireless Mechanical Keyboard', '75% compact wireless mechanical keyboard with Gateron G Pro Brown switches, per-key RGB backlighting, Mac & Windows layouts, and 4000mAh battery.', 7499.00, 40, 'Electronics', '/static/images/products/prod-2-keychron-keyboard.jpg', 'Keychron', 'KEYCHRON-K2-RGB', 8999.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'Dell UltraSharp 27" 4K USB-C Hub Monitor (U2723QE)', '27-inch 4K UHD IPS Black monitor with 2000:1 contrast ratio, 98% DCI-P3 color gamut, 90W USB-C power delivery, and RJ45 Ethernet connectivity.', 52499.00, 15, 'Electronics', '/static/images/products/prod-3-dell-monitor.jpg', 'Dell', 'DELL-U2723QE-4K', 64900.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 'Logitech MX Master 3S Wireless Performance Mouse', 'Ergonomic mouse with 8K DPI any-surface tracking, Quiet Clicks, MagSpeed electromagnetic scrolling, and multi-device cross-computer flow.', 9995.00, 55, 'Electronics', '/static/images/products/prod-4-logitech-mouse.jpg', 'Logitech', 'LOGI-MX3S-GRAPH', 11995.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 3, 'Classic Oxford Cotton Shirt', 'Premium 100% combed Egyptian organic cotton shirt in crisp white with button-down collar and regular fit for professional refinement.', 2499.00, 50, 'Fashion', '/static/images/products/prod-5-oxford-shirt.jpg', 'Raymond', 'RAY-OXFORD-WHT', 3299.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 3, 'Vintage Washed Denim Trucker Jacket', 'Original iconic American trucker jacket crafted from 100% durable cotton denim with antique metallic shank buttons and point collar.', 4999.00, 35, 'Fashion', '/static/images/products/prod-6-denim-jacket.jpg', 'Levi''s', 'LEVI-TRUCKER-DNM', 6599.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 3, 'Handcrafted Genuine Leather Crossbody Bag', 'Full-grain vegetable-tanned genuine leather bag with solid brass buckle hardware, padded tablet compartment, and adjustable shoulder strap.', 6895.00, 22, 'Fashion', '/static/images/products/prod-7-leather-crossbody-bag.jpg', 'Hidesign', 'HIDE-LEATHER-XMSG', 8995.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 2, 'Fellow Stagg EKG Electric Pour-Over Kettle', 'Variable temperature control precision electric kettle with counterbalanced gooseneck spout, LCD screen, and 60-minute temperature hold.', 16999.00, 20, 'Home & Kitchen', '/static/images/products/prod-8-fellow-stagg-kettle.jpg', 'Fellow', 'FELLOW-STAGG-EKG', 19999.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 3, 'Shun Classic 8" Japanese Chef Knife', 'Handcrafted in Seki City, Japan. VG-MAX cutting core clad with 32 layers of Damascus steel, razor-sharp 16-degree double-bevel edge, and Pakkawood handle.', 14990.00, 16, 'Home & Kitchen', '/static/images/products/prod-9-shun-chef-knife.jpg', 'Shun', 'SHUN-CLASSIC-8CHEF', 17990.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 3, 'Leuchtturm1917 Hardcover Dotted Journal Set', '160gsm archival bleed-proof paper with vegan leather hardcover, blank table of contents, and precision rollerball pen.', 2199.00, 70, 'Books & Stationery', '/static/images/products/prod-10-leuchtturm-journal.jpg', 'Leuchtturm1917', 'LT-A5-DOT-BLK', 2499.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 2, 'Apple AirPods Pro (2nd Generation, USB-C)', 'H2 chip with up to 2x more Active Noise Cancellation, Adaptive Audio, Transparency mode, Personalized Spatial Audio, and MagSafe USB-C case.', 24900.00, 30, 'Electronics', '/static/images/products/prod-11-apple-airpods-pro.jpg', 'Apple', 'APL-AIRPODS-PRO2', 24900.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 2, 'boAt Airdopes 141 ANC Wireless Earbuds', 'Affordable true wireless earbuds featuring up to 32dB active noise cancellation, ENx technology for crystal calls, and 42 hours total playback.', 1499.00, 85, 'Electronics', '/static/images/products/prod-12-boat-airdopes.jpg', 'boAt', 'BOAT-AIR141-ANC', 4490.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 2, 'Marshall Stanmore III Bluetooth Speaker', 'Legendary classic Marshall design with wider soundstage, dynamic loudness balance, Bluetooth 5.2, and tactile analog control brass knobs.', 34999.00, 18, 'Electronics', '/static/images/products/prod-13-marshall-speaker.jpg', 'Marshall', 'MARSHALL-STAN3-BLK', 39999.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 3, 'Nike Air Force 1 07 Classic Sneakers', 'Timeless hoops classic sneaker featuring crisp leather edges, encapsulated Nike Air cushioning, and durable non-marking rubber outsole.', 8195.00, 28, 'Fashion', '/static/images/products/prod-14-nike-air-force-1.jpg', 'Nike', 'NIKE-AF1-07-WHT', 8195.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 3, 'Fabindia Handblock Printed Chanderi Kurta', 'Artisanal hand-block printed ethnic kurta woven from luxurious Chanderi silk-cotton blend with mandarin collar and side slits.', 3290.00, 40, 'Fashion', '/static/images/products/prod-15-fabindia-kurta.jpg', 'Fabindia', 'FAB-CHAND-KURTA', 4190.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 2, 'Prestige Deluxe Alpha Pressure Cooker 3L', 'Induction compatible heavy gauge stainless steel pressure cooker with unique Alpha base, pressure indicator, and controlled gasket release.', 2150.00, 60, 'Home & Kitchen', '/static/images/products/prod-16-prestige-pressure-cooker.jpg', 'Prestige', 'PREST-ALPHA-3L', 2750.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 2, 'Philips Digital Air Fryer HD9252', 'Digital touch screen air fryer with 7 preset cooking programs, 4.1L capacity, Rapid Air technology for up to 90% less fat, and keep warm function.', 7999.00, 35, 'Home & Kitchen', '/static/images/products/prod-17-philips-air-fryer.jpg', 'Philips', 'PHIL-AF-9252', 11995.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 3, 'Lamy Safari Charcoal Fountain Pen (M)', 'Timeless German design crafted from robust ABS plastic with ergonomic grip section, black chromium-plated steel nib, and flexible metal clip.', 2750.00, 45, 'Books & Stationery', '/static/images/products/prod-18-lamy-safari-pen.jpg', 'Lamy', 'LAMY-SAFARI-CHAR', 3200.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 3, 'Parker Jotter Bond Street Ballpoint Pen', 'The authentic everyday writing icon since 1954. Stainless steel cap with signature arrow clip and Quinkflow ballpoint refill for effortless smooth lines.', 375.00, 120, 'Books & Stationery', '/static/images/products/prod-19-parker-jotter-pen.jpg', 'Parker', 'PARKER-JOT-BLK', 450.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 3, 'Titan Edge Ceramic Slim Analog Watch', 'One of the slimmest ceramic watches in the world at just 4.4mm thickness. High-tech ceramic case and bracelet with sapphire crystal glass.', 24995.00, 12, 'Fashion', '/static/images/products/prod-20-titan-edge-watch.jpg', 'Titan', 'TITAN-EDGE-CERAM', 27995.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 2, 'Noise ColorFit Pro 5 AMOLED Smartwatch', 'Bluetooth calling smartwatch with vibrant 1.85-inch AMOLED display, Tru Sync technology, 100+ sports modes, 24/7 health tracking, and 7-day battery.', 3999.00, 65, 'Electronics', '/static/images/products/prod-21-noise-colorfit-watch.jpg', 'Noise', 'NOISE-CFP5-JET', 8999.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 2, 'Milton Thermosteel Flip Lid Flask 1000ml', 'Double walled vacuum insulated 304 stainless steel flask keeping beverages hot or cold for 24 hours. Leak-proof flip lid with carry pouch.', 999.00, 90, 'Home & Kitchen', '/static/images/products/prod-22-milton-flask.jpg', 'Milton', 'MILTON-TS-1000', 1270.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 3, 'Manduka PRO Yoga & Pilates Mat 6mm', 'Ultra-dense cushioning for unmatched support and joint protection, non-slip textured grip, closed-cell surface preventing sweat absorption.', 11500.00, 25, 'Wellness & Fitness', '/static/images/products/prod-23-manduka-yoga-mat.jpg', 'Manduka', 'MANDUKA-PRO-6MM', 12999.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 2, 'Fitbit Charge 6 Advanced Health Tracker', 'Advanced fitness tracker with Google apps integration, built-in GPS, 40+ exercise modes, ECG app, Daily Readiness Score, and up to 7-day battery life.', 14999.00, 20, 'Wellness & Fitness', '/static/images/products/prod-24-fitbit-charge-6.jpg', 'Fitbit', 'FITBIT-CHG6-OBS', 14999.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 2, 'ASUS ROG Zephyrus G16 (2024) Gaming Laptop', 'Intel Core Ultra 9 185H, NVIDIA GeForce RTX 4070 8GB, 16" 2.5K 240Hz OLED Display, 32GB LPDDR5X RAM, 1TB PCIe 4.0 SSD, Eclipse Gray.', 179990.00, 10, 'Electronics', '/static/images/products/prod-25-asus-rog-laptop.jpg', 'ASUS', 'ASUS-ROG-G16-OLED', 199990.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 2, 'Sony Alpha 7 IV Full-Frame Mirrorless Camera', '33MP Full-Frame Exmor R CMOS Sensor, 4K 60p 10-bit video, 759-point Fast Hybrid AF, with 28-70mm Lens Kit and 5-axis Optical Stabilization.', 242490.00, 8, 'Electronics', '/static/images/products/prod-26-sony-alpha-camera.jpg', 'Sony', 'SONY-A7M4-LENSKIT', 262490.00, 'INR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================================================
-- 4. ORDERS (Sample completed, confirmed, and pending orders)
-- =============================================================================
MERGE INTO orders (id, buyer_id, status, total_amount, shipping_address, order_number, subtotal, shipping_fee, payment_status, created_at) KEY (id) VALUES
(1, 4, 'DELIVERED', 37489.00, '123 Tech Lane, Apt 4B, Indiranagar, Bengaluru, KA 560038', 'DJM-202610-001', 37489.00, 0.00, 'PAID', CURRENT_TIMESTAMP),
(2, 5, 'CONFIRMED', 7498.00, '456 Boulevard West, Suite 12, Bandra West, Mumbai, MH 400050', 'DJM-202610-002', 7498.00, 0.00, 'PAID', CURRENT_TIMESTAMP),
(3, 4, 'PENDING', 7499.00, '123 Tech Lane, Apt 4B, Indiranagar, Bengaluru, KA 560038', 'DJM-202610-003', 7499.00, 0.00, 'PAID', CURRENT_TIMESTAMP);

-- =============================================================================
-- 5. ORDER ITEMS (Linked to orders and products with line totals)
-- =============================================================================
MERGE INTO order_items (id, order_id, product_id, quantity, unit_price, product_name_snapshot, line_total, created_at) KEY (id) VALUES
(1, 1, 1, 1, 29990.00, 'Sony WH-1000XM5 Wireless Noise-Cancelling Headphones', 29990.00, CURRENT_TIMESTAMP),
(2, 1, 2, 1, 7499.00, 'Keychron K2 V2 Wireless Mechanical Keyboard', 7499.00, CURRENT_TIMESTAMP),
(3, 2, 5, 1, 2499.00, 'Classic Oxford Cotton Tailored Shirt', 2499.00, CURRENT_TIMESTAMP),
(4, 2, 6, 1, 4999.00, 'Vintage Washed Denim Trucker Jacket', 4999.00, CURRENT_TIMESTAMP),
(5, 3, 2, 1, 7499.00, 'Keychron K2 V2 Wireless Mechanical Keyboard', 7499.00, CURRENT_TIMESTAMP);

-- =============================================================================
-- 6. CART ITEMS (Active cart contents for demo)
-- =============================================================================
MERGE INTO cart_items (id, user_id, product_id, quantity, created_at) KEY (id) VALUES
(1, 5, 3, 1, CURRENT_TIMESTAMP),
(2, 5, 10, 2, CURRENT_TIMESTAMP);

-- =============================================================================
-- 7. REVIEWS (Ratings and comments on products from verified buyers)
-- =============================================================================
MERGE INTO reviews (id, product_id, user_id, rating, comment, created_at) KEY (id) VALUES
(1, 1, 4, 5, 'Exceptional noise cancellation! Battery life easily lasts throughout my entire work week.', CURRENT_TIMESTAMP),
(2, 2, 4, 5, 'Satisfying mechanical tactile feedback and solid aluminum build quality.', CURRENT_TIMESTAMP),
(3, 5, 5, 5, 'Impeccable stitching and breathable cotton. Fits true to size.', CURRENT_TIMESTAMP),
(4, 11, 4, 5, 'Best ANC and transparency mode on the market. USB-C case is very convenient.', CURRENT_TIMESTAMP),
(5, 8, 5, 5, 'Temperature stability is incredible for manual pour-over coffee.', CURRENT_TIMESTAMP);

-- Reset auto-increment sequences past seed data to prevent primary key collision
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 100;
ALTER TABLE products ALTER COLUMN id RESTART WITH 100;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 100;
ALTER TABLE order_items ALTER COLUMN id RESTART WITH 100;
ALTER TABLE cart_items ALTER COLUMN id RESTART WITH 100;
ALTER TABLE reviews ALTER COLUMN id RESTART WITH 100;
