package com.openobserve.demo.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InventoryControllerTest {

    private final InventoryController controller = new InventoryController();

    @Test
    void reportsAnAvailableSku() throws InterruptedException {
        InventoryResponse response = controller.checkInventory("OO-HOODIE");

        assertThat(response.available()).isTrue();
        assertThat(response.availableQuantity()).isEqualTo(25);
    }

    @Test
    void reportsAnOutOfStockSku() throws InterruptedException {
        InventoryResponse response = controller.checkInventory("OUT-OF-STOCK");

        assertThat(response.available()).isFalse();
        assertThat(response.availableQuantity()).isZero();
    }
}

