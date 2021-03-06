app.controller('itemController',function($scope,$http){
	//数量操作
	$scope.addNum=function(num){
		$scope.num = $scope.num+num;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	//记录用户选择的规格
	$scope.selectSpecificationItems={};
	$scope.selectSpecification=function(name,value){
		$scope.selectSpecificationItems[name]=value;
		searchSku();
	}
	//判断某规格选项是否被用户选中
	$scope.isSelect = function(name,value){
		if($scope.selectSpecificationItems[name]==value){
			return true;
		}else{
			return false;
		}
	}
	//加载默认SKU
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		//根据sku 的规格选中
		$scope.selectSpecificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
	}
	
	searchSku = function(){
		for(i=0;i<skuList.length;++i){
			//遍历sku集合中的规格是否与选中的规格一样
			if(matchObject(skuList[i].spec,$scope.selectSpecificationItems)){
				//一样的话就把，当前sku变量中的值替换
				$scope.sku=skuList[i];
				return;
			}
		}
		$scope.sku={id:0,title:'发生错误',price:0};//如果没有匹配的		
	}

	matchObject=function(map1,map2){
		//遍历值查看是否相同
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}			
		}
		//确保完全一样，没有全部选中则不改变sku
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}			
		}

		return true;	

	}
	//加入购物车,使用跨域请求，
	$scope.addToCart=function(){
		//将商品详情页的，商品id和数量num通过addGoodsToCartList方法添加到购物车
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
			function(response) {
				if(response.success){
					location.href = 'http://localhost:9107/cart.html';//跳转到购物车页面
				}else{
					alert(response.message);
				}
			}
		)
	}

})