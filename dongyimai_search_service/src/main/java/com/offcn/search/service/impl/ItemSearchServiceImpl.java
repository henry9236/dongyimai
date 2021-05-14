package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.dongyimai.bean.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

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

        //查询列表
        map.putAll(searchList(searchMap));
        return map;
    }

    //根据关键字查询，对查询的结果进行高亮
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //1，创建一个支持高亮的查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
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
        return map;
    }
}
