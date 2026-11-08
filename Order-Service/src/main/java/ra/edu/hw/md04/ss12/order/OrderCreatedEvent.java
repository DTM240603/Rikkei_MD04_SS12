package ra.edu.hw.md04.ss12.order;

import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        String customerEmail,
        Long productId,
        int quantity,
        Instant createdAt) {
}
