package com.order.service.impl;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.dongyimai.bean.ItemCart;
import com.dongyimai.bean.TbOrder;
import com.dongyimai.bean.TbOrderExample;
import com.dongyimai.bean.TbOrderItem;
import com.dongyimai.dao.TbOrderItemMapper;
import com.dongyimai.dao.TbOrderMapper;
import com.dongyimai.result.PageResult;
import com.offcn.utils.IdWorker;
import com.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private RedisTemplate<String,Object> redisTemplate;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private IdWorker idWorker;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//得到购物车数据,从redis中通过登录用户名获取购物车ItemCartList
		List<ItemCart> cartList = (List<ItemCart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		//每个商家生成一个order
		for(ItemCart cart : cartList){
			//使用ID生成器获取随机数
			long orderId = idWorker.nextId();
			System.out.println("sellerId:" + cart.getSellerId());
			// 新创建订单对象
			TbOrder tbOrder = new TbOrder();
			//设置订单ID，使用ID生成器生成的数据
			tbOrder.setOrderId(orderId);
			//设置用户名 在cartController中通过SpringSecurity获取的用户名
			tbOrder.setUserId(order.getUserId());
			//设置支付类型
			tbOrder.setPaymentType(order.getPaymentType());
			//设置状态：未付款
			tbOrder.setStatus("1");
			//设置订单创建日期
			tbOrder.setCreateTime(new Date());
			tbOrder.setUpdateTime(new Date());// 设置订单更新日期
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());// 设置地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());// 设置手机号
			tbOrder.setReceiver(order.getReceiver());// 设置收货人
			tbOrder.setSourceType(order.getSourceType());// 设置订单来源
			tbOrder.setSellerId(cart.getSellerId());// 设置商家ID
			//循环购物车明细
			double money = 0;
			//每个item生成一个Orderitem
			for(TbOrderItem orderItem : cart.getOrderItemList()){
				//设置list中orderItem的值
				orderItem .setId(idWorker.nextId());
				orderItem.setOrderId(orderId);//订单ID
				orderItem.setSellerId(cart.getSellerId());
				money += orderItem.getTotalFee().doubleValue();// 设置金额累加（总付款金额）
				//将商品订单保存到数据库中
				orderItemMapper.insert(orderItem);
			}
			//设置金额总数
			tbOrder.setPayment(new BigDecimal(money));
			//将订单（商家订单）保存到数据中
			orderMapper.insert(tbOrder);
		}
		//从redis中移除当前用户的购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		TbOrderExample.Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


}
