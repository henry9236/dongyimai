app.controller('baseController',function ($scope){

    //设置分页参数
    $scope.paginationConf = {
        'currentPage':1,    //当前页码
        'itemsPerPage': 10,  //每页查询记录数
        'totalItems': 10,    //总记录数
        'perPageOptions': [10,20,30,40,50],   //每页查询记录数选择器
        onChange: function (){
            $scope.loadList();
        }
    }

    $scope.selectIds = [];
    //选中/反选
    $scope.updateSelection = function($event,id){
        //判断复选框是否被选中
        if($event.target.checked){
            $scope.selectIds.push(id);
        }else{
            //反选  在集合中移除元素
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }
    }

});