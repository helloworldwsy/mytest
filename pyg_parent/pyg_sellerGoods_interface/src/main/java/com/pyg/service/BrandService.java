package com.pyg.service;

import com.pyg.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {

    public List<TbBrand> findAll();

    /**
     * 品牌分页
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findPage(int pageNum,int pageSize);
    /**
     * 增加
     */
    public void add(TbBrand brand);
    //根据id查询一个
    public TbBrand findOne(long id);
    //修改
    public void update(TbBrand brand);

    //删除
    public void delete(long[] ids);
    //条件查询
    public PageResult search(TbBrand brand,int pageNum,int pageSize);

    /**
     * 品牌下拉框数据
     */
    List<Map> selectOptionList();
}
