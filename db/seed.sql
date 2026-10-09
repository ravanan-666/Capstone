-- Active: 1791468148037@@@443@PUBLIC
-- seed.sql
-- DJ Mart: Initial Development & Demo Seed Data
-- Passwords for all accounts are 'Password@123' (hashed using jBCrypt with 10 salt rounds)

-- =============================================================================
-- 1. USERS (Admin, Sellers, Buyers)
-- =============================================================================
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES
(1, 'System Administrator', 'admin@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'ADMIN', CURRENT_TIMESTAMP),
(2, 'Tech Trends Official', 'seller.tech@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'SELLER', CURRENT_TIMESTAMP),
(3, 'Urban Style Studio', 'seller.style@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'SELLER', CURRENT_TIMESTAMP),
(4, 'John Doe', 'buyer.john@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'BUYER', CURRENT_TIMESTAMP),
(5, 'Sarah Connor', 'buyer.sarah@djmart.com', '$2a$10$5rO4MIw/XvxvNgvGxNhGvuQIDwwGaINgjUGvIkWGCDCJ6xgxoDzbO', 'BUYER', CURRENT_TIMESTAMP);

-- =============================================================================
-- 2. PRODUCTS (Multiple categories with realistic pricing and stock)
-- =============================================================================
INSERT INTO products (id, seller_id, name, description, price, stock_qty, category, image_url, created_at) VALUES
(1, 2, 'Wireless Noise-Cancelling Headphones', 'Premium over-ear headphones with active noise cancellation, 30-hour battery life, and high-fidelity sound.', 149.99, 35, 'Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500', CURRENT_TIMESTAMP),
(2, 2, 'Mechanical Gaming Keyboard', 'Custom mechanical switches with per-key RGB backlighting and durable aircraft-grade aluminum chassis.', 89.50, 50, 'Electronics', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500', CURRENT_TIMESTAMP),
(3, 2, 'Ultra HD 4K IPS Monitor 27"', 'Crisp 3840x2160 resolution with HDR400, 99% sRGB color gamut, and ultra-thin bezels.', 299.00, 15, 'Electronics', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500', CURRENT_TIMESTAMP),
(4, 2, 'Ergonomic Wireless Mouse', 'Contoured ergonomic design with hyper-fast scrolling and silent click switches.', 39.99, 80, 'Electronics', 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500', CURRENT_TIMESTAMP),
(5, 3, 'Classic Oxford Cotton Shirt', 'Tailored fit button-down oxford shirt crafted from 100% breathable organic cotton.', 45.00, 60, 'Fashion', 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500', CURRENT_TIMESTAMP),
(6, 3, 'Vintage Washed Denim Jacket', 'Classic American trucker jacket made with premium durable denim and antique brass buttons.', 79.95, 25, 'Fashion', 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500', CURRENT_TIMESTAMP),
(7, 3, 'Handcrafted Leather Crossbody Bag', 'Full-grain vegetable tanned genuine leather bag with brass hardware and adjustable strap.', 120.00, 20, 'Fashion', 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=500', CURRENT_TIMESTAMP),
(8, 2, 'Stainless Steel Electric Pour-Over Kettle', 'Gooseneck spout for precise flow control, variable temperature presets, and keep-warm function.', 54.99, 40, 'Home & Kitchen', 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=500', CURRENT_TIMESTAMP),
(9, 3, 'Ceramic Japanese Chef Santoku Knife', 'Ultra-sharp high-carbon ceramic blade designed for effortless slicing, dicing, and chopping.', 68.50, 30, 'Home & Kitchen', 'https://images.unsplash.com/photo-1593618998160-e34014e67546?w=500', CURRENT_TIMESTAMP),
(10, 3, 'Hardcover Dotted Journal & Pen Set', '160gsm archival bleed-proof paper with vegan leather hardcover and precision rollerball pen.', 22.00, 100, 'Books & Stationery', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500', CURRENT_TIMESTAMP);

-- =============================================================================
-- 3. ORDERS (Sample completed, confirmed, and pending orders)
-- =============================================================================
INSERT INTO orders (id, buyer_id, status, total_amount, shipping_address, created_at) VALUES
(1, 4, 'DELIVERED', 239.49, '123 Tech Lane, Apt 4B, Innovation City, NY 10001', CURRENT_TIMESTAMP),
(2, 5, 'CONFIRMED', 124.95, '456 Boulevard West, Suite 12, Fashion Valley, CA 90210', CURRENT_TIMESTAMP),
(3, 4, 'PENDING', 89.50, '123 Tech Lane, Apt 4B, Innovation City, NY 10001', CURRENT_TIMESTAMP);

-- =============================================================================
-- 4. ORDER ITEMS (Linked to orders and products with unit prices)
-- =============================================================================
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, created_at) VALUES
(1, 1, 1, 1, 149.99, CURRENT_TIMESTAMP),
(2, 1, 2, 1, 89.50, CURRENT_TIMESTAMP),
(3, 2, 5, 1, 45.00, CURRENT_TIMESTAMP),
(4, 2, 6, 1, 79.95, CURRENT_TIMESTAMP),
(5, 3, 2, 1, 89.50, CURRENT_TIMESTAMP);

-- =============================================================================
-- 5. CART ITEMS (Active cart contents for demo)
-- =============================================================================
INSERT INTO cart_items (id, user_id, product_id, quantity, created_at) VALUES
(1, 5, 3, 1, CURRENT_TIMESTAMP),
(2, 5, 10, 2, CURRENT_TIMESTAMP);

-- =============================================================================
-- 6. REVIEWS (Ratings and comments on products from buyers)
-- =============================================================================
INSERT INTO reviews (id, product_id, user_id, rating, comment, created_at) VALUES
(1, 1, 4, 5, 'Exceptional noise cancellation! Battery life easily lasts throughout my entire work week.', CURRENT_TIMESTAMP),
(2, 2, 4, 4, 'Satisfying mechanical tactile feedback and solid aluminum build quality.', CURRENT_TIMESTAMP),
(3, 5, 5, 5, 'Impeccable stitching and breathable cotton. Fits true to size.', CURRENT_TIMESTAMP);

-- Reset auto-increment sequences past seed data to prevent primary key collision
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE products ALTER COLUMN id RESTART WITH 100;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 100;
ALTER TABLE order_items ALTER COLUMN id RESTART WITH 100;
ALTER TABLE cart_items ALTER COLUMN id RESTART WITH 100;
ALTER TABLE reviews ALTER COLUMN id RESTART WITH 100;

