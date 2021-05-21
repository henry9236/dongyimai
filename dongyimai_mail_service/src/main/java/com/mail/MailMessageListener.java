package com.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class MailMessageListener implements MessageListener {

    @Autowired
    private MailUtil mailUtil;

    @Override
    public void onMessage(Message message) {
        if(message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                if(mailUtil.isEmail(textMessage.getText())){
                    mailUtil.sendMail(textMessage.getText());
                }else {
                    System.out.println("邮箱格式不正确");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("接收的消息的格式不是TextMessage");
        }
    }
}
