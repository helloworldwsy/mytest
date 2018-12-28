package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.*;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
//        Query query=new SimpleQuery();
//       // 添加查询条件
//        Criteria criteria= new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//        ScoredPage<TbItem> page= solrTemplate.queryForPage(query, TbItem.class);
//        Map<String, Object> map=new HashMap<>();
//        map.put("rows", page.getContent());
//        //2.根据关键字查询商品分类
//        List categoryList = searchCategoryList(searchMap);
//        map.put("categoryList",categoryList);
//        return map;


       // 高亮的优化
        	Map<String,Object> map=new HashMap<>();
        	//空格处理
            searchMap.put("keywords",searchMap.get("keywords").toString().replace(" ",""));//关键字去掉空格
         	//1.查询列表
            map.putAll(searchList(searchMap));
            //2.根据关键字查询商品分类
            List categoryList = searchCategoryList(searchMap);
            map.put("categoryList",categoryList);
            //3.查询品牌和规格列表
        String category = (String) searchMap.get("category");
        if (!"".equals(category)){
                map.putAll(searchBrandAndSpecList(category));
            }else {
                if(categoryList.size()>0){
                    map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
                }
            }


          return map;



//        Map map=new HashMap();
//        HighlightQuery query=new SimpleHighlightQuery();
//        //设置高亮的域
//        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");
//        //高亮前缀
//        highlightOptions.setSimplePrefix("<em style='color:red'>");
//        //高亮后缀
//        highlightOptions.setSimplePostfix("</em>");
//        //设置高亮选项
//        query.setHighlightOptions(highlightOptions);
//        //按照关键字查询
//        Criteria criteria= new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//
//        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);
//
//        //循环高亮入口集合
//        for (HighlightEntry<TbItem> highlightEntry : page.getHighlighted()) {
//            //获取原实体类
//            TbItem item = highlightEntry.getEntity();
//            if (highlightEntry.getHighlights().size()>0 && highlightEntry.getHighlights().get(0).getSnipplets().size()>0){
//                //设置高亮的结果
//                item.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
//            }
//        }
//        map.put("rows",page.getContent());
//        return map;




    }

    @Override
    public void importList(List list) {
      // System.out.println(list.size());
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {

        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    private Map searchList(Map searchMap){
        Map map=new HashMap();
        HighlightQuery query=new SimpleHighlightQuery();
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项
        //1.1按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2按照分类筛选
        if (!"".equals(searchMap.get("category"))){
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3按照品牌筛选
        if (!"".equals(searchMap.get("brand"))){
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4规格过滤
        if (searchMap.get("spec")!=null){
            Map<String, String> spec = (Map<String, String>) searchMap.get("spec");
            for (String key : spec.keySet()) {
                Criteria filterCriteria=new Criteria("item_spec_"+key).is( spec.get(key) );
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }

        //1.5价格过滤
        if (!"".equals(searchMap.get("price"))){
            String[] price = ((String) searchMap.get("price")).split("-");
            if (!"0".equals(price[0])){//当开始区间不为0时执行（即如果为0，不用设置条件）
                FilterQuery filterQuery=new SimpleFilterQuery();
                //价格大于等于price[0]
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!"*".equals(price[1])){//当结束区间不为*时执行（即如果为*，不用设置条件）
                FilterQuery filterQuery=new SimpleFilterQuery();
                //价格小于等于price[1]
                Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }


        //1.6 分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo==null){
            pageNo=1;//默认第一页
        }
        Integer pageSize=(Integer) searchMap.get("pageSize");//每页记录数
        if(pageSize==null){
            pageSize=20;//默认 20
        }
        //开始查询索引数
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //1.7排序
        String sortValue= (String) searchMap.get("sort");//ASC升序  DESC 降序
        //排序字段
        String sortField= (String) searchMap.get("sortField");
        if (sortValue!=null&&!"".equals(sortValue)){
            if ("ASC".equals(sortValue)){
                Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if ("DESC".equals(sortValue)){
                Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        for(HighlightEntry<TbItem> h: page.getHighlighted()){//循环高亮入口集合
            TbItem item = h.getEntity();//获取原实体类
            if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0){
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮的结果
            }
        }
        map.put("rows",page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;
    }


    @Test
    public void test1() {

        Map map=new HashMap<>();
        HighlightQuery query=new SimpleHighlightQuery();
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项
        //按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is("手机");
        query.addCriteria(criteria);
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
    }

    private List<String> searchCategoryList(Map searchMap){
        List<String> list=new ArrayList();
        Query query=new SimpleQuery();
        //按照关键字查询
        Criteria criteria= new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组集合
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            //将分组结果的名称封装到返回值中
            list.add(tbItemGroupEntry.getGroupValue());
        }
        return list;
    }


    @Autowired
    private RedisTemplate redisTemplate;

    /**
     查询品牌和规格列表
     @param category 分类名称
     @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        //获取模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null ){
            //根据模板id查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);

            map.put("brandList",brandList);//返回值添加品牌列表
            //根据模板id查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);

            map.put("specList",specList);
        }

        return map;
    }


}
