package com.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.cart.service.ItemCartService;
import com.dongyimai.bean.ItemCart;
import com.dongyimai.bean.TbItem;
import com.dongyimai.bean.TbOrderItem;
import com.dongyimai.dao.TbItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemCartServiceImpl implements ItemCartService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车,Cookie 为未登录状态，没有数据库保存操作
     *
     * @param itemCartList 购物车
     * @param itemId   商品id
     * @param num      商品数量
     * @return
     */
    @Override
    public List<ItemCart> addGoodsToCarList(List<ItemCart> itemCartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
            //1.1 查看商品是否存在，状态是否合法
            if(null==item || !"1".equals(item.getStatus())){
                throw new RuntimeException("商品状态出错");
            }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        ItemCart cart = searchCartBySellerId(itemCartList,sellerId);
        //4.如果购物车列表中不存在该商家的购物车,新建购物车对象
        if(null==cart){
            //新建购物车对象添加到购物车列表
            cart = new ItemCart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            //item 变成 orderItem 通过createOrderItem方法（商品，数量）
            TbOrderItem orderItem = createOrderItem(item,num);
            List orderItemList=new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //将购物车对象添加到购物车列表
            itemCartList.add(cart);
        }else{
        //5.如果购物车列表中存在该商家的购物车,直接对返回的cart进行操作
            //查询ItemCart 的 OrderItem 中是否存在 Item 商品
            TbOrderItem orderItemForCurrentItem = searchItemInOrderItemList(cart.getOrderItemList(),itemId);
            //判断orderItemForCurrentItem
            if(null==orderItemForCurrentItem){
                //如果没有，新增OrderItem
                TbOrderItem orderItem = createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else {
                //如果有，在OrderItem上添加数量
                orderItemForCurrentItem.setNum(orderItemForCurrentItem.getNum()+num);
                //修改数量后的数量值必须，大于0，如果小于等于0 则把当前OrderItem从 cart的OrderItemList中删除
                if(orderItemForCurrentItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItemForCurrentItem);
                }else {
                    orderItemForCurrentItem.setTotalFee(new BigDecimal(orderItemForCurrentItem.getNum()*item.getPrice().doubleValue()));
                }
                //如果ItemCart中没有OrderItem则把当前的ItemCart从 购物车中删除CartList
                if(cart.getOrderItemList().size()==0){
                    itemCartList.remove(cart);
                }

            }
        }
        return itemCartList;
    }

    /**
     * 从redis中查询购物车
     *
     * @param username
     * @return
     */
    @Override
    public List<ItemCart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....."+username);
        List<ItemCart> itemcartList = (List<ItemCart>) redisTemplate.boundHashOps("cartList").get(username);
        if(itemcartList==null){
            return new ArrayList();
        }
        return itemcartList;
    }

    /**
     * 将购物车保存到redis
     *
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<ItemCart> cartList) {
        System.out.println("向redis存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    /**
     * 合并购物车
     *
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<ItemCart> mergeCartList(List<ItemCart> cartList1, List<ItemCart> cartList2) {
        System.out.println("合并购物车");
        for (ItemCart itemCart : cartList1){
            for (TbOrderItem orderItem: itemCart.getOrderItemList()){
                cartList2 = addGoodsToCarList(cartList2,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList2;
    }

    /**
     * 根据商家id 查看cartList中是否有该商家的购物车
     * @param cartList
     * @param sellerId
     * @return
     */
    private ItemCart searchCartBySellerId(List<ItemCart> cartList, String sellerId) {
        for (ItemCart cart : cartList){
            if(sellerId.equals(cart.getSellerId())){
                return cart;
            }
        }
        return null;
    }

    /**
     * 把Item放到OrderItem订单列表中返回 OrderItem
     * @param item
     * @return
     */
    public TbOrderItem createOrderItem(TbItem item,Integer num){
        if(num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        //使用item对orderItem进行设置赋值
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
    //在OrderItemList订单列表中根据Item搜索 返回搜索到的OrderItem
    public TbOrderItem searchItemInOrderItemList(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem : orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

}

