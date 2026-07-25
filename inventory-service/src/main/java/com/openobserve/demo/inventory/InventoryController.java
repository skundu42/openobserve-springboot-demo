package com.openobserve.demo.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryController.class);

    @GetMapping("/{sku}")
    InventoryResponse checkInventory(@PathVariable String sku) throws InterruptedException {
        try (var ignoredSku = MDC.putCloseable("sku", sku)) {
            if ("SLOW-PACK".equalsIgnoreCase(sku)) {
                LOGGER.info("Simulating a slow warehouse lookup");
                Thread.sleep(700);
            }

            int availableQuantity = "OUT-OF-STOCK".equalsIgnoreCase(sku) ? 0 : 25;
            boolean available = availableQuantity > 0;

            LOGGER.info(
                    "Inventory lookup completed: available={}, available_quantity={}",
                    available,
                    availableQuantity);

            return new InventoryResponse(sku, available, availableQuantity);
        }
    }
}

