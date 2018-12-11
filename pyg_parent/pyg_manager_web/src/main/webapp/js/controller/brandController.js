//定义控制器
app.controller("brandController",function ($scope,$controller,brandService) {
    // //读取列表数据绑定到表单中
    // $scope.findAll=function () {
    // $http.get("../brand/findAll.do").success(function (response) {
    // 	$scope.list=response;
    //     });
    // }

    $controller("baseController",{$scope:$scope});//继承

    //分页
    $scope.findPage=function(page,size) {
        brandService.findPage(page,size).success(
            function (response) {
                $scope.list = response.rows;//显示当前页数据
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };


    //保存
    $scope.save=function () {
        var object=null;
        if($scope.entity.id!=null){
            object=brandService.update($scope.entity);
        }else {
            object=brandService.add($scope.entity);
        }

        object.success(function (response) {
            if(response.success){
                // alert("dd");
                $scope.reloadList();//刷新
                // alert("ee");
            }else {
                alert(response.message)
            }
        })
    };

    //查询实体

    $scope.findOne=function (id) {

        //alert(id);
        brandService.findOne(id).success(function (response) {
            $scope.entity=response;
            //alert($scope.entity);
        })
    };

    //删除
    $scope.dele=function () {
        //获取选中的复选框
        brandService.dele($scope.selectIds).success(function (response) {
            if(response.success){
                //alert("aaa");
                $scope.reloadList();
                //alert("bbb");
            }else {
                //alert("ccc");
                alert(response.message)
            }
        })
    };
//条件查询
    $scope.searchEntity={};
    $scope.search=function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;//显示当前页数据
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };


});
