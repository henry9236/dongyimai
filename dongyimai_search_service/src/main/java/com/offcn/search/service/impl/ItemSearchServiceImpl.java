package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dongyimai.bean.TbItem;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String, Object> map = new HashMap();
//        Query query = new SimpleQuery();
//        // is：基于分词后的结果 和 传入的参数匹配
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        //添加查询条件
//        query.addCriteria(criteria);
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//        map.put("rows", page.getContent());
        String keywords = (String) searchMap.get("keywords");
        if(null==keywords){
            return map;
        }
        searchMap.put("keywords",keywords.replace(" ",""));
        //1，按关键字查询（高亮显示），item
        map.putAll(searchList(searchMap));
        //2，根据关键字查询商品分类，item_category
        //对solr搜索的结果根据category做个group by 返回分类列表数据，因为分类与规格无关，所以不用做过滤
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //3,查询品牌和规格列表，根据categoryList
        String categoryName = (String) searchMap.get("category");
        //判断，如果searchMap传进来的category有值
        if(!"".equals(categoryName)){
            //如果有值，通过分类名称，重新读取对象品牌，规格
            map.putAll(searchBrandAndSpecList(categoryName));
        }else{//不然，如果没有传来 category的值
            //判断返回页面的分类列表不为空
            if(categoryList.size()>0){
                //获取分类列表第一个，分类的规格选项值并返回给页面
                map.putAll(searchBrandAndSpecList((String)categoryList.get(0)));
            }
        }

        return map;
    }

    /**
     * 将item的数据导入到solr中
     *
     * @param list
     */
    @Override
    public void imporItemtList(List<TbItem> list) {
        for(TbItem item:list){
            System.out.println(item.getTitle());
            Map<String,String> specMap = JSON.parseObject(item.getSpec(),Map.class);
            Map map = new HashMap();
            for(String key : specMap.keySet()){
                //把规格key转成拼音，和value一起放到map中
                map.put("item_spec_"+Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            item.setSpecMap(map);	//给带动态域注解的字段赋值
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 从solr中删除数据
     *
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query = new SimpleQuery();
        Criteria  criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //根据关键字查询，对查询的结果进行高亮
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //1，创建一个支持高亮的查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //添加筛选
        //添加分类筛选
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //添加品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //添加过滤规格
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
            for(String key : specMap.keySet()){
                Criteria filterCriteria=new Criteria("item_spec_"+Pinyin.toPinyin(key, "").toLowerCase()).is( specMap.get(key) );
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //按价格筛选
        Map<String,Integer> price = (Map<String,Integer>) searchMap.get("price");
        if(!CollectionUtils.isEmpty(price)){
            //如果区间起点不等于0
            if(price.get("low") != 0){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price.get("low"));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            //如果高价限制不等于null
            if(price.get("height") != null){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price.get("height"));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo == null){pageNo=1;}//默认第一页
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize==null){pageSize=20;}//默认20
        query.setOffset((pageNo-1)*pageSize); //从第几条记录查询
        query.setRows(pageSize);
        //排序
        String sortValue = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");
        if(sortValue!=null && !sortValue.isEmpty()){
            if(sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        //2。设定需要高亮处理字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        //3,设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //4,设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //5，关联高亮选项到高亮查询器对象
        query.setHighlightOptions(highlightOptions);
        //6、设定查询条件 根据关键字查询
        //创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联查询条件到查询器对象
        query.addCriteria(criteria);
        //7,发出带高亮数据查询请求
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //8,获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        //9,遍历高亮集合
        for (HighlightEntry<TbItem> highlightEntry : highlightEntryList) {
            //获取基本数据类型
            TbItem tbItem = highlightEntry.getEntity();
            if (highlightEntry.getHighlights().size() > 0 && highlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                //高亮结果集合
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对象的高亮结果，设置到商品标题
                tbItem.setTitle(snipplets.get(0));
            }
        }
        //把带高亮数据集合存放map
        map.put("rows",page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;
    }

    /**
     * 查询分类列表
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap){
        List<String> list = new ArrayList<String>();
        Query query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query,TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for(GroupEntry<TbItem> entry:content){
            //将分组结果的名称封装到返回值中
            list.add(entry.getGroupValue());
        }
        return list;
    }

    /**
     * 查询品牌和规格列表
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        //通过item的模板名字获取模板ID
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //下面通过模板id获取品牌信息，和规格信息(包括规格选项)
        if(typeId != null){
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);//返回值添加品牌列表
            //根据模板ID查询，规格列表，与规格选项
            //{{id:xx,text:xx,options:listxx}}
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);//返回值添加规格信息
        }
        return map;
    }
}
