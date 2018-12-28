package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import com.pyg.pojoGroup.Cart;
import com.pyg.util.CookieUtil;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    //购物车列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+username);
        String cartListString = CookieUtil.getCookieValue(request, "cartList","UTF-8");
        if(cartListString==null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if ("anonymousUser".equals(username)){
         //未登录

            System.out.println("在cookie中提取购物车");
            return cartList_cookie;
        }else {
         //以登录
            //从redis 中提取
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            System.out.println("在redis中提取购物车");
          if (cartList_cookie.size()>0){
              //合并购物车
              cartList_redis = cartService.mergeCartList(cartList_cookie, cartList_redis);
              //清除本地 cookie 的数据
              CookieUtil.deleteCookie(request, response, "cartList");
              //将合并后的数据存入 redis
              cartService.saveCartListToRedis(username, cartList_redis);
              System.out.println("执行了合并逻辑");
          }
            return cartList_redis;
        }
        //从cookie中提取购物车

    }


    // 添加商品到购物车
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")//解决跨域问题
    public Result addGoodsToCartList(Long itemId, Integer num){
        //解决跨域问题
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //允许修改cookie(上面的值不能是*)
        //response.setHeader("Access-Control-Allow-Credentials", "true");

        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+username);
        try {
            //获取购物车列表
            List<Cart> cartList = findCartList();
            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            if ("anonymousUser".equals(username)){
                //未登录
                //将新的购物车加入到cookie
                System.out.println("向cookie中存入购物车");
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList),3600*24,"UTF-8");
            }else {
                //已经登录
                //将购物车存入缓存中
                System.out.println("向redis中存入购物车");

                cartService.saveCartListToRedis(username,cartList);
            }

            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }
}
