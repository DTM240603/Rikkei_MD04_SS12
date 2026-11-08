package ra.edu.hw.md04.ss12.order;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

@Service
public class OrderService {

    private final OrderPersistenceService orderPersistenceService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderService(
            OrderPersistenceService orderPersistenceService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.orderPersistenceService = orderPersistenceService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Order create(OrderRequest request) {
        if (request.customerEmail() == null || request.customerEmail().isBlank()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (request.productId() == null || request.productId() <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Order order = orderPersistenceService.save(request);
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerEmail(),
                order.getProductId(),
                order.getQuantity(),
                order.getCreatedAt());

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order-events", order.getId().toString(), payload)
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KafkaException("Interrupted while publishing order event", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new KafkaException("Could not publish order event", exception);
        }

        return order;
    }
}
