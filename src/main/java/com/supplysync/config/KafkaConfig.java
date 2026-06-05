package com.supplysync.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic inventoryUpdatedTopic() {
        return TopicBuilder.name("inventory-updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryTransferInitiatedTopic() {
        return TopicBuilder.name("inventory-transfer-initiated")
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic purchaseOrderReceivedTopic() {
        return TopicBuilder.name("purchase-order-received")
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic salesOrderCreatedTopic() {
        return TopicBuilder.name("sales-order-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic salesOrderCancelledTopic() {
        return TopicBuilder.name("sales-order-cancelled")
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic lowStockAlertTopic() {
        return TopicBuilder.name("low-stock-alert")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
