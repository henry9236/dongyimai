package com.offcn;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.IOException;

@Component
public class SmsMessageLisneterImpl implements MessageListener {
    @Autowired
    private SmsUtil smsUtil;
    @Override
    public void onMessage(Message message) { //手机号和验证码通过MapMessage消息传输
        //判断消息是否是MapMessage
        if(message instanceof MapMessage){
        //转换message to MapMessage
            MapMessage mapMessage = (MapMessage) message;
        //把mobile和param通过MapMessage取出来
            try {
                String mobile = mapMessage.getString("mobile");
                String param = mapMessage.getString("param");
                //使用smUtil的实例发送消息
                HttpResponse httpResponse = smsUtil.sendSms(mobile,param);
                System.out.println("发送短信成功："+ EntityUtils.toString(httpResponse.getEntity()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
