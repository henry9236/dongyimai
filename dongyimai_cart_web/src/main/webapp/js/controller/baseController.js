app.controller('baseController',function ($scope){
    //设置分页参数
    $scope.paginationConf = {
        'currentPage': 1,    //当前页码
        'itemsPerPage': 10,  //每页查询记录数
        'totalItems': 10,    //总记录数
        'perPageOptions': [10, 20, 30, 40, 50],   //每页查询记录数选择器
        onChange: function () {
            //执行分页查询
            //$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
            $scope.reloadList();
        }
    }
    $scope.reloadList = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    $scope.selectIds = [];     //初始化ID集合的数据结构

    //选中/反选
    $scope.updateSelection = function ($event, id) {
        //判断复选框是否被选中
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            //反选  在集合中移除元素
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index, 1)    //参数一：元素的索引位置  参数二：删除的个数
        }
    }

    //全选/反选
    $scope.selectAll = function ($event){
        //选中，则将集合中的所有的ID放入到selectIds数组中
        if($event.target.checked){
            for(var i=0;i<$scope.list.length;i++){
                $scope.selectIds.push($scope.list[i].id);
            }
        }else{
            //反选，将selectIds清空
            $scope.selectIds = [];
        }
    }

    //判断复选框是否选中
    $scope.isSelected = function(id){
        for(var i=0;i<$scope.selectIds.length;i++){
            if($scope.selectIds[i]==id){
                return true;
            }
        }
        return false;
    }


    //转换字符串
    $scope.jsonToString = function (jsonStr,key){
        //1.将JSON结构的字符串转换成JSON对象
       var json = JSON.parse(jsonStr);
       var value='';
        //2.遍历集合
        if(json!=null){
            for(var i=0;i<json.length;i++){
                if(i>0){
                    value+=",";
                }
                //3.根据key取值，并完成拼接
                value +=  json[i][key];
            }
        }
        return value;
    }

    //在list中查找attributeName == name的对象返回
    $scope.searchList= function(list,name){
        for(i=0;i<list.length;++i){
            if(list[i].attributeName==name){
                return list[i];
            }
        }
        return null;
    }

    //查询JSON集合中是否有对象
    $scope.searchObjectByKey = function(list,key,value){
        for(var i=0;i<list.length;i++){
            if(list[i][key]==value){
                return list[i];
            }
        }
        return null;
    }
})