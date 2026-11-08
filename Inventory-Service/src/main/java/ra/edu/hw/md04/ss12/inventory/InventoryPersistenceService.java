package ra.edu.hw.md04.ss12.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryPersistenceService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryPersistenceService(
            InventoryRepository inventoryRepository,
            InventoryReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Inventory setStock(Long productId, int availableStock) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseGet(() -> new Inventory(productId, availableStock));
        inventory.setAvailableStock(availableStock);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public boolean reserveStock(Long orderId, Long productId, int quantity) {
        return reservationRepository.findById(orderId)
                .map(InventoryReservation::isReserved)
                .orElseGet(() -> reserveAndRecord(orderId, productId, quantity));
    }

    private boolean reserveAndRecord(Long orderId, Long productId, int quantity) {
        boolean reserved = inventoryRepository.reserveStock(productId, quantity) > 0;
        reservationRepository.save(new InventoryReservation(orderId, productId, quantity, reserved));
        return reserved;
    }
}
