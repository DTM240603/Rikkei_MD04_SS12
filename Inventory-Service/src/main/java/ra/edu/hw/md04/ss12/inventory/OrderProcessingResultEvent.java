package ra.edu.hw.md04.ss12.inventory;

public record OrderProcessingResultEvent(
        Long orderId,
        String customerEmail,
        Long productId,
        int quantity,
        boolean successful,
        String message) {
}
