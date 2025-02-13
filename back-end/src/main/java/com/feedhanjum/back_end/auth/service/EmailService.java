package com.feedhanjum.back_end.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {
    private static final String EMAIL_TEMPLATE_NAME = "email";
    private static final String LOGO_IMAGE_NAME = "static/logo.png";
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendCodeToMail(String to, String title, String type, String code, Integer expireMinutes) {
        try {
            log.info("email to: {}, type:{}, code:{}", to, type, code);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(renderEmailTemplate(title, type, code, expireMinutes), true);
            // base64 이미지를 디코딩하여 첨부
            ClassPathResource logoImageResource = new ClassPathResource(LOGO_IMAGE_NAME);
            helper.addInline("logo", logoImageResource, "image/png");

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("메일 전송 오류 발생", e);
        }
    }

    private String renderEmailTemplate(String title, String type, String code, Integer expireMinutes) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("type", type);
        context.setVariable("code", code);
        context.setVariable("expireMinutes", expireMinutes);
        return templateEngine.process(EMAIL_TEMPLATE_NAME, context);
    }
}
