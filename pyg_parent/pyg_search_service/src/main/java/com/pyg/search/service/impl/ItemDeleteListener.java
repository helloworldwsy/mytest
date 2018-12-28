package com.pyg.search.service.impl;

import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage =(ObjectMessage)message;
        try {
            Long[] goodsIds= (Long[]) objectMessage.getObject();
            System.out.println("监听获取到消息："+goodsIds+111111);
            itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
            System.out.println("执行索引库删除"+222222);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
