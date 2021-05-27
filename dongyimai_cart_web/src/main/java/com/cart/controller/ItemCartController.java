package com.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.cart.service.ItemCartService;
import com.dongyimai.bean.ItemCart;
import com.dongyimai.result.Result;
import com.offcn.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping("/cart")
public class ItemCartController {

    @Reference(timeout = 100000)
    private ItemCartService itemCartService;
    //通过springMVC 注入
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<ItemCart> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

            //从cookie中取出 购物车对象的JSON字符串
            String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            //如果 取出的 购物车对象为空
            if (cartListString == null || cartListString.equals("")) {
                cartListString = "[]";
            }
            List<ItemCart> itemCartList_cookie = JSON.parseArray(cartListString, ItemCart.class);
            //如果未登录，则直接返回cookie
            if("anonymousUser".equals(username)){
                return itemCartList_cookie;
            }else{//登录则，把cookie和redis的数据合并，并把数据存入redis后返回给页面
                List<ItemCart> cartList_redis = itemCartService.findCartListFromRedis(username);
                if(0<itemCartList_cookie.size()){
                    //合并购物车
                    cartList_redis = itemCartService.mergeCartList(itemCartList_cookie,cartList_redis);
                    //清除本地cookie的数据
                    CookieUtil.deleteCookie(request,response,"cartList");
                    //将合并后的数据存入 redis
                    itemCartService.saveCartListToRedis(username,cartList_redis);
                }
                return cartList_redis;
            }
    }
    /**
     * 添加商品到购物车
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户："+username);

        response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials","true");

        try {
            //取 ItemCartList的数据,从redis中或cookie
            List<ItemCart> cartList = findCartList();
            cartList = itemCartService.addGoodsToCarList(cartList, itemId, num);
            if("anonymousUser".equals(username)){
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
                System.out.println("向cookie存入数据");
            }else {
                itemCartService.saveCartListToRedis(username,cartList);
                System.out.println("向redis存入数据");
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

    @RequestMapping("/getLoginUserName")
    public String getLoginUserName(){
        return  SecurityContextHolder.getContext().getAuthentication().getName();
    }
}