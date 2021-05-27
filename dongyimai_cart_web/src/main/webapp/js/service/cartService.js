app.service('cartService',function($http){

    this.findCartList=function(){
        return $http.get('cart/findCartList.do');
    }

    //添加商品到购物车
    this.addGoodsToCartList=function(itemId,num){
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }

    //求商品价格，和数量 总和 通过遍历cartList中的所有orderItemList
    this.priceSum = function(cartList){
        var totalValue = {totalNum:0, totalMoney:0.00 };//合计实体
        for(var i=0;i<cartList.length;i++){
            for(var j=0;j<cartList[i].orderItemList.length;j++){
                totalValue.totalNum += cartList[i].orderItemList[j].num;
                totalValue.totalMoney += cartList[i].orderItemList[j].totalFee;
            }
        }
        return totalValue;
    }

    //获取地址列表
    this.findAddressList = function(){
        return $http.get('address/findListByLoginUser.do');
    }
    //获取登录的用户名
    this.getLoginUserName = function(){
        return $http.get('cart/getLoginUserName.do');
    }
});