package com.sellergoods.service;
import com.dongyimai.bean.TbGoods;
import com.dongyimai.bean.TbItem;
import com.dongyimai.group.Goods;
import com.dongyimai.result.PageResult;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(Goods goods);
	
	
	/**
	 * 修改
	 * @param goods
	 */
	public void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);
	
	
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
	public PageResult findPage(TbGoods goods, int pageNum,int pageSize);

	/**
	 * 更新商品审核状态
	 * @param ids
	 * @param status
	 */
	public void updateStatus(Long[] ids,String status);

	//改变商品上架下架状态
	public void marketableStatusChange(long id,long marketableStatus);

	/**
	 *
	 * @param goodsId
	 * @param status
	 * @return
	 */
	public List<TbItem> findTtemListByGoodsIdandStatus(Long[] goodsId,String status);
}
