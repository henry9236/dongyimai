package com.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.dongyimai.bean.TbGoods;
import com.dongyimai.bean.TbGoodsDesc;
import com.dongyimai.bean.TbItem;
import com.dongyimai.bean.TbItemExample;
import com.dongyimai.dao.TbGoodsDescMapper;
import com.dongyimai.dao.TbGoodsMapper;
import com.dongyimai.dao.TbItemCatMapper;
import com.dongyimai.dao.TbItemMapper;
import com.page.service.ItemPageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${pagedir}")
    private String pagedir;
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;
    /**
     * 生成商品详情页
     *
     * @param goodsId
     * @return
     */
    @Override
    public boolean generateItemHtml(Long goodsId){
        try {
            //创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是 freemarker的版本号
            Configuration configuration = freeMarkerConfig.getConfiguration();
            //加载一个模板，创建一个模板对象。
            Template template = configuration.getTemplate("item.ftl");
            //创建一个模板使用的数据集，可以是 pojo 也可以是 map。一般是 Map。
            Map dataModel = new HashMap();
            //1,加载商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            //2,加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);
            //3,加载商品分类数据
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);
            //4,SKU列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//过滤条件状态为有效
            criteria.andGoodsIdEqualTo(goodsId);//指定spu的id
            example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList",itemList);
            //创建一个 Writer 对象，指定生成的文件名。
            Writer out = new FileWriter(pagedir + goodsId + ".html");
            //调用模板对象的 process 方法输出文件
            template.process(dataModel, out);
            //关闭流
            out.close();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
