package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.order.service.OrderService;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbPayLog;
import com.pyg.util.IdWorker;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {


    @Reference
    private OrderService orderService;

    @Reference
    private WeixinPayService weixinPayService;

    //生成二维码
    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到 redis 查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        //判断支付日志存在
        if(payLog!=null){
            return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else {
            return new HashMap();
          }

    }


    //查询支付状态
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        int x=0;
        while (true){
            //调用查询接口
            Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
            if (map==null){
                //出错
                result=new Result(false,"支付错误");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")){
                //成功
                result=new Result(true,"支付成功");
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
                break;
            }

            x++;
            if(x>=100){
                result=new Result(false, "二维码超时");
                break;
            }


            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return result;
    }
}
