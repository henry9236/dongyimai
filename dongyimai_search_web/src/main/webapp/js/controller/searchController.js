app.controller('searchController',function($scope,$location,searchService){
    //搜索
    $scope.search=function(){
        //从input框输入的数据是字符串，需要在这里做个转换，不然后台没做转换就会报ClassCastException
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
        searchService.search($scope.searchMap).success(
            function(response){
                $scope.resultMap=response;
                //调用
                buildPageLabel();
            }
        );
    }
    //搜索条件封装对象
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':{},'pageNo':1,'pageSize':20};
    //添加搜索项，搜索对象,把过滤条件添加到searchMap
    $scope.addSearchItem=function(key,value){
        //如果点击的是分类或者是品牌，设置分类或品牌，价格
        if(key=='category' || key=='brand'){
            $scope.searchMap[key] = value;
        }else{//不然就是规格，添加给spec,传入的时规格名称，和属性，作为key和value
            $scope.searchMap.spec[key] = value;
        }
        $scope.searchMap.pageNo = 1;
        //执行查询
        $scope.search();//执行搜索
    }
    //添加
    $scope.addPriceFilter = function(price1,price2){
        $scope.searchMap.price.low = price1;
        $scope.searchMap.price.height = price2;
        $scope.searchMap.pageNo = 1;
        //执行查询
        $scope.search();
    }

    $scope.isEmpty = function (obj) {
        for (var i in obj) if (obj.hasOwnProperty(i)) return false;
        return true;
    };

    //移除复合搜索条件
    $scope.removeSearchItem = function(key){
        if(key=='price'){
            $scope.searchMap[key] = {};
            $scope.searchMap.pageNo = 1;
            //执行查询
            $scope.search();//执行搜索
            return;
        }
        //如果是分类或品牌
        if(key=="category" || key=="brand"  ){
            $scope.searchMap[key]="";
        }else{
            delete $scope.searchMap.spec[key];
        }
        $scope.searchMap.pageNo = 1;
        //执行查询
        $scope.search();//执行搜索
    }

    //构建分页标签（totalPages）为总页数
    buildPageLabel = function(){
       //新增分页栏属性
        $scope.pageLabel=[];
        $scope.firstDot=false;//前面点
        $scope.lastDot=false;//后边点
        //得到最后页码,通过后台返回的总页数
        var maxPageNo = $scope.resultMap.totalPages;
        //开始页码
        var firstPage = 1;
        //截止页码
        var lastPage = maxPageNo;
        // 显示部分页码，通过设置 firstPage和 lastPage
        //如果总页数大于5页
        if($scope.resultMap.totalPages>5){
            //如果当前页小于等于3,显示前5页
            if($scope.searchMap.pageNo<=3){
                lastPage = 5;
                $scope.lastDot=true;//后面有点
                //如果当前页大于等于最大页码-2，显示后5页
            }else if($scope.searchMap.pageNo>=lastPage-2){
                firstPage = maxPageNo-4;
                $scope.firstDot=true;
            }else{//显示以当前页为中心的5页
                firstPage = $scope.searchMap.pageNo-2;
                lastPage = $scope.searchMap.pageNo+2;
                $scope.firstDot=true;//前面有点
                $scope.lastDot=true;//后边有点
            }
        }
        //循环产生页码标签
        for(var i = firstPage;i<=lastPage;++i){
            $scope.pageLabel.push(i);
        }
    }
    //根据页码查询
    $scope.queryByPage = function(pageNo){
        //页码验证非法查询
        if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
            return;
        }
        //把传入的查询页赋值给搜索实例
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }
    //设置排序规则,
    $scope.sortSearch=function(sortField,sort){
        //把排序规则，交给过滤条件，交给后端
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.searchMap.pageNo = 1;
        $scope.search();
    }
    //判断关键字是不是品牌，同过遍历barnList中的元素
    $scope.keywordsIsBrand=function(){
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)!=-1){
                return true;
            }
        }
        return false;
    }
    //加载从首页传来的搜索关键字
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }
});