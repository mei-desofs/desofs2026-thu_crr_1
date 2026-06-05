package com.techstore.app.service.interfaces;

public interface NotificationService {

    public void sendOrderConfirmationEmail(String to, String subject, String body);
}
