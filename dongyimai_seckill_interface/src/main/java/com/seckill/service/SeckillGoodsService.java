package com.seckill.service;
import com.dongyimai.bean.TbSeckillGoods;
import com.dongyimai.result.PageResult;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillGoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillGoods seckill_goods);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillGoods seckill_goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillGoods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillGoods seckill_goods, int pageNum,int pageSize);

	/**
	 * 返回当前正在参与秒杀的商品
	 * @return
	 */
	public List<TbSeckillGoods> findList();

	/**
	 * 根据ID获取实体(从缓存中读取)
	 */
	public TbSeckillGoods findOneFromRedis(Long id);

}
