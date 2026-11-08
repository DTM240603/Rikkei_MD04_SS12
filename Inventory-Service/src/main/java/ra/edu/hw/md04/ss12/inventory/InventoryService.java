package ra.edu.hw.md04.ss12.inventory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ObjectMapper objectMapper;

    public InventoryService(InventoryRepository inventoryRepository, ObjectMapper objectMapper) {
        this.inventoryRepository = inventoryRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Inventory setStock(Long productId, int availableStock) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (availableStock < 0) {
            throw new IllegalArgumentException("Available stock cannot be negative");
        }
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseGet(() -> new Inventory(productId, availableStock));
        inventory.setAvailableStock(availableStock);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public void reserveStock(Long productId, int quantity) {
        int updatedRows = inventoryRepository.reserveStock(productId, quantity);
        if (updatedRows == 0) {
            throw new IllegalStateException(
                    "Product " + productId + " has no inventory or insufficient stock for quantity " + quantity);
        }
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderCreated(String payload) throws Exception {
        OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        reserveStock(event.productId(), event.quantity());
    }
}
