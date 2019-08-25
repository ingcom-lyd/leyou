package com.leyou.item.mapper;

import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Specification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpecparamMapperTest {
    @Autowired
    private SpecParamMapper specParamMapper;
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecificationMapper specificationMapper;

    @Test
    public void test(){
        //从表‘tb_specification’获取规格参数集合
        List<Specification> specifications = specificationMapper.selectAll();
        int count = 0;
        //遍历规格参数集合
        for (Specification specification : specifications) {
            //获取具体规格参数
            String spec = specification.getSpec();
            //将规格参数转为对象
            List<JsonObject> jsonObjects = JsonUtils.parseList(spec, JsonObject.class);
            for (JsonObject jsonObject : jsonObjects) {
                String group = jsonObject.getGroup();
                SpecGroup specGroup = new SpecGroup();
                specGroup.setName(group);
                //获取规格参数group
                List<SpecGroup> groupList = specGroupMapper.select(specGroup);
                //组id
                Long groupId = null;
                //因为specGroup有两个相同组名，需要进行判断
                if (groupList.size() == 1){
                    groupId = groupList.get(0).getId();
                }else {
                    if (count == 0){
                        count++;
                        groupId = groupList.get(0).getId();
                    }else {
                        groupId = groupList.get(1).getId();
                    }
                }
                //遍历规格参数params
                for (Param param : jsonObject.getParams()) {
                    SpecParam specParam = new SpecParam();

                    specParam.setId(null);
                    specParam.setCid(specification.getCategoryId());
                    specParam.setGroupId(groupId);

                    specParam.setName(param.getK());
                    //有些数据是空的需要先过滤一下
                    specParam.setGeneric(param.getGlobal() == null ? false : param.getGlobal());
                    specParam.setNumeric(param.getNumerical() == null ? false : param.getNumerical());
                    specParam.setSearching(param.getSearchable());
                    specParam.setUnit(param.getUnit() == null ? "" : param.getUnit());
                    //表‘tb_specification’中没有这个数据，先设置为空字符串，后面再手动设置
                    specParam.setSegments("");

                    int insert = specParamMapper.insert(specParam);
                    System.out.println("insert = " + insert);
                }
            }
        }
    }

}