package com.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.dongyimai.bean.TbBrand;
import com.dongyimai.bean.TbBrandExample;
import com.dongyimai.dao.TbBrandMapper;
import com.dongyimai.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
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
    public PageResult findPage(Integer pageNum, Integer pageSize,TbBrand brand) {
        //1，使用分页插件设置参数
        PageHelper.startPage(pageNum,pageSize);
        //2,进行分页查询，创建criteria条件
        TbBrandExample tbBrandExample = new TbBrandExample();
        TbBrandExample.Criteria tbBrandExample_criteria = tbBrandExample.createCriteria();
        if(null!=brand.getName() && !"".equals(brand.getName()))
            tbBrandExample_criteria.andNameLike('%'+brand.getName()+'%');
        if(null!=brand.getFirstChar() && !"".equals(brand.getFirstChar()))
            tbBrandExample_criteria.andFirstCharEqualTo(brand.getFirstChar());

        Page<TbBrand> page = (Page<TbBrand>) tbBrandMapper.selectByExample(tbBrandExample);
        //3，返回分页结果
        return new PageResult(page.getTotal(),page.getResult());
    }
    /**
     * 添加品牌
     * 如果返回影响行不为1则代表出现了错误，抛异常
     * @param brand
     */
    @Override
    public void addBrand(TbBrand brand) throws Exception {
        if(1!=tbBrandMapper.insert(brand))
        {
            throw new Exception("insert fail");
        }
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @Override
    public TbBrand findByID(Long id) {
        TbBrandExample tbBrandExample = new TbBrandExample();
        TbBrandExample.Criteria tbBrandExample_criteria = tbBrandExample.createCriteria();
        tbBrandExample_criteria.andIdEqualTo(id);
        return  tbBrandMapper.selectByExample(tbBrandExample).get(0);
    }
    /**
     * 更新品牌
     * @param brand
     * @throws Exception
     */
    @Override
    public void updateBrand(TbBrand brand) throws Exception {
        if(1!=tbBrandMapper.updateByPrimaryKey(brand))
        {
            throw new Exception("update fail");
        }
    }

    /**
     * 根据id删除brand
     * @param id
     */
    @Override
    public void deleteById(Long id) throws Exception {
        if(1!=tbBrandMapper.deleteByPrimaryKey(id)){
            throw new Exception("delete fail");
        }
    }

    @Override
    public List<Map> selectOptionList() {
        return tbBrandMapper.selectOptionList();
    }
}
