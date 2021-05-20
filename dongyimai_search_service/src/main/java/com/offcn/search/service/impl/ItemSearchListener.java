package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.dongyimai.bean.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        System.out.println("监听接收到消息。。");
        //1,从消息队列中获取消息，通过message变量
        TextMessage textMessage = (TextMessage) message;
        try {
            String itemListJSONString = textMessage.getText();
            //2,把获取到的JSON字符串转成List<TbItem>存入solr，通过importList方法
            List<TbItem> itemList = JSON.parseArray(itemListJSONString,TbItem.class);
            itemSearchService.imporItemtList(itemList);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
