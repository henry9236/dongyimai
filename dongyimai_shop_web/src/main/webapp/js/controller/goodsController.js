 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
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
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.add = function(){
		//获取富文件编辑器的内容
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(
			function(response){
				if(response.success){
					alert('保存成功');
					$scope.entity={};
					editor.html('');//清空富文本编辑器
				}else{
					alert(response.message);
				}
			}
		)
	}

	$scope.uploadFile=function(){
		uploadService.uploadFile().success(
			function(response){
			if(response.success){
				$scope.image_entity.url = response.message;
			}else{
				alert(response.message);
			}
		}).error(function(){
			alert("上传发生错误");
		});
	}

	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
	//添加图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}

	$scope.selectItemCat1List = function(){
		itemCatService.findByParentIdNoPage(0).success(
			function(response){
				$scope.itemCat1List = response;
			}
		)
	}
	//监视某个变量的值，发生变化则执行该方法，通过新值查询列表
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
		//判断一级分类有选择具体分类值，在去获取二级分类
		if(newValue){
			//根据选择的值，查询二级分类
			itemCatService.findByParentIdNoPage(newValue).success(
				function(response){
					$scope.itemCat2List = response;
					itemCatService.findOne(newValue).success(
						function(response){
							$scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
						}
					)
					$scope.itemCat3List = {};
				}
			);
		}
	})

	$scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
		if(newValue){
			itemCatService.findByParentIdNoPage(newValue).success(
				function(response){
					$scope.itemCat3List = response;
					itemCatService.findOne(newValue).success(
						function(response){
							$scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
						}
					)
				}
			)
		}
	})

	$scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
		if(newValue){
			itemCatService.findOne(newValue).success(
				function(response){
					$scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
				}
			)
		}
	})

	//根据模板id获取关联规格
	$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
		if(newValue){
			typeTemplateService.findOne(newValue).success(
				function(response){
					$scope.typeTemplateList = JSON.parse(response.brandIds);
					$scope.entity.goods.brandId = {};
					$scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
				}
			)
			typeTemplateService.findSpecOptionList(newValue).success(
				function(response){
					$scope.specList=response;
				}
			)
		}
	})


	$scope.updateSpecAttribute=function($event,name,value){
		//[{“attributeName”:”规格名称”,”attributeValue”:[“规格选项1”,“规格选项2”.... ]  } , ....  ]
		//判断是选中还是取选，选中往对象当中添加规格选项，取选往对象中移除规格选项
		if($event.target.checked){//判断是否选中，因为配置到了click上所以每次选中取选都会调用该方法做判断
			//通过查找确定数组中要修改的对象，如果没有则使用push新增对象
			var tempSpecItem =  $scope.searchList($scope.entity.goodsDesc.specificationItems,name);
			if(null==tempSpecItem){
				$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
			}else{
				tempSpecItem.attributeValue.push(value);
			}
		}else{
			//这里取选，不应该取不到所以不做null判断
			var tempSpecItem =  $scope.searchList($scope.entity.goodsDesc.specificationItems,name);
			if(1==tempSpecItem.attributeValue.length){
				//如果attributeValue只剩一个对象那么要删除的肯定是它，需要将specificationItems数组里的对象删除
				$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(tempSpecItem),1);
			}else{
				//如果不是，则移除attributeValue数组中的对象即可
				tempSpecItem.attributeValue.splice(tempSpecItem.attributeValue.indexOf(tempSpecItem),1);
			}
		}
	}
});	