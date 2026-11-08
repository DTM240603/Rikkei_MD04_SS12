package ra.edu.hw.md04.ss12.inventory;

import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        String customerEmail,
        Long productId,
        int quantity,
        Instant createdAt) {
}
