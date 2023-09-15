package com.alibou.security.emailing;

public interface EmailService {

    // Method
    // To send a simple email
    boolean sendSimpleMail(EmailDetails details);

    // Method
    // To send an email with attachment
    String sendMailWithAttachment(EmailDetails details);
}
