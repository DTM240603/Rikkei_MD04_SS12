package ra.edu.hw.md04.ss12.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Modifying
    @Query(value = """
            UPDATE inventory
            SET available_stock = available_stock - :quantity
            WHERE product_id = :productId AND available_stock >= :quantity
            """, nativeQuery = true)
    int reserveStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
