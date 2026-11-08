package ra.edu.hw.md04.ss12.email;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

@Service
public class OrderEmailConsumer {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    public OrderEmailConsumer(JavaMailSender mailSender, ObjectMapper objectMapper) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "email-service")
    public void handleOrderCreated(String payload) throws Exception {
        OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.customerEmail());
        message.setSubject("Order confirmation #" + event.orderId());
        message.setText("Your order for product " + event.productId()
                + " (quantity " + event.quantity() + ") has been received.");
        mailSender.send(message);
    }
}
