package com.techstore.app.service.interfaces;

public interface NotificationService {

    void sendEmail(String to, String subject, String body);
}
