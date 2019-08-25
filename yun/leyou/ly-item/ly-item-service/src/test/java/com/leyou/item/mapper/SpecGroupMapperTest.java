package com.leyou.item.mapper;

import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.Specification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpecGroupMapperTest {

    @Autowired
    private SpecificationMapper specificationMapper;
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Test
    public void test(){
        //从表‘tb_specification’获取规格参数集合
        List<Specification> specifications = specificationMapper.selectAll();

        //遍历规格参数集合
        for (Specification specification : specifications) {
            //获取具体规格参数
            String spec = specification.getSpec();
            //将规格参数转为对象
            List<JsonObject> jsonObjects = JsonUtils.parseList(spec, JsonObject.class);
            for (JsonObject jsonObject : jsonObjects) {
                String group = jsonObject.getGroup();

                SpecGroup specGroup = new SpecGroup();
                specGroup.setId(null);
                specGroup.setCid(specification.getCategoryId());
                specGroup.setName(group);

                int insert = specGroupMapper.insert(specGroup);
                System.out.println("insert = " + insert);
            }
        }
    }
}