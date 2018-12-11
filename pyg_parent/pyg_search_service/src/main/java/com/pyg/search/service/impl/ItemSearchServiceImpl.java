package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
         	//1.查询列表
            map.putAll(searchList(searchMap));
            //2.根据关键字查询商品分类
            List categoryList = searchCategoryList(searchMap);
            map.put("categoryList",categoryList);
            //3.查询品牌和规格列表
           if(categoryList.size()>0){
            map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
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

    private Map searchList(Map searchMap){
        Map map=new HashMap();
        HighlightQuery query=new SimpleHighlightQuery();
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项
        //按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        for(HighlightEntry<TbItem> h: page.getHighlighted()){//循环高亮入口集合
            TbItem item = h.getEntity();//获取原实体类
            if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0){
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮的结果
            }
        }
        map.put("rows",page.getContent());
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
