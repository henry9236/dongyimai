package com.sellergoods.service;

import com.dongyimai.bean.TbBrand;
import com.dongyimai.result.PageResult;

import java.util.List;

public interface BrandService {
    /**
     * 查询所有品牌
     * @return
     */
    public List<TbBrand> findAll();

    /**
     * 分页查询品牌列表
     * @param pageNum   当前页码
     * @param pageSize  每页查询记录数
     * @return
     */
    public PageResult findPage(Integer pageNum, Integer pageSize);
}
