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
    public PageResult findPage(Integer pageNum, Integer pageSize,TbBrand brand);

    /**
     * 添加品牌
     * 如果返回影响行不为1则代表出现了错误，抛异常
     * @param brand
     */
    public void addBrand (TbBrand brand) throws Exception;

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    public TbBrand findByID (Long id);

    /**
     * 更新品牌
     * @param brand
     * @throws Exception
     */
    public void updateBrand (TbBrand brand)throws Exception;

    /**
     * 根据id删除brand
     * @param id
     */
    public void deleteById (Long id) throws Exception;
}

