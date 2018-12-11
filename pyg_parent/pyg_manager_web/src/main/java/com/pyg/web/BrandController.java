package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbBrand;
import com.pyg.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    //从远程获取接口的服务实现
    @Reference
    private BrandService brandService;

    @RequestMapping("findAll")
    public List<TbBrand> findAll(){
       return  brandService.findAll();
    }

    @RequestMapping("findPage")
    public PageResult findPage(int page,int size){
        return brandService.findPage(page,size);
    }
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }
    @RequestMapping("findOne")
    public TbBrand findOne(long id){
       return brandService.findOne(id);
    }

    @RequestMapping("update")
    public Result update(@RequestBody TbBrand brand){
       // System.out.println("aaa");
        try {
            brandService.update(brand);
           // System.out.println("bbb");
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    @RequestMapping("delete")
    public Result delete(long[] ids){
        try {
            brandService.delete(ids);
            return  new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
    @RequestMapping("search")
    public PageResult search(@RequestBody TbBrand brand,int page,int size){
        //System.out.println("aaa");
        return brandService.search(brand, page, size);
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }

}
