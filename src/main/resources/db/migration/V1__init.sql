CREATE SCHEMA IF NOT EXISTS supplysync;

-- Users Table
CREATE TABLE IF NOT EXISTS supplysync.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(30) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Warehouses Table
CREATE TABLE IF NOT EXISTS supplysync.warehouses (
    id BIGSERIAL PRIMARY KEY,
    warehouse_code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    location TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    capacity INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Categories Table
CREATE TABLE IF NOT EXISTS supplysync.categories (
    id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    parent_category_id BIGINT REFERENCES supplysync.categories(id) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Products Table
CREATE TABLE IF NOT EXISTS supplysync.products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT NULL,
    category_id BIGINT REFERENCES supplysync.categories(id) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    reorder_level INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Inventory Table
CREATE TABLE IF NOT EXISTS supplysync.inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES supplysync.products(id) NOT NULL,
    warehouse_id BIGINT REFERENCES supplysync.warehouses(id) NOT NULL,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    quantity_damaged INTEGER NOT NULL DEFAULT 0,
    last_updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT unique_product_warehouse UNIQUE (product_id, warehouse_id)
);

-- Inventory Transactions Table (No soft delete, immutable)
CREATE TABLE IF NOT EXISTS supplysync.inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES supplysync.products(id) NOT NULL,
    warehouse_id BIGINT REFERENCES supplysync.warehouses(id) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    quantity INTEGER NOT NULL,
    reference_id VARCHAR(100) NULL,
    performed_by BIGINT REFERENCES supplysync.users(id) NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Suppliers Table
CREATE TABLE IF NOT EXISTS supplysync.suppliers (
    id BIGSERIAL PRIMARY KEY,
    supplier_code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(150) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    gstin VARCHAR(20) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Purchase Orders Table
CREATE TABLE IF NOT EXISTS supplysync.purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(30) UNIQUE NOT NULL,
    supplier_id BIGINT REFERENCES supplysync.suppliers(id) NOT NULL,
    warehouse_id BIGINT REFERENCES supplysync.warehouses(id) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    expected_delivery_date DATE NULL,
    actual_delivery_date DATE NULL,
    created_by BIGINT REFERENCES supplysync.users(id) NOT NULL,
    approved_by BIGINT REFERENCES supplysync.users(id) NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Purchase Order Items Table (No soft delete)
CREATE TABLE IF NOT EXISTS supplysync.purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    po_id BIGINT REFERENCES supplysync.purchase_orders(id) NOT NULL,
    product_id BIGINT REFERENCES supplysync.products(id) NOT NULL,
    quantity_ordered INTEGER NOT NULL,
    quantity_received INTEGER NOT NULL DEFAULT 0,
    unit_price NUMERIC(12,2) NOT NULL,
    total_price NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Sales Orders Table
CREATE TABLE IF NOT EXISTS supplysync.sales_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(30) UNIQUE NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    warehouse_id BIGINT REFERENCES supplysync.warehouses(id) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    dispatched_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    created_by BIGINT REFERENCES supplysync.users(id) NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Sales Order Items Table (No soft delete)
CREATE TABLE IF NOT EXISTS supplysync.sales_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES supplysync.sales_orders(id) NOT NULL,
    product_id BIGINT REFERENCES supplysync.products(id) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    total_price NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
