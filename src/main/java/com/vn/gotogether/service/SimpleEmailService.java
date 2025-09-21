package com.vn.gotogether.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import jakarta.mail.internet.MimeMessage;


@Service
@RequiredArgsConstructor
public class SimpleEmailService implements EmailService {
    private final JavaMailSender mailSender;


    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(msg);
        } catch (MailException | jakarta.mail.MessagingException ex) {
// Log and swallow or rethrow depending on requirement
            throw new RuntimeException("Failed to send email", ex);
        }
    }
}