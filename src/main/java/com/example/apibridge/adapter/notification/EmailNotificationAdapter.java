package com.example.apibridge.adapter.notification;

import com.example.apibridge.dto.AIResponse;
import com.example.apibridge.dto.ExtractionResponse;
import com.example.apibridge.port.NotificationPort;
import com.example.apibridge.util.MessageFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Email adapter for {@link NotificationPort}.
 * All JavaMail / SMTP details are confined here.
 */
@Component
@Qualifier("email")
public class EmailNotificationAdapter implements NotificationPort {

    private final JavaMailSender mailSender;
    private final Environment env;

    public EmailNotificationAdapter(JavaMailSender mailSender, Environment env) {
        this.mailSender = mailSender;
        this.env = env;
    }

    @Override
    public void sendExtraction(String recipient, ExtractionResponse extraction) {
        send(recipient, "Extraction Result", MessageFormatter.formatExtraction(extraction));
    }

    @Override
    public void sendAIExtraction(String recipient, AIResponse aiResponse) {
        send(recipient, "AI Extraction Result", MessageFormatter.formatAIExtraction(aiResponse));
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(env.getProperty("spring.mail.username", "noreply@example.com"));
        mailSender.send(message);
    }
}
