package ra.edu.hw.md04.ss12.order;

public record OrderRequest(String customerEmail, Long productId, int quantity) {
}
