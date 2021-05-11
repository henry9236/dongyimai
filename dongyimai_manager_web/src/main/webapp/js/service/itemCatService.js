//商品类目服务层
app.service('itemCatService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../itemCat/findAll.do');		
	}
	//分页 
	this.findPage=function(page,rows){
		return $http.get('../itemCat/findPage.do?page='+page+'&rows='+rows);
	}
	//查询实体
	this.findOne=function(id){
		return $http.get('../itemCat/findOne.do?id='+id);
	}
	//增加 
	this.add=function(entity){
		return  $http.post('../itemCat/add.do',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../itemCat/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('../itemCat/delete.do?ids='+ids);
	}
	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('../itemCat/search.do?page='+page+"&rows="+rows, searchEntity);
	}

	//分页，根据parentID查找
	this.findByParentId=function(page,rows,parentId){
		return $http.get('../itemCat/findByParentId.do?page='+page+'&rows='+rows+'&parentId='+parentId);
	}
	//根据parentID查找
	this.findByParentIdNoPage=function(parentId){
		return $http.get('../itemCat/findByParentIdNoPage.do?parentId='+parentId);
	}
	//获取模板列表
	this.selectList=function(){
		return $http.get('../typeTemplate/selectList.do');
	}
});