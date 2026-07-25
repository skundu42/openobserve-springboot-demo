package com.openobserve.demo.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OrderWorkflowTest {

    @Test
    void confirmsAnOrderWhenInventoryIsAvailable() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://inventory.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("http://inventory.test/api/inventory/OO-HOODIE"))
                .andRespond(withSuccess(
                        """
                        {"sku":"OO-HOODIE","available":true,"availableQuantity":25}
                        """,
                        MediaType.APPLICATION_JSON));

        OrderResponse response = new OrderWorkflow(builder.build())
                .submitOrder("order-1", "OO-HOODIE", 2);

        assertThat(response.status()).isEqualTo("CONFIRMED");
        server.verify();
    }

    @Test
    void rejectsAnOrderWhenInventoryIsUnavailable() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://inventory.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("http://inventory.test/api/inventory/OUT-OF-STOCK"))
                .andRespond(withSuccess(
                        """
                        {"sku":"OUT-OF-STOCK","available":false,"availableQuantity":0}
                        """,
                        MediaType.APPLICATION_JSON));

        OrderWorkflow workflow = new OrderWorkflow(builder.build());

        assertThatThrownBy(() -> workflow.submitOrder("order-2", "OUT-OF-STOCK", 1))
                .isInstanceOf(OutOfStockException.class);
        server.verify();
    }
}

