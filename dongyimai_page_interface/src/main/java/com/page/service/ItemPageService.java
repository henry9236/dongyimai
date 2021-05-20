package com.page.service;

import java.io.IOException;

public interface ItemPageService {
    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public boolean generateItemHtml(Long goodsId) ;
    /**
     * 删除商品静态化网页文件
     * @param goodsIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);

}
