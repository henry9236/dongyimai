package com.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.dongyimai.bean.TbSpecificationOption;
import com.dongyimai.bean.TbSpecificationOptionExample;
import com.dongyimai.dao.TbSpecificationMapper;
import com.dongyimai.dao.TbSpecificationOptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.dongyimai.dao.TbTypeTemplateMapper;
import com.dongyimai.bean.TbTypeTemplate;
import com.dongyimai.bean.TbTypeTemplateExample;
import com.dongyimai.bean.TbTypeTemplateExample.Criteria;
import com.sellergoods.service.TypeTemplateService;

import com.dongyimai.result.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectList() {
		return typeTemplateMapper.selectList();
	}

	@Override
	public List<Map> findSpecOptionList(Long id) {
		//1，通过id获取typeTemplate对象
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//2，获取typeTemplate对象下的specId，规格数据
			//2.1将规格数据JSON字符串，转换成List<Map>
		List<Map> specificationlist = JSON.parseArray(tbTypeTemplate.getSpecIds(),Map.class);
		//3，通过规格id获取规格选项
		for(Map map:specificationlist){
			//{id:?,text:?,options:[]}
			Long specId = new Long((Integer) map.get("id"));
			TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria tbSpecificationOptionExample_criteria = tbSpecificationOptionExample.createCriteria();
			tbSpecificationOptionExample_criteria.andSpecIdEqualTo(specId);
			List<TbSpecificationOption>specificationOptionList = specificationOptionMapper.selectByExample(tbSpecificationOptionExample);
			map.put("options",specificationOptionList);
		}
		//4，将规格选项数据和规格放在一起返回
		return specificationlist;
	}


}
