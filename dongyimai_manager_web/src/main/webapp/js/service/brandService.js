app.service('brandService',function($http){

    this.findPage = function(pageNum,pageSize,searchBrand){
        return $http.post('../brand/findPage.do?pageNum='+pageNum+'&pageSize='+pageSize,searchBrand);
    }
    //添加
    this.add = function(brand){
        return $http.post('../brand/add.do',brand);
    }
    //修改
    this.update = function(brand){
        return $http.post('../brand/update.do',brand);
    }
    //获取指定商品id信息
    this.findOne = function(id){
        return $http.get('../brand/findOne.do?id=' + id);
    }
    //删除
    this.dele = function(ids){
        return $http.get('../brand/delete.do?ids='+ids);
    }
});