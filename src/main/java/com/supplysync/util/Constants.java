package com.supplysync.util;

public final class Constants {
    private Constants() {}

    // Cache names
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_WAREHOUSES = "warehouses";
    public static final String CACHE_LOW_STOCK = "inventory:low-stock";
    public static final String CACHE_SUPPLIERS = "suppliers";
    public static final String CACHE_DASHBOARD = "reports:dashboard";

    // Kafka Topics
    public static final String TOPIC_INVENTORY_UPDATED = "inventory-updated";
    public static final String TOPIC_TRANSFER_INITIATED = "inventory-transfer-initiated";
    public static final String TOPIC_PO_RECEIVED = "purchase-order-received";
    public static final String TOPIC_SO_CREATED = "sales-order-created";
    public static final String TOPIC_SO_CANCELLED = "sales-order-cancelled";
    public static final String TOPIC_LOW_STOCK_ALERT = "low-stock-alert";

    // Error Codes
    public static final String ERR_INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK_FOR_ORDER";
    public static final String ERR_INSUFFICIENT_INVENTORY = "INSUFFICIENT_INVENTORY";
    public static final String ERR_SELF_APPROVAL = "SELF_APPROVAL_NOT_ALLOWED";
    public static final String ERR_PO_CANCELLATION = "PO_CANCELLATION_NOT_ALLOWED";
    public static final String ERR_WAREHOUSE_ACTIVE_INVENTORY = "WAREHOUSE_HAS_ACTIVE_INVENTORY";
    public static final String ERR_TOO_MANY_LOGIN_ATTEMPTS = "TOO_MANY_LOGIN_ATTEMPTS";
}
