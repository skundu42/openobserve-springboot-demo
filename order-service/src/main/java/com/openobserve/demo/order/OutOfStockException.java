package com.openobserve.demo.order;

class OutOfStockException extends RuntimeException {

    OutOfStockException(String message) {
        super(message);
    }
}

