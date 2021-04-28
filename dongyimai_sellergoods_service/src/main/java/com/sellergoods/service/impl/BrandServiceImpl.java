package com.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.dongyimai.bean.TbBrand;
import com.dongyimai.dao.TbBrandMapper;
import com.dongyimai.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper tbBrandMapper;

    /**
     * 查询所有品牌
     * @return
     */
    @Override
    public List<TbBrand> findAll() {
        return tbBrandMapper.selectByExample(null);
    }

    /**
     * 分页查询品牌列表
     * @param pageNum   当前页码
     * @param pageSize  每页查询记录数
     * @return
     */
    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        //1，使用分页插件设置参数
        PageHelper.startPage(pageNum,pageSize);
        //2,进行分页查询
        Page<TbBrand> page = (Page<TbBrand>) tbBrandMapper.selectByExample(null);
        //3，返回分页结果
        return new PageResult(page.getTotal(),page.getResult());
    }
}
