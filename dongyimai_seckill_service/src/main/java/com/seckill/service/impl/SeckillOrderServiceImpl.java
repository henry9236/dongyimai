package com.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.dongyimai.bean.TbSeckillGoods;
import com.dongyimai.bean.TbSeckillOrder;
import com.dongyimai.bean.TbSeckillOrderExample;
import com.dongyimai.dao.TbSeckillOrderMapper;
import com.dongyimai.result.PageResult;
import com.offcn.utils.IdWorker;
import com.seckill.service.SeckillOrderService;
import com.seckill.service.utils.RedisLock;
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
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private  RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private RedisLock redisLock;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}

	/**
	 * 根据ID获取实体
	 *
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id) {
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			seckillOrderMapper.deleteByPrimaryKey(id);
		}
	}


	@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillOrderExample example = new TbSeckillOrderExample();
		TbSeckillOrderExample.Criteria criteria = example.createCriteria();

		if (seckillOrder != null) {
			if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
			}
			if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
			}
			if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
				criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
			}
			if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
				criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
			}
			if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
			}
			if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
			}
			if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
				criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
			}
		}

		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 提交订单,
	 *
	 * @param seckillId
	 * @param userId
	 */
	@Override
	public void submitOrder(Long seckillId, String userId) {

		String appid = "createOrderLock";
		//获取redis分布式锁
		long ex= 1 * 1000L;
		String value = String.valueOf(System.currentTimeMillis() + ex);
		boolean lock = redisLock.lock(appid,value);

		if(lock) {
			//根据秒杀商品编号获取秒杀商品
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			//判断秒杀商品是否为空
			if (null == seckillGoods) {
				//抛出异常结束秒杀提交
				throw new RuntimeException("秒杀商品不存在");
			}
			if (0 >= seckillGoods.getStockCount()) {
				//抛出异常结束秒杀提交
				throw new RuntimeException("商品已经被抢光");
			}
			//扣减库存(Redis库存)
			seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
			System.out.println("库存还剩="+seckillGoods.getStockCount());
			//更新最新库存到redis
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
			//保存订单到Redis
			TbSeckillOrder seckillOrder = new TbSeckillOrder();
			seckillOrder.setId(idWorker.nextId());
			seckillOrder.setSeckillId(seckillId);
			seckillOrder.setCreateTime(new Date());
			seckillOrder.setMoney(seckillGoods.getCostPrice());
			seckillOrder.setSellerId(seckillGoods.getSellerId());
			seckillOrder.setUserId(userId);
			seckillOrder.setStatus("0"); //订单状态

			redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
			System.out.println("redis保存订单:" + userId );

			//判断当库存正好等于0的时候，把redis存储的秒杀商品信息同步保存到数据库
			if (0 >= seckillGoods.getStockCount()) {
				seckillOrderMapper.deleteByPrimaryKey(seckillId);
				//清理掉redis缓存 秒杀商品
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
			}
			redisLock.unlock(appid,value);
		}
	}

	/**
	 * 根据用户名查询秒杀订单
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	/**
	 * 保存秒杀订单到数据库
	 *
	 * @param userId
	 * @param orderId
	 * @param transactionId
	 */
	@Override
	public void saveOrderFromRedisToDataBase(String userId, Long orderId, String transactionId) {
		System.out.println("保存秒杀订单从Redis到数据库:"+userId);
		//从redis中通过UserId获取秒杀订单
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		//如果Redis中没有该秒杀订单
		if(seckillOrder==null){
			throw new RuntimeException("订单不存在");
		}
		//如果与传递过来的订单号不符
		if(seckillOrder.getId().longValue()!=orderId.longValue()){
			throw new RuntimeException("订单不相符");
		}

		seckillOrder.setTransactionId(transactionId);//交易流水号
		seckillOrder.setPayTime(new Date());//支付时间
		seckillOrder.setStatus("1");//状态

		//保存到数据库
		seckillOrderMapper.insert(seckillOrder);
		//从Redis中删除订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	/**
	 * 从Redis中删除订单
	 *
	 * @param userId
	 * @param orderId
	 */
	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		//删除缓存中的订单
		if(seckillOrder!=null && seckillOrder.getId().longValue()== orderId.longValue() ){
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
		}
		//恢复库存
		//从缓存中提取秒杀商品，将库存加1,存回Redis中
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
		if(null!=seckillGoods){
			seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
		}
	}
}
