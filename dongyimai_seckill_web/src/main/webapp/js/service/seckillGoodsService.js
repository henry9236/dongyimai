//服务层
app.service('seckillGoodsService',function($http){
    //读取列表数据绑定到表单中
    this.findList=function(){
        return $http.get('seckillGoods/findList.do');
    }

    //根据指定id查询秒杀商品
    this.findOne=function(id){
        return $http.get('seckillGoods/findOneFromRedis.do?id='+id);
    }

    //提交订单
    this.submitOrder = function(seckillId){
        return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
    }

    //查询当前登陆的用户名
    this.getLoginUserName = function(){
        return $http.get('seckillGoods/getLoginUserName.do');
    }
});
