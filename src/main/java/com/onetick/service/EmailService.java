package com.onetick.service;

public interface EmailService {
    void send(String to, String subject, String body);
}
