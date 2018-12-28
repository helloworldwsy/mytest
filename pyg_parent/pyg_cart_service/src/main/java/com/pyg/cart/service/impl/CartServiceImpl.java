package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojoGroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品 SKU ID 查询 SKU 商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态无效");
        }
        //2.获取商家 ID
        String sellerId = item.getSellerId();
        //3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        if(cart==null){
          //4.如果购物车列表中不存在该商家的购物车
            cart=new Cart();
            //商家id
            cart.setSellerId(sellerId);
            //商家名称
            cart.setSellerName(item.getSeller());
            //创建购物车明细列表
            List orderItemList=new ArrayList();
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //4.2 将购物车对象添加到购物车列表
            cartList.add(cart);

        }else {
            //5.如果购物车列表中存在该商家的购物车
            // 判断购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if (orderItem==null){
                //5.1. 如果没有，新增购物车明细
                orderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                //更改金额
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()) );
                //如果数量操作后小于等于 0，则移除
                if(orderItem.getNum()<=0){
                    //移除购物车明细
                    cart.getOrderItemList().remove(orderItem);
                }
                //如果移除后 cart 的明细数量为 0，则将 cart 移除
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }

        }

        return cartList;
    }
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            cartList=new ArrayList();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
           System.out.println("向 redis 存入购物车数据....."+username);
            redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                 cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }


    /**
     * 根据商家 ID 查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId){
        for(Cart cart:cartList){
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
//创建购物车明细对象
    private TbOrderItem createOrderItem(TbItem item,Integer num){

        //创建购物车明细对象
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }
// 根据商品明细 ID 查询
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long itemId ){
        for(TbOrderItem orderItem :orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

}
