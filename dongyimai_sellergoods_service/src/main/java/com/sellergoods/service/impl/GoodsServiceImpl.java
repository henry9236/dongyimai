package com.sellergoods.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.dongyimai.bean.*;
import com.dongyimai.dao.*;
import com.dongyimai.group.Goods;
import com.dongyimai.result.PageResult;
import com.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.util.CollectionUtils;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemMapper itemMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//设置为审核状态
		goods.getGoods().setAuditStatus("0");
		//1,保存网页传来的商品信息到goods表
		goodsMapper.insert(goods.getGoods());
		//2，获取返回的主键设置给goodsDesc表
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		//3，保存网页传来的商品信息到goodsDesc表
		goodsDescMapper.insert(goods.getGoodsDesc());
		//4，保存网页传来的sku数据
		saveItem(goods);
	}

	/**
	 * 给每个item对象赋值，再存入数据库
	 * @param goods
	 */
	private void saveItem(Goods goods){
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {

			if (!CollectionUtils.isEmpty(goods.getItemList())) {
				for (TbItem item : goods.getItemList()) {
					String title = goods.getGoods().getGoodsName();   //SPU名称
					Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
					for (String key : specMap.keySet()) {
						title += " " + specMap.get(key);
					}
					item.setTitle(title);                                        //SKU名称
					this.setItemValue(goods, item);//设置item的值
					//执行保存
					itemMapper.insert(item);
				}
			}
		} else {  //不启用规格，设置默认值
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());     //SKU的名称
			item.setPrice(goods.getGoods().getPrice());             //价格
			item.setNum(9999);
			item.setStatus("1");
			item.setIsDefault("1");
			item.setSpec("{}");
			this.setItemValue(goods, item);
			//执行保存
			itemMapper.insert(item);


		}
	}
	private void setItemValue(Goods goods, TbItem item) {
		item.setCategoryid(goods.getGoods().getCategory3Id());     //3级分类
		item.setCreateTime(new Date());                                //创建时间
		item.setUpdateTime(new Date());                                //更新时间
		item.setGoodsId(goods.getGoods().getId());                    //SPU的ID
		item.setSellerId(goods.getGoods().getSellerId());            //商家的ID
		//查询分类信息
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());                        //三级分类名称
		//查询品牌信息
		TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(tbBrand.getName());                            //品牌名称
		//查询商家信息
		TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(tbSeller.getNickName());                            //店铺名称
		//查询商品图片
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (!CollectionUtils.isEmpty(imageList)) {
			String url = (String) imageList.get(0).get("url");
			item.setImage(url);                                        //图片路径
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 */
	@Override
	public void update(Goods goods){
		//设置未申请状态:如果是经过修改的商品，需要重新设置状态
		goods.getGoods().setAuditStatus("0");
		goodsMapper.updateByPrimaryKey(goods.getGoods());//保存商品表
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());//保存商品扩展表
		//删除原有的sku列表数据
		TbItemExample tbItemExample = new TbItemExample();
		TbItemExample.Criteria tbItemCatExample_criteria = tbItemExample.createCriteria();
		tbItemCatExample_criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(tbItemExample);
		//添加新的sku列表数据
		saveItem(goods);//插入商品SKU列表数据
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods=new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		//查询SKU商品列表
		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);//查询条件：商品ID
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;

	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods good = new TbGoods();
			good.setId(id);
			good.setIsDelete("1");
			goodsMapper.updateByPrimaryKeySelective(good);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		TbGoodsExample.Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}	criteria.andIsDeleteIsNull();
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for(Long id:ids){
			//根据商品id获取商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改商品状态
			goods.setAuditStatus(status);
			//更新商品信息到数据库
			goodsMapper.updateByPrimaryKey(goods);
			//修改sku的状态
			TbItemExample example = new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(id);
			List<TbItem> itemList = itemMapper.selectByExample(example);
			//遍历sku集合
			for(TbItem item:itemList){
				//修改状态
				item.setStatus("1");
				itemMapper.updateByPrimaryKey(item);
			}
		}
	}

}
