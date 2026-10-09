-- V3__expand_catalog_and_features.sql
-- DJ Mart: Expand Catalog Schema, Chatbot Persistence, and Enterprise Order Tracking

-- 1. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Enhanced Product Attributes
ALTER TABLE products ADD COLUMN IF NOT EXISTS brand VARCHAR(100);
ALTER TABLE products ADD COLUMN IF NOT EXISTS sku VARCHAR(100);
ALTER TABLE products ADD COLUMN IF NOT EXISTS original_price DECIMAL(10, 2);
ALTER TABLE products ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'INR';
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE products ADD COLUMN IF NOT EXISTS price_verified_at TIMESTAMP;
ALTER TABLE products ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 3. AI Chatbot Conversations Table
CREATE TABLE IF NOT EXISTS chat_conversations (
    id VARCHAR(100) PRIMARY KEY,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_chat_conv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 4. AI Chatbot Messages Table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(100) NOT NULL,
    sender_type VARCHAR(20) NOT NULL CHECK (sender_type IN ('USER', 'BOT', 'SYSTEM')),
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_chat_msg_conv FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE
);

-- 5. Enhanced Order Tracking Attributes
ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_number VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS subtotal DECIMAL(10, 2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_fee DECIMAL(10, 2) DEFAULT 0.00;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) DEFAULT 'PAID';

-- 6. Order Items Snapshot & Line Total
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS product_name_snapshot VARCHAR(255);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS line_total DECIMAL(10, 2);

-- 7. Performance Indexes
CREATE INDEX IF NOT EXISTS idx_chat_msg_conv ON chat_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_chat_conv_user ON chat_conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_orders_number ON orders(order_number);
