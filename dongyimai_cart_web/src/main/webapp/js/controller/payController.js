app.controller('payController',function($scope,$location,payService){
   //本地生产二维码
   $scope.createNative=function(){
       payService.createNative().success(
           function(response){
               //金额
               $scope.money=(response.total_fee/100).toFixed(2);
               //订单号
               $scope.out_trade_no=response.out_trade_no;
               //根据后台返回的支付包支付链接，创建支付的二维码
               var qr = new QRious({
                   element:document.getElementById('qrious'),
                   size:250,
                   level:'H',
                   value:response.qrcode
               });
               //查询支付状态
               queryPayStatus(response.out_trade_no);
           }
       );
   }
    $scope.tiemoutview = true;

    queryPayStatus=function(out_trade_no){
        payService.queryPayStatus(out_trade_no).success(
            function(response){
            if(response.success){
                location.href="paysuccess.html#?money="+$scope.money;
            }else{
                if(response.message=='二维码超时'){
                    $scope.tiemoutview = false;
                }else{
                    location.href="payfail.html";
                }
            }
        });
    }

    $scope.getMoney=function(){
        return $location.search()['money'];
    }

});