package com.pyg.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.seckill.service.SeckillOrderService;
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
    private SeckillOrderService seckillOrderService;

    @Reference
    private WeixinPayService weixinPayService;

    //生成二维码
    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到 redis 查询秒杀订单
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        //判断支付日志存在
        if(seckillOrder!=null){
            long fen= (long)(seckillOrder.getMoney().doubleValue()*100);//金额（分）
            return weixinPayService.createNative(seckillOrder.getId()+"",+fen+"");
        }else {
            return new HashMap();
          }

    }


    //查询支付状态
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //获取当前用户
        String userId=SecurityContextHolder.getContext().getAuthentication().getName();
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
                //存入数据库
                seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
                break;
            }

            x++;
            if(x>=100){
                result=new Result(false, "二维码超时");
                //删除订单
                seckillOrderService.deleteOrderFromRedis(userId,Long.valueOf(out_trade_no));
                //关闭支付
                Map<String,String> payresult = weixinPayService.closePay(out_trade_no);
                if( !"SUCCESS".equals(payresult.get("result_code")) ){//如果返回结果是正常关闭
                    if("ORDERPAID".equals(payresult.get("err_code"))){
                        result=new Result(true, "支付成功");
                        seckillOrderService.saveOrderFromRedisToDb(userId,
                                Long.valueOf(out_trade_no), map.get("transaction_id"));
                    }
                }
                if(result.getSuccess()==false){
                    System.out.println("超时，取消订单");
//2.调用删除
                    seckillOrderService.deleteOrderFromRedis(userId,
                            Long.valueOf(out_trade_no));
                }
                break;
            }

                break;
            }


            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



        return result;
    }
}
