app.controller('cartController',
    function($scope,cartService){

        //查询购物车类别
        $scope.findCarList=function() {
            cartService.findCartList().success(
                function (response) {
                    $scope.cartList = response;
                    $scope.totalValue=cartService.priceSum($scope.cartList);//求合计数
                })
        }
         //添加商品到购物车
        $scope.addGoodsToCartList = function(itemId,num){
            cartService.addGoodsToCartList(itemId,num).success(
              function(response){
                  if(response.success){
                      $scope.findCarList();//刷新列表
                  }else{
                      alert(response.message);//弹出错误提示
                  }
              }
            );
        }
        //获取地址列表
        $scope.findAddressList= function(){
            cartService.findAddressList().success(
                function(response){
                    $scope.addressList= response;
                    //设置默认地址
                    for(var i=0;$scope.addressList.length;i++){
                        if('1'==$scope.addressList[i].isDefault){
                            $scope.address = $scope.addressList[i];
                            break;
                        }
                    }
            });
        }

        //选中地址
        $scope.selectAddress = function(address){
            $scope.address = address;
        }
        //判断是否是当前选择的地址
        $scope.isSelectedAddress = function(address){
            if(address==$scope.address){
                return true;
            }else{
                return false;
            }
        }

        //获取当前登录的用户名
        $scope.getLoginUserName = function(){
            cartService.getLoginUserName().success(
                function(response){
                    var stringName = response.replaceAll("\"","");
                    if(response == "\"anonymousUser\"")
                    {

                        $scope.loginUserName =null;
                    }else{

                        $scope.loginUserName = stringName;
                    }
                }
            );
        }

        $scope.order={paymentType:'1'};

        //选责支付方式
        $scope.selectPayType = function(type){
            $scope.order.paymentType = type;
        }

        //提交订单，先将订单保存
        $scope.submitOrder = function(){
            $scope.order.receiverAreaName = $scope.address.address;//地址
            $scope.order.receiverMobile = $scope.address.mobile;//手机
            $scope.order.receiver = $scope.address.contact;//联系人
            cartService.submitOrder($scope.order).success(
                function(response){
                    if(response.success){
                        if($scope.order.paymentType=='1'){
                            location.href = "pay.html";
                        }else{
                            location.href="paysuccess.html";
                        }
                    }else{
                        alert(response.message);
                    }
                }
            );
        }

        $scope.addAddress=function(){
            $scope.newAddress.userId = $scope.loginUserName;
            cartService.addAddress($scope.newAddress).success(
                function(response) {
                    if(response.success){
                        location.reload();
                    }else{
                        alert(response.message);
                    }
                }
            );
        }

});