app.controller("baseController",function ($scope) {
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],//分页选项
        onChange: function(){ //当页码变更后自动触发的方法
            $scope.reloadList();
        }
    };


    // //刷新列表
    // $scope.reloadList=function () {
    //     $scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage)
    // };


    //刷新列表
    $scope.reloadList=function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage)
    };


    // //全选全不选
    $scope.choose = function ($event) {
        if ($event.target.checked) {
            for (var i = 0; i < $scope.list.length; i++) {
                $scope.selectIds.push($scope.list[i].id);
            }
        } else {
            $scope.selectIds.splice(0, $scope.selectIds.length);
        }
        console.log($scope.selectIds);
    };

    $scope.selectIds = [];//用户勾选的id集合
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);//向集合添加元素
        } else {
            var index = $scope.selectids.indexOf(id);//查找值在数组中的位置
            $scope.selectIds.splice(index, 1);//参数1：移除的位置，参数2：移除的个数
        }
    };


    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将json字符串转换为json对象
        var value=" ";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=","
            }
            value+=json[i][key];
        }
        return value;
    };
});