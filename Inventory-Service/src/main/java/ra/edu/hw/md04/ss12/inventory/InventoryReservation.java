package ra.edu.hw.md04.ss12.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_reservations")
public class InventoryReservation {

    @Id
    private Long orderId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean reserved;

    protected InventoryReservation() {
    }

    public InventoryReservation(Long orderId, Long productId, int quantity, boolean reserved) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.reserved = reserved;
    }

    public boolean isReserved() {
        return reserved;
    }
}
