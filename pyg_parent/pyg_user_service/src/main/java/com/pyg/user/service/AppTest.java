package com.pyg.user.service;


import com.alibaba.fastjson.JSON;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

public class AppTest {


    public static void main(String[] args) {

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/applicationContext-jms-producer.xml");
        JmsTemplate jmsTemplate = (JmsTemplate) applicationContext.getBean("jmsTemplate");
        Destination destination = (Destination) applicationContext.getBean("smsDestination");

        //生成 6 位随机数
        String code = (long) (Math.random()*1000000)+"";
        System.out.println("验证码："+code);
        //存入缓存
        //发送到 activeMQ
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile", "");//手机号
                mapMessage.setString("template_code", "SMS_123737132");//模板编号
                mapMessage.setString("sign_name", "品优taotao");//签名
                Map m=new HashMap<>();
                m.put("number", code);
                mapMessage.setString("param", JSON.toJSONString(m));//参数
                return mapMessage;

            }
        });


    }
}
