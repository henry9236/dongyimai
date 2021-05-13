package com.offcn.solrutil;

import com.alibaba.fastjson.JSON;
import com.dongyimai.bean.TbItem;
import com.dongyimai.bean.TbItemExample;
import com.dongyimai.dao.TbItemMapper;
import com.github.promeg.pinyinhelper.Pinyin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private SolrTemplate solrTemplate;
    public void importItems(){
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria tbItemCatExample_criteria = tbItemExample.createCriteria();
        tbItemCatExample_criteria.andStatusEqualTo("1");
        List<TbItem> itemList = tbItemMapper.selectByExample(tbItemExample);
        System.out.println("===商品列表===");
        for (TbItem item : itemList) {
            System.out.println(item.getTitle());
            Map<String,String> specMap = JSON.parseObject(item.getSpec(),Map.class);
            Map<String,String> mapPinYin=new HashMap<>();
            for(String key : specMap.keySet()){
                mapPinYin.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            item.setSpecMap(mapPinYin);
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("===结束===");
    }
   public static void main(String[] args){
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:applicationContext-*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItems();
   }

   public void delete (){
       Query query = new SimpleQuery("*:*");
       solrTemplate.delete(query);
       solrTemplate.commit();
   }
}
