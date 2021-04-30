app.controller('brandController',function ($scope,$controller,brandService){

    $controller('baseController',{$scope:$scope});//继承

    //分页查询
    $scope.findPage = function (pageNum,pageSize,searchBrand){
        brandService.findPage(pageNum,pageSize,searchBrand).success(
            function (response){
                $scope.list = response.rows;   //分页后的集合
                $scope.paginationConf.totalItems = response.totalCount;     //总记录数
                $scope.checkAllChecked = false;
                $scope.selectIds = [];
            })
    }

    $scope.searchBrand = {};
    $scope.loadList = function (){
        $scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage,$scope.searchBrand);
    }

    //保存品牌
    $scope.save = function(){
        //对要保存的数据做正则判断

        if (!/^[A-Za-z]{1}$/.test($scope.brand.firstChar)){
            alert("首字母输入不符合规范，请重写");
            return;
        }

        if (null==$scope.brand.id){
            brandService.add($scope.brand).success(
                function(response){
                    if(response.success){
                        $scope.loadList();
                    }
                    else{
                        alert(response.message);
                    }
                }
            );
        }else{
            brandService.update($scope.brand).success(
                function(response){
                    if(response.success){
                        $scope.loadList();
                    }
                    else{
                        alert(response.message);
                    }
                }
            )
        }
    }

    //根据id找到要修改的品牌
    $scope.findOne = function(id){
        brandService.findOne(id).success(
            function(response){
                $scope.brand = response;
            });
    }


    $scope.del = function () {
        if($scope.selectIds.length==0){
            alert("请选择要删除的品牌");
            return;
        }
        brandService.dele($scope.selectIds).success(
            function(response){
                if(response.success){
                    //删除成功，刷新页面
                    $scope.loadList();
                    //清空ID集合
                    $scope.selectIds = [];
                }else{
                    //失败则弹出失败提示
                    alert(response.message);
                }
            }
        )
    }

    $scope.checkAll = function($event){
        if($event.target.checked){

            //遍历list把是否选中设置true
            angular.forEach($scope.list,function(brand){
                //把要删除的id放进selectIds
                $scope.selectIds.push(brand.id);
                brand.checked = true;
            })
        }else{
            //清空ID集合
            $scope.selectIds = [];
            //遍历list把是否选中设置为false
            angular.forEach($scope.list,function(brand){
                brand.checked = false;
            })
        }
    }

    $scope.searchEntity = {};
    $scope.searchReload = function(){
        $scope.searchBrand = $scope.searchEntity;
        $scope.loadList();
    }
});