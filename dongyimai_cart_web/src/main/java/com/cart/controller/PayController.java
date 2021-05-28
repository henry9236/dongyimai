package com.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dongyimai.bean.TbPayLog;
import com.dongyimai.result.Result;
import com.offcn.utils.IdWorker;
import com.order.service.OrderService;
import com.pay.service.AliPayService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 */
@RestController
@RequestMapping("/pay")
public class PayController {
    //从pay_service注入
    @Reference
    private AliPayService aliPayService;
    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前登录的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //根据用户名在redis中查找支付表数据,
        TbPayLog paylog = orderService.searchPaylogInRedis(username);
        //判断reids中是否有 当前用户的支付表单数据
        if(null==paylog){
            return new HashMap();
        }
        return aliPayService.createNative(paylog.getOutTradeNo(),paylog.getTotalFee()+"");
    }
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result = null;
        long time = 0;
        //不断查询，直到条件满足则退出循环
        while(true){
            Map<String, String> map = null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e1) {
                e1.printStackTrace();
                System.out.println("调用查询服务出错");
            }
            if(map==null){//出错
                result=new  Result(false, "支付出错");
                break;
            }
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_SUCCESS")){//如果成功
                result=new  Result(true, "支付成功");
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("trade_no"));
                break;
            }
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_CLOSED")){//如果成功
                result=new  Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_FINISHED")){//如果成功
                result=new  Result(true, "交易结束");
                break;
            }
            //为了不让循环无休止地运行，我们定义一个循环变量，如果这个变量超过了这个值则退出循环，设置时间为5分钟
            ++time;
            if(time>=100){
                result=new  Result(false, "二维码超时");
                break;
            }
            //让线程沉睡三秒
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
