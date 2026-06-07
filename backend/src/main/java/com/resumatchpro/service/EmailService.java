package com.resumatchpro.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}") private String fromEmail;

    @Async public void sendWelcomeEmail(String to, String name) {
        sendHtml(to, "Welcome to ResuMatch Pro, " + name + "!",
            "<div style='font-family:Arial;max-width:600px;margin:0 auto;'><div style='background:#4F46E5;padding:20px;color:white;text-align:center;'><h1>Welcome!</h1></div><div style='padding:30px;background:#f9fafb;'><h2>Hi "+name+",</h2><p>You're now part of the smartest recruitment platform.</p><p><b>Get started:</b> Upload your resume, browse jobs, and track applications in real-time.</p><a href='http://localhost:8080/login' style='display:inline-block;padding:12px 24px;background:#4F46E5;color:white;text-decoration:none;border-radius:6px;'>Go to Dashboard</a></div></div>");
    }

    @Async public void sendPasswordResetEmail(String to, String name, String token) {
        sendHtml(to, "Reset Your ResuMatch Pro Password",
            "<div style='font-family:Arial;max-width:600px;margin:0 auto;'><div style='background:#4F46E5;padding:20px;color:white;text-align:center;'><h1>Password Reset</h1></div><div style='padding:30px;background:#f9fafb;'><h2>Hi "+name+",</h2><p>Click below to reset. Link expires in 15 min.</p><a href='http://localhost:8080/reset-password?token="+token+"' style='display:inline-block;padding:12px 24px;background:#4F46E5;color:white;text-decoration:none;border-radius:6px;'>Reset Password</a></div></div>");
    }

    @Async public void sendApplicationReceivedEmail(String to, String name, String jobTitle, String company) {
        sendHtml(to, "Application Submitted - " + jobTitle + " at " + company,
            "<div style='font-family:Arial;max-width:600px;margin:0 auto;'><div style='background:#059669;padding:20px;color:white;text-align:center;'><h1>Application Submitted!</h1></div><div style='padding:30px;background:#f9fafb;'><h2>Hi "+name+",</h2><p>You applied for <b>"+jobTitle+"</b> at <b>"+company+"</b>.</p><p>We'll notify you once your ATS score is ready.</p></div></div>");
    }

    @Async public void sendStatusChangeEmail(String to, String name, String jobTitle, String company, String newStatus) {
        sendHtml(to, "Application Update - " + jobTitle + " at " + company,
            "<div style='font-family:Arial;max-width:600px;margin:0 auto;'><div style='background:#4F46E5;padding:20px;color:white;text-align:center;'><h1>Status Update</h1></div><div style='padding:30px;background:#f9fafb;'><h2>Hi "+name+",</h2><p>Your application for <b>"+jobTitle+"</b> at <b>"+company+"</b> is now: <b>"+newStatus+"</b></p></div></div>");
    }

    @Async public void sendInterviewScheduledEmail(String to, String name, String jobTitle, String company, String date, String time, String mode, String link) {
        sendHtml(to, "Interview Scheduled - " + jobTitle,
            "<div style='font-family:Arial;max-width:600px;margin:0 auto;'><div style='background:#7C3AED;padding:20px;color:white;text-align:center;'><h1>Interview Scheduled!</h1></div><div style='padding:30px;background:#f9fafb;'><h2>Hi "+name+",</h2><p>Interview for <b>"+jobTitle+"</b> at <b>"+company+"</b>:</p><ul><li><b>Date:</b> "+date+"</li><li><b>Time:</b> "+time+"</li><li><b>Mode:</b> "+mode+"</li><li><b>Link:</b> "+link+"</li></ul></div></div>");
    }

    private void sendHtml(String to, String subject, String body) {
        if (fromEmail == null || fromEmail.isBlank()) { log.warn("Mail not configured. Skipping email to {}", to); return; }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail); helper.setTo(to); helper.setSubject(subject); helper.setText(body, true);
            mailSender.send(msg);
        } catch (MessagingException e) { log.error("Failed to send email to {}: {}", to, e.getMessage()); }
    }
}
