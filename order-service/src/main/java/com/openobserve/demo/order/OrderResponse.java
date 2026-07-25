package com.openobserve.demo.order;

record OrderResponse(String orderId, String sku, int quantity, String status, String message) {
}

