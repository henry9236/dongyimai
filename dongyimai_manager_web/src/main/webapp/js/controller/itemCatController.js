 //商品类目控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{

			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){
		itemCatService.findByParentId(page,rows,$scope.currentParentId).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

	$scope.currentParentId = 0;
	$scope.findByParentId=function(parentId){
		$scope.currentParentId = parentId;
		itemCatService.findByParentId($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage,parentId).success(
			function(response){
				$scope.list = response.rows;
				$scope.paginationConf.totalItems=response.total;
			}
		);
	}
	$scope.grade = 1;//默认等级为1
	$scope.setGrade = function(value){
		$scope.grade=value;//设置级别
	}

	$scope.selectList = function(p_entity){
		if(1==$scope.grade){
			$scope.entity_1 = null;
			$scope.entity_2 = null;
		}
		if(2==$scope.grade){
			$scope.entity_1 = p_entity;
			$scope.entity_2 = null;
		}
		if(3==$scope.grade){
			$scope.entity_2 = p_entity;
		}
		$scope.findByParentId(p_entity.id);
	}

	$scope.getTypeTemplatetList = function(){
		itemCatService.selectList().success(
			function(response){
				$scope.typeTemplateList = {data:response};
			}
		);
	}
});	