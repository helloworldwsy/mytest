package com.pyg.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.util.HttpClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;


    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.封装参数
        Map param=new HashMap();
        //公众号
        param.put("appid",appid);
        //商户号
        param.put("mch_id",partner);
        //随机字符串
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        //商品描述
        param.put("body","品优购");
        //商户订单号
        param.put("out_trade_no",out_trade_no);
        //总金额（分）
        param.put("total_fee",total_fee);
        //IP
        param.put("spbill_create_ip","127.0.0.1");
        //回调地址(随便写)
        param.put("notify_url","https://www.baidu.com");
        //交易类型
        param.put("trade_type","NATIVE");

        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求参数："+paramXml);
            //2.生成要发送的xml
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(paramXml);
            client.post();
            //3.获得结果
            String xmlResult = client.getContent();
            System.out.println("结果："+xmlResult);
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            Map map=new HashMap();
            map.put("code_url", mapResult.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade_no",out_trade_no);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        //1.封装参数
        Map param=new HashMap();
        param.put("appid", appid);//公众账号 ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //2.发送请求
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获取结果
            String xmlResult = client.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("查询的结果："+xmlParam);
            return mapResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }




    }

    @Override
    public Map closePay(String out_trade_no) {
        Map param=new HashMap();
        param.put("appid", appid);//公众账号 ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url="https://api.mch.weixin.qq.com/pay/closeorder";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client=new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



}
