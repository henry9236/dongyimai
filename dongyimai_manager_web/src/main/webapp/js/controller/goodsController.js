 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
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
	$scope.findOne=function(){
		var id= $location.search()['id'];//获取参数值
		if(id==null){
			return ;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;

				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//显示图片列表
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				//显示扩展属性
				$scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//SKU列表规格列转换
				for( var i=0;i<$scope.entity.itemList.length;i++ ){
					$scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
				}
			}
		);				
	}

	//保存 
	$scope.save=function(){
		//提取文本编辑器的值
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加
		}
		serviceObject.success(
			function(response){
				if(response.success){
					location.href="goods.html";//跳转到商品列表页
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
					$scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]}  };
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
					if($location.search()['id']==null) {
						$scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
					}
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

	//[{spec:{多个规格1:规格选项1},price:0,num:9999,status:'0',isDefault:'0' }，{......} ];
	//将specification数组中的，attributeValue每个数据，与specification数组中其他元素的，attributeValue数组的每个数据组合起来，放到itemList中，每个组合存为一个对象
	$scope.createItemList = function(){
		//取选的时候也会对itemList数组中的所有数据进行遍历添加，会导致数据无法删除,需要每次响应checkbox事件时对itemList做个清空的初始化
		$scope.entity.itemList=[{spec:{},price:0,num:9999,status:'0',isDefault:'0' } ];//初始化itemList
		//遍历specification数组
		for (i=0;i<$scope.entity.goodsDesc.specificationItems.length;++i){
			//创建tempItemList用于保存对itemList数据的修改，最后用于替换itemList中的数据
			var tempItemList = [];
			//遍历itemList数组，每次都对itemList中的所有数据进行操作
			for(j=0;j<$scope.entity.itemList.length;++j){
				//保存，itemList数组下标为j的数据,做深克隆用
				var oldrow = $scope.entity.itemList[j];
				//循环specification下标为I的attributeValue数组的数据
				for(k=0;k<$scope.entity.goodsDesc.specificationItems[i].attributeValue.length;++k){
					//做深克隆，(深克隆，根据原有对象，创建一个独立于原有对象的拷贝)
					var newrow = JSON.parse( JSON.stringify( oldrow )  );//深克隆
					//$scope.entity.goodsDesc.specificationItems[i].attributeName中的名称，
					//将attributeValue数组中k下标的数据保存到，深克隆出来的对象中的spec对象中的名称为 specificationItems的i下标的attributeName的字段
					newrow.spec[$scope.entity.goodsDesc.specificationItems[i].attributeName] = $scope.entity.goodsDesc.specificationItems[i].attributeValue[k];
					//将数据保存到tempItemList数组中
					tempItemList.push(newrow);
				}
			}
			//用tempItemLsit数组中的数据替换itemList的数据
			$scope.entity.itemList = tempItemList;
		}
	}

	//初始化一个状态值的集合  0 未审核  1 审核通过 2 驳回  3 关闭
	$scope.status = ['未审核', '审核通过', '驳回', '关闭'];

	//商品分类类表
	$scope.itemCatList = [];
	//加载商品分类列表
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(
			function(response){
				for(i=0;i<response.length;++i){
					$scope.itemCatList[response[i].id] = response[i].name;
				}
			}
		)
	}

	//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function(specName,optionName){
		var items= $scope.entity.goodsDesc.specificationItems;
		var object= $scope.searchObjectByKey(items,'attributeName',specName);
		if(object==null){
			return false;
		}else{
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}

	$scope.updateStatus=function(status){
		goodsService.updateStatus($scope.selectIds,status).success(
			function(response){
				if(response.success){
					$scope.reloadList();
					$scope.selectIds=[];
				}else{
					alert(response.message);
				}
			}
		);
	}
});	