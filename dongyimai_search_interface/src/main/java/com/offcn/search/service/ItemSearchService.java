package com.offcn.search.service;

import com.dongyimai.bean.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     * 将item的数据导入到solr中
     * @param list
     */
    public void imporItemtList(List<TbItem> list );

    /**
     * 从solr中删除数据
     * @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);
}
