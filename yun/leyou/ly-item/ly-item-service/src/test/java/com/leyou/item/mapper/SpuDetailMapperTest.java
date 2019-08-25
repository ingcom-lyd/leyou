package com.leyou.item.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.SpuDetail;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpuDetailMapperTest {

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Test
    public void test(){
        List<SpuDetail> spuDetails = spuDetailMapper.selectAll();

        for (SpuDetail spuDetail : spuDetails) {
            String specifications = spuDetail.getSpecifications();
            if (! specifications.equals("[]")){
                List<JsonObject> jsonObjects = JsonUtils.parseList(specifications, JsonObject.class);
                System.out.println("specifications = " + specifications);
                System.out.println("jsonObjects = " + jsonObjects);

                Map<Integer,String> map = new HashMap<>();

                Integer x = 1;
                for (JsonObject jsonObject : jsonObjects) {

                    List<Param> p = jsonObject.getParams();

                    for (int i = 0;i < p.size();i++ ){
                        Integer key = x++;
                        String v = p.get(i).getV();
                        if (StringUtils.isBlank(v)) {
                            v = "其他";
                        }
                        if (key != 4 && key != 12 && key != 13){
                            map.put(key,v);
                        }else{

                        }
                    }
                }

                String s = JsonUtils.serialize(map);

                spuDetail.setGenericSpec(s);

                int i = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);

                System.out.println("i = " + i);
                System.out.println("--------------------------------------------------------------------------------------------");
            }
        }
//        System.out.println("spuDetails = " + spuDetails);

        /*SpuDetail spuDetail = new SpuDetail();
        spuDetail.setSpuId(218L);
        SpuDetail spuDetail1 = spuDetailMapper.selectByPrimaryKey(spuDetail);
        String specifications = spuDetail.getSpecifications();
        System.out.println("specifications = " + specifications);
        boolean notBlank = StringUtils.isNotBlank(specifications);
        System.out.println("notBlank = " + notBlank);*/
    }

    @Test
    public void test2(){
        List<SpuDetail> spuDetails = spuDetailMapper.selectAll();

        for (SpuDetail spuDetail : spuDetails) {
            String specifications = spuDetail.getSpecifications();
            String specTemplate = spuDetail.getSpecTemplate();

            if (! specifications.equals("[]")){
                Map<String, List<String>> stringMap = JsonUtils.nativeRead(specTemplate, new TypeReference<Map<String,List<String>>>() {
                });
                System.out.println("stringMap = " + stringMap);

                List<JsonObject> jsonObjects = JsonUtils.parseList(specifications, JsonObject.class);

                Map<Integer,List<String>> map = new HashMap<>();

                Integer x = 1;
                for (JsonObject jsonObject : jsonObjects) {
//                    System.out.println("jsonObject = " + jsonObject);

                    List<Param> p = jsonObject.getParams();

                    for (int i = 0;i < p.size();i++ ){
                        Integer key = x++;
                        String v = p.get(i).getV();
                        if (StringUtils.isBlank(v)) {
                            v = "其他";
                        }
                        if (key == 4 && p.get(i).getK().equals("机身颜色")){
                            List<String> value = stringMap.get("机身颜色");
                            map.put(key,value);
                        }
                        if (key == 12 && p.get(i).getK().equals("内存")){
                            List<String> value = stringMap.get("内存");
                            map.put(key,value);
                        }
                        if (key == 13 && p.get(i).getK().equals("机身存储")){
                            List<String> value = stringMap.get("机身存储");
                            map.put(key,value);
                        }
                        if (key == 2 && p.get(i).getK().equals("适用机型")){
                            List<String> value = stringMap.get("适用机型");
                            map.put(key,value);
                        }
                        if (key == 2 && p.get(i).getK().equals("型号") && jsonObjects.size() == 2){
                            List<String> value = stringMap.get("型号");
                            map.put(key,value);
                        }
                        if (key == 6 && p.get(i).getK().equals("屏幕尺寸")){
                            List<String> value = stringMap.get("屏幕尺寸");
                            map.put(key,value);
                        }
                    }
                }

//                System.out.println("map = " + map);
                String s = JsonUtils.serialize(map);

                spuDetail.setSpecialSpec(s);
//
                int i = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);

                System.out.println("i = " + i);
                System.out.println("--------------------------------------------------------------------------------------------");
            }
        }
    }

    @Test
    public void test02(){
        List<SpuDetail> spuDetails = spuDetailMapper.selectAll();

       /* for (SpuDetail spuDetail : spuDetails) {
            String special = spuDetail.getSpecial();
            System.out.println("special = " + special);
            String generic = spuDetail.getGeneric();
            System.out.println("generic = " + generic);
            System.out.println("-------------------------------------------------------------");
        }*/

        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(215);
        System.out.println("spuDetail = " + spuDetail);
    }
}