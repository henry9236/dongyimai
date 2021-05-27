package com.cart.service;

import com.dongyimai.bean.ItemCart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface ItemCartService {

    /**
     * 添加商品到购物车
     * @param cartList 购物车
     * @param itemId 商品id
     * @param num 商品数量
     * @return
     */
    public List<ItemCart> addGoodsToCarList(List<ItemCart> cartList,Long itemId,Integer num);

    /**
     * 从redis中查询购物车
     * @param username
     * @return
     */
    public List<ItemCart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<ItemCart> cartList);

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<ItemCart> mergeCartList(List<ItemCart> cartList1,List<ItemCart> cartList2);
}
