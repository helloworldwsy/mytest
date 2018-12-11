package com.pyg.service.impl;
import java.util.List;
import java.util.Map;

import com.pyg.mapper.TbSpecificationOptionMapper;
import com.pyg.pojo.TbSpecificationExample;
import com.pyg.pojo.TbSpecificationOption;
import com.pyg.pojo.TbSpecificationOptionExample;
import com.pyg.pojoGroup.Specification;
import com.pyg.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSpecificationMapper;
import com.pyg.pojo.TbSpecification;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		//获取规格实体类
		TbSpecification tbspecification = specification.getSpecification();
		specificationMapper.insert(tbspecification);

		//获取规格选项集合
		List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
		for (TbSpecificationOption option : specificationOptionList) {
			option.setSpecId(tbspecification.getId());//设置规格id
			specificationOptionMapper.insert(option);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){

		//获取规格实体类
		TbSpecification tbspecification = specification.getSpecification();
		specificationMapper.updateByPrimaryKey(tbspecification);
		//删除原来的选项集合
		TbSpecificationOptionExample example=new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(specification.getSpecification().getId());//指定id
		specificationOptionMapper.deleteByExample(example);

		//获取规格选项集合
		List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
		for (TbSpecificationOption option : specificationOptionList) {
			option.setSpecId(tbspecification.getId());//设置规格id
			specificationOptionMapper.insert(option);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		Specification specification=new Specification();
		//获取规格实体
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);

		specification.setSpecification(tbSpecification);
		//获取规格选项列表
		TbSpecificationOptionExample example=new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(id);//根据规格ID查询
		List<TbSpecificationOption> optionList = specificationOptionMapper.selectByExample(example);

		specification.setSpecificationOptionList(optionList);
		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
		    //删除规格表数据
			specificationMapper.deleteByPrimaryKey(id);

			//删除规格选项表数据
            TbSpecificationOptionExample example=new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(example);
		}

	}
	
	
		@Override
	public PageResult search(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		TbSpecificationExample.Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {

		return specificationMapper.selectOptionList();
	}

}
