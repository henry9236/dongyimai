package com.page.service;

import java.io.IOException;

public interface ItemPageService {
    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public boolean generateItemHtml(Long goodsId) ;
}
