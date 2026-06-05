package com.supplysync.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplysync.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private void sendEvent(String topic, Object event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            log.info("Publishing event to topic {}: {}", topic, jsonMessage);
            kafkaTemplate.send(topic, jsonMessage);
        } catch (Exception e) {
            log.error("Failed to publish event to topic {}: {}", topic, e.getMessage());
        }
    }

    public void publishInventoryUpdated(InventoryUpdatedEvent event) {
        sendEvent(Constants.TOPIC_INVENTORY_UPDATED, event);
    }

    public void publishInventoryTransferInitiated(InventoryTransferEvent event) {
        sendEvent(Constants.TOPIC_TRANSFER_INITIATED, event);
    }

    public void publishPurchaseOrderReceived(PurchaseOrderReceivedEvent event) {
        sendEvent(Constants.TOPIC_PO_RECEIVED, event);
    }

    public void publishSalesOrderCreated(SalesOrderCreatedEvent event) {
        sendEvent(Constants.TOPIC_SO_CREATED, event);
    }

    public void publishSalesOrderCancelled(SalesOrderCancelledEvent event) {
        sendEvent(Constants.TOPIC_SO_CANCELLED, event);
    }

    public void publishLowStockAlert(LowStockAlertEvent event) {
        sendEvent(Constants.TOPIC_LOW_STOCK_ALERT, event);
    }
}
