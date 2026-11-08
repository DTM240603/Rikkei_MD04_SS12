package ra.edu.hw.md04.ss12.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    private Long productId;

    @Column(nullable = false)
    private int availableStock;

    protected Inventory() {
    }

    public Inventory(Long productId, int availableStock) {
        this.productId = productId;
        this.availableStock = availableStock;
    }

    public Long getProductId() {
        return productId;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }
}
