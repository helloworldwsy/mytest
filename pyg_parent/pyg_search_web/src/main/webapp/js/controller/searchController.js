app.controller('searchController',function($scope,$location,searchService){

    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};//搜索对象



    //搜索
    $scope.search=function(){
        $scope.searchMap.pageNo=parseInt( $scope.searchMap.pageNo);
        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
               //查询后显示页码为1
                //$scope.searchMap.pageNo=1;
                //构建分页栏
               buildPage();

            }
        );
    };

    buildPage=function() {
        $scope.pageLabel=[];//新增分页栏属性
        var firstPage = 1;//开始页码
        var lastPage =$scope.resultMap.totalPages;//截止页码
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点
        if($scope.resultMap.totalPages>5){//如果页码数量大于5
            if($scope.searchMap.pageNo<=3){//如果当前页码小于3
                lastPage=5;
                //前面没点
                $scope.firstDot=false;
            }else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){
                firstPage=$scope.resultMap.totalPages-4;
                //后面没点
                $scope.lastDot=false;
            }else {
               // alert($scope.searchMap.pageNo)
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else {
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后边无点
        }

        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    };


    //添加搜索项
    $scope.addSearchItem=function(key,value){
        if(key=='category'|| key=='brand'|| key=='price'){
            $scope.searchMap[key]=value;
        }else {
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();//执行搜索
    };

    $scope.removeSearchItem=function(key){
        if(key=='category'|| key=='brand' ||key=='price'){
            $scope.searchMap[key]="";
        }else {
           delete $scope.searchMap.spec[key];
        }
        $scope.search();//执行搜索
    };

    //根据页码查询
    $scope.queryByPage = function (pageNo){
        //页码验证
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        //刷新
        $scope.search();
    };

    //判断当前页为第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    };
//判断当前页是否未最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    };

    //排序
    $scope.sortSearch=function (sortField,sort) {
       $scope.searchMap.sort=sort;
       $scope.searchMap.sortField=sortField;
       $scope.search();//查询
    };

    //判断关键字是不是品牌
    $scope.keywordsIsBrand=function(){
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
                if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                    //如果包含
                    return true;
                }
        }
        return false;
    }


    //加载查询字符串

    $scope.loadkeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();
    }
});