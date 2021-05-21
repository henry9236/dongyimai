package com.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.regex.Pattern;

@Component
public class MailUtil {
    @Autowired
    private JavaMailSenderImpl mailSender;

    @Value("${fromMail}")
    private String fromMail;
    @Value("${subjectTitle}")
    private String subjectTitle;

    /**
     * 正则表达式：验证邮箱
     */
    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    public void sendMail(String mail){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,true,"utf-8");
            mimeMessageHelper.setFrom(fromMail);
            mimeMessageHelper.setTo(mail);
            mimeMessageHelper.setSubject(subjectTitle);
            String mailContext = "验证你的邮箱地址";
            mimeMessageHelper.setText(mailContext,true);

            mailSender.send(mimeMessage);
            System.out.println("发送验证邮件到邮箱："+mail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 校验邮箱
     *
     * @param email
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isEmail(String email) {
           return Pattern.matches(REGEX_EMAIL, email);
    }


}
