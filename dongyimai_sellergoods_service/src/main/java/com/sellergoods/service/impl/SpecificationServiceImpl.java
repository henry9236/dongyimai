package com.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.dongyimai.bean.TbSpecificationOption;
import com.dongyimai.bean.TbSpecificationOptionExample;
import com.dongyimai.dao.TbSpecificationOptionMapper;
import com.dongyimai.group.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.dongyimai.dao.TbSpecificationMapper;
import com.dongyimai.bean.TbSpecification;
import com.dongyimai.bean.TbSpecificationExample;
import com.dongyimai.bean.TbSpecificationExample.Criteria;
import com.sellergoods.service.SpecificationService;

import com.dongyimai.result.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		specificationMapper.insert(specification.getSpecification());

		//循环插入规格选项
		for(TbSpecificationOption specificationOption : specification.getSpecificationOptionList()){
			//设置规格通过seleckkey标签获取到的id
			specificationOption.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insert(specificationOption);
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//保存修改的规格
		specificationMapper.updateByPrimaryKey(specification.getSpecification());
		//删除原有的规格选项
		TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria tbSpecificationOptionExample_criteria = tbSpecificationOptionExample.createCriteria();
		tbSpecificationOptionExample_criteria.andSpecIdEqualTo(specification.getSpecification().getId());
		specificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
		//将新的规格选项存入
		for (TbSpecificationOption tbSpecificationOption:specification.getSpecificationOptionList()){
			specificationOptionMapper.insert(tbSpecificationOption);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		Specification specificationGroup = new Specification();
		//查询规格并存储到复合实体类
		specificationGroup.setSpecification(specificationMapper.selectByPrimaryKey(id));
		//查询规格内容并存储到复合实体类
		TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria tbSpecificationOptionExample_criteria = tbSpecificationOptionExample.createCriteria();
		tbSpecificationOptionExample_criteria.andSpecIdEqualTo(id);
		specificationGroup.setSpecificationOptionList(specificationOptionMapper.selectByExample(tbSpecificationOptionExample));
		//返回复合实体类
		return specificationGroup;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	/**
	 * 列表数据
	 */
	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}

}
