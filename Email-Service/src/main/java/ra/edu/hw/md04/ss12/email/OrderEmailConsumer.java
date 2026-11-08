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

    @KafkaListener(topics = "order-results", groupId = "email-service")
    public void handleOrderProcessed(String payload) throws Exception {
        OrderProcessingResultEvent event = objectMapper.readValue(payload, OrderProcessingResultEvent.class);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.customerEmail());
        message.setSubject((event.successful() ? "Order confirmation #" : "Order unavailable #") + event.orderId());
        message.setText(event.message() + ". Product " + event.productId()
                + ", quantity " + event.quantity() + ".");
        mailSender.send(message);
    }
}
