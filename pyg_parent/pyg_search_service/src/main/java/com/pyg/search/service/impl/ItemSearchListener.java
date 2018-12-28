package com.pyg.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
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
        TextMessage textMessage=(TextMessage)message;

        try {
            //json字符串
            String text = textMessage.getText();
            System.out.println("监听到消息:"+text+11111);
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            itemSearchService.importList(itemList);
            System.out.println("导入到solr索引库"+22222);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
