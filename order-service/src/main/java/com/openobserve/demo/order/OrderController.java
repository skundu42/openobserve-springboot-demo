package com.openobserve.demo.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderWorkflow orderWorkflow;

    OrderController(OrderWorkflow orderWorkflow) {
        this.orderWorkflow = orderWorkflow;
    }

    @GetMapping("/{orderId}")
    ResponseEntity<OrderResponse> submitOrder(
            @PathVariable String orderId,
            @RequestParam(defaultValue = "OO-HOODIE") String sku,
            @RequestParam(defaultValue = "1") @Min(1) @Max(10) int quantity) {
        try {
            return ResponseEntity.ok(orderWorkflow.submitOrder(orderId, sku, quantity));
        } catch (OutOfStockException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new OrderResponse(orderId, sku, quantity, "REJECTED", exception.getMessage()));
        }
    }
}

