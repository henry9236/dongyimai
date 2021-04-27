package com.sellergoods.service;

import com.dongyimai.bean.TbBrand;

import java.util.List;

public interface BrandService {
    /**
     * 查询所有品牌
     * @return
     */
    public List<TbBrand> findAll();
}
