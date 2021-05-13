package com.offcn.content.service.impl;
import java.util.List;

import com.dongyimai.bean.TbContent;
import com.dongyimai.bean.TbContentExample;
import com.dongyimai.dao.TbContentMapper;
import com.dongyimai.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;


/**
 * 服务实现层
 * @author Administrator
 *
 */

@Service
@Transactional
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清除缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//查询对象修改前的分类id
		Long categorId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		//因为做了修改，使用修改前的分类id，清除redis中的数据
		redisTemplate.boundHashOps("content").delete(categorId);
		//修改数据
		contentMapper.updateByPrimaryKey(content);
		//使用修改前的分类id和修改后的分类id做比较，如果分类id发生了变化,代表修改后的分类id数据发生了变化，清除修改后的分类Id的缓存，
		if(!categorId.equals(content.getCategoryId())){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//清除缓存
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		TbContentExample.Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, Long status) {
		for (Long id : ids){
			TbContent tb = contentMapper.selectByPrimaryKey(id);
			tb.setStatus(String.valueOf(status));
			contentMapper.updateByPrimaryKeySelective(tb);
		}
	}

	/**
	 * 根据广告分类Id查询广告列表
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		List<TbContent> contentList=(List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);//在redis中查找
		//TODO 处理缓存穿透
		if(contentList==null) {
			System.out.println("从数据库读取数据放入缓存");
			//根据广告分类Id查询广告列表
			TbContentExample tbContentExample = new TbContentExample();
			TbContentExample.Criteria tbContentExampleCriteria = tbContentExample.createCriteria();
			tbContentExampleCriteria.andCategoryIdEqualTo(categoryId);
			tbContentExampleCriteria.andStatusEqualTo("1");//开启状态
			tbContentExample.setOrderByClause("sort_order");//排序
			contentList = contentMapper.selectByExample(tbContentExample);

			redisTemplate.boundHashOps("content").put(categoryId,contentList);//存入缓存
		}else{
			System.out.println("从缓存读取数据");
		}
		return contentList;
	}

}
