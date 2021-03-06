package com.sellergoods.service;
import com.dongyimai.bean.TbSeller;
import com.dongyimai.result.PageResult;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SellerService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeller> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeller seller);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeller seller);
	

	/**
	 * 根据ID获取实体
	 * @return
	 */
	public TbSeller findOne(String sellerId);
	
	
	/**
	 * 批量删除
	 */
	public void delete(String [] sellerIds);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeller seller, int pageNum,int pageSize);

	/**
	 * 更改商家审核状态
	 * @param sellerId
	 * @param status
	 */
	public void updateStatus(String sellerId,String status);

	/**
	 * 通过sellerId查询
	 * @param sellerId
	 * @return
	 */
	public TbSeller findBySellerId(String sellerId);
}
