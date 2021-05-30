package com.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;

import com.alipay.api.response.AlipayTradeQueryResponse;
import com.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 生成支付宝支付二维码
     *
     * @param out_trade_no 订单号
     * @param total_fee    金额
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        Map<String,String> map=new HashMap<String, String>();
        //创建预下单请求对象
        AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();

        //转换下单金额按照元，传入的以分为单位
        long total = Long.parseLong(total_fee);
        BigDecimal bigTotal = BigDecimal.valueOf(total);
        BigDecimal cs = BigDecimal.valueOf(100d);
        //使用bigTotal除以cs 结果：把精度转成元
        BigDecimal bigYuan = bigTotal.divide(cs);
        System.out.println("预下单金额:"+bigYuan.doubleValue());

        //发送给支付宝的
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"total_amount\":\""+bigYuan.doubleValue()+"\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数

        try {
            AlipayTradePrecreateResponse alipayResponse = alipayClient.execute(alipayRequest);
            //从alipayResponse对象读取相应结果
            String code = alipayResponse.getCode();
            System.out.println("响应码:"+code);
            //全部的响应结果
            String body = alipayResponse.getBody();
            System.out.println("返回结果:"+body);
            //如果响应码为10000，代表成功，把数据放到map中返回
            if(code.equals("10000")){
                map.put("qrcode", alipayResponse.getQrCode());
                map.put("out_trade_no", alipayResponse.getOutTradeNo());
                map.put("total_fee",total_fee);
                System.out.println("qrcode:"+alipayResponse.getQrCode());
                System.out.println("out_trade_no:"+alipayResponse.getOutTradeNo());
                System.out.println("total_fee:"+total_fee);
            }else{
                System.out.println("预下单接口调用失败:"+body);
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * 查询支付状态
     * 查询指定订单编号的，交易状态
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map<String,String> map=new HashMap<String, String>();
        AlipayTradeQueryRequest alipayRequest=new AlipayTradeQueryRequest();
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数
        //发送请求
        try {
            AlipayTradeQueryResponse alipayResponse = alipayClient.execute(alipayRequest);
            String code = alipayResponse.getCode();
            System.out.println("返回值1:"+alipayResponse.getBody());
            //如果响应成功，把响应数据放到map中返回

            if(code.equals("10000")){
                map.put("out_trade_no", out_trade_no);
                map.put("tradestatus", alipayResponse.getTradeStatus());
                map.put("trade_no",alipayResponse.getTradeNo());
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 关闭支付
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map closePay(String out_trade_no) {
        Map<String,String> map=new HashMap<String, String>();
        //撤销交易请求对象
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数

        try {
            AlipayTradeCancelResponse response = alipayClient.execute(request);
            String code=response.getCode();

            if(code.equals("10000")){

                System.out.println("返回值:"+response.getBody());
                map.put("code", code);
                map.put("out_trade_no", out_trade_no);
                return map;
            }
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return null;
    }
}
