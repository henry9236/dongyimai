package com.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dongyimai.bean.TbBrand;
import com.dongyimai.result.PageResult;
import com.dongyimai.result.Result;
import com.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    @RequestMapping("/findPage")
    public PageResult findPage(Integer pageNum, Integer pageSize,@RequestBody TbBrand brand ){
        return brandService.findPage(pageNum,pageSize,brand);
    }

    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findByID(id);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
                brandService.addBrand(brand);
                return new Result(true,"增加成功");
        }catch (Exception e){
                return new Result(false,"增加失败");
        }
    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try{
            brandService.updateBrand(brand);
            return new Result(true,"更新成功");
        } catch(Exception e){
            return new Result(false,"更新失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long [] ids){
        try{
            for (Long id:ids) {
                brandService.deleteById(id);
            }
            return new Result(true,"删除成功");
        }catch (Exception e){
            return new Result(false,"删除失败");
        }
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }

}
