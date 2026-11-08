package ra.edu.hw.md04.ss12.inventory;

import org.springframework.kafka.annotation.KafkaListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class InventoryService {

    private final InventoryPersistenceService inventoryPersistenceService;
    private final RedissonClient redissonClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public InventoryService(
            InventoryPersistenceService inventoryPersistenceService,
            RedissonClient redissonClient,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.inventoryPersistenceService = inventoryPersistenceService;
        this.redissonClient = redissonClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Inventory setStock(Long productId, int availableStock) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (availableStock < 0) {
            throw new IllegalArgumentException("Available stock cannot be negative");
        }
        return withProductLock(productId, () -> inventoryPersistenceService.setStock(productId, availableStock));
    }

    private boolean reserveStock(Long orderId, Long productId, int quantity) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return withProductLock(
                productId,
                () -> inventoryPersistenceService.reserveStock(orderId, productId, quantity));
    }

    private <T> T withProductLock(Long productId, LockedOperation<T> operation) {
        RLock lock = redissonClient.getLock("lock:product:{" + productId + "}");
        boolean acquired = false;
        try {
            acquired = lock.tryLock(10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not acquire product lock");
            }
            return operation.execute();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for product lock", exception);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderCreated(String payload) throws Exception {
        OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        boolean successful = reserveStock(event.orderId(), event.productId(), event.quantity());
        OrderProcessingResultEvent result = new OrderProcessingResultEvent(
                event.orderId(),
                event.customerEmail(),
                event.productId(),
                event.quantity(),
                successful,
                successful ? "Đặt hàng thành công" : "Sản phẩm đã hết hàng");
        String resultPayload = objectMapper.writeValueAsString(result);
        try {
            kafkaTemplate.send("order-results", event.orderId().toString(), resultPayload)
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw exception;
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Could not publish order processing result", exception);
        }
    }

    @FunctionalInterface
    private interface LockedOperation<T> {
        T execute();
    }
}
