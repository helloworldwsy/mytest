package com.pyg.pay.service;

import java.util.Map;

public interface WeixinPayService {
    //生成微信支付二维码
    public Map createNative(String out_trade_no, String total_fee);

    //查询订单状态
    public Map queryPayStatus(String out_trade_no);
    //关闭订单
    public Map closePay(String out_trade_no);

}
