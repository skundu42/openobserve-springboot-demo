package com.openobserve.demo.order;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OrderWorkflow {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderWorkflow.class);

    private final RestClient inventoryRestClient;

    OrderWorkflow(RestClient inventoryRestClient) {
        this.inventoryRestClient = inventoryRestClient;
    }
    //Whenever submitOrder() runs, OpenTelemetry creates a span named "validate-and-submit-order"
    @WithSpan("validate-and-submit-order")//creates a trace span
    OrderResponse submitOrder(
            //Attached to the span as attributes
            @SpanAttribute("app.order.id") String orderId,
            @SpanAttribute("app.product.sku") String sku,
            @SpanAttribute("app.order.quantity") int quantity) {

            //MDC adds fields to every log generated inside the block.
        try (var ignoredOrder = MDC.putCloseable("order_id", orderId);
                var ignoredSku = MDC.putCloseable("sku", sku);
                var ignoredQuantity = MDC.putCloseable("quantity", Integer.toString(quantity))) {
            LOGGER.info("Order request received");

            //The inventory request creates another span and the OpenTelemetry agent automatically creates an HTTP client span for this request.
            InventoryResponse inventory = inventoryRestClient.get()
                    .uri("/api/inventory/{sku}", sku)
                    .retrieve()
                    .body(InventoryResponse.class);

            if (inventory == null || !inventory.available() || inventory.availableQuantity() < quantity) {
                LOGGER.warn("Order rejected because inventory is unavailable");
                throw new OutOfStockException("Not enough inventory for SKU " + sku);
            }

            LOGGER.info("Order accepted with status=CONFIRMED");
            return new OrderResponse(orderId, sku, quantity, "CONFIRMED", "Inventory reserved");
        }
    }
}
