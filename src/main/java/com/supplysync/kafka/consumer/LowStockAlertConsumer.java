package com.supplysync.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplysync.entity.Inventory;
import com.supplysync.entity.Product;
import com.supplysync.entity.Warehouse;
import com.supplysync.kafka.producer.InventoryUpdatedEvent;
import com.supplysync.kafka.producer.LowStockAlertEvent;
import com.supplysync.kafka.producer.KafkaEventProducer;
import com.supplysync.repository.InventoryRepository;
import com.supplysync.repository.ProductRepository;
import com.supplysync.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.supplysync.util.Constants;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockAlertConsumer {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = Constants.TOPIC_INVENTORY_UPDATED, groupId = "low-stock-checker")
    @org.springframework.transaction.annotation.Transactional
    public void consumeInventoryUpdated(String message) {
        log.info("Received inventory update event message: {}", message);
        try {
            InventoryUpdatedEvent event = objectMapper.readValue(message, InventoryUpdatedEvent.class);

            Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(event.getProductId(), event.getWarehouseId())
                    .orElse(null);

            if (inventory != null) {
                Product product = inventory.getProduct();
                Warehouse warehouse = inventory.getWarehouse();

                if (inventory.getQuantityAvailable() <= product.getReorderLevel()) {
                    log.warn("LOW STOCK ALERT: Product {} in Warehouse {} has {} units remaining (reorder level: {})",
                            product.getSku(), warehouse.getWarehouseCode(), inventory.getQuantityAvailable(), product.getReorderLevel());

                    int deficit = product.getReorderLevel() - inventory.getQuantityAvailable();
                    LowStockAlertEvent alertEvent = LowStockAlertEvent.builder()
                            .productId(product.getId())
                            .sku(product.getSku())
                            .productName(product.getName())
                            .warehouseId(warehouse.getId())
                            .warehouseName(warehouse.getName())
                            .quantityAvailable(inventory.getQuantityAvailable())
                            .reorderLevel(product.getReorderLevel())
                            .deficit(deficit)
                            .timestamp(LocalDateTime.now().toString())
                            .build();

                    kafkaEventProducer.publishLowStockAlert(alertEvent);
                }
            }
        } catch (Exception e) {
            log.error("Error processing inventory update event in low stock consumer: {}", e.getMessage());
        }
    }
}
