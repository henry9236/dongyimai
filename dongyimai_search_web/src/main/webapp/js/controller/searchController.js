app.controller('searchController',function($scope,searchService){
    //搜索
    $scope.search=function(){
        searchService.search($scope.searchMap).success(
            function(response){
                $scope.resultMap=response;
            }
        );
    }

    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{}};
    //添加搜索项，搜索对象,把过滤条件添加到searchMap
    $scope.addSearchItem=function(key,value){
        //如果点击的是分类或者是品牌，设置分类或品牌
        if(key=='category' || key=='brand'){
            $scope.searchMap[key] = value;
        }else{//不然就是规格，添加给spec,传入的时规格名称，和属性，作为key和value
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//执行搜索
    }
    //移除复合搜索条件
    $scope.removeSearchItem = function(key){
        //如果是分类或品牌
        if(key=="category" || key=="brand"){
            $scope.searchMap[key]="";
        }else{
            delete $scope.searchMap.spec[key];
        }
        $scope.search();//执行搜索
    }
});