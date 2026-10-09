-- Active: 1791468148037@@@443@PUBLIC
-- V2__add_performance_indexes.sql
-- DJ Mart: Search, Category Filtering, Price Sorting, and Status Workflow Indexes

-- Product Search & Filter Indexes
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);

-- Order Status Workflow & History Indexes
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);

-- Product Rating Aggregation Index
CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(rating);
