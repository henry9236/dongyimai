package com.sellergoods.service.impl;
import java.util.List;

import com.dongyimai.bean.TbBrand;
import com.dongyimai.bean.TbItemCat;
import com.dongyimai.bean.TbItemCatExample;
import com.dongyimai.dao.TbItemCatMapper;
import com.dongyimai.result.PageResult;
import com.sellergoods.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

/**
 * 商品类目服务实现层
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			itemCatMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		TbItemCatExample.Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据上级ID查询列表
	 * @param parentId
	 * @return
	 */
	@Override
	public PageResult findByParentId(int pageNum,int pageSize,long parentId) {
		PageHelper.startPage(pageNum,pageSize);
		//根据parentId查询列表
		TbItemCatExample tbItemCatExample=new TbItemCatExample();
		TbItemCatExample.Criteria tbItemCatExample_criteria = tbItemCatExample.createCriteria();
		tbItemCatExample_criteria.andParentIdEqualTo(parentId);
		Page<TbItemCat> page = (Page<TbItemCat>)itemCatMapper.selectByExample(tbItemCatExample);
		return new PageResult(page.getTotal(),page.getResult());
	}

	@Override
	public List<TbItemCat> findByParentId(long parentId) {
		TbItemCatExample tbItemCatExample=new TbItemCatExample();
		TbItemCatExample.Criteria tbItemCatExample_criteria = tbItemCatExample.createCriteria();
		tbItemCatExample_criteria.andParentIdEqualTo(parentId);
		List<TbItemCat> itemCatList = itemCatMapper.selectByExample(tbItemCatExample);
		return itemCatList;
	}
}
