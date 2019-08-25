package com.leyou.search.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodRepositoryTest {

    @Autowired
    private GoodRepository repository;
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;

    @Test
    public void testCreateIndex(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void test(){
       /* String s = "[{\"group\":\"主体\",\"params\":[{\"k\":\"品牌\",\"searchable\":false,\"global\":true,\"v\":null},{\"k\":\"型号\",\"searchable\":false,\"global\":true,\"v\":\"G9青春版（全网通版）\"},{\"k\":\"上市年份\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"年\",\"v\":2016.0}]},{\"group\":\"基本信息\",\"params\":[{\"k\":\"机身颜色\",\"searchable\":false,\"global\":false,\"options\":[\"白色\",\"金色\",\"玫瑰金\"]},{\"k\":\"机身重量（g）\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"g\",\"v\":143},{\"k\":\"机身材质工艺\",\"searchable\":true,\"global\":true,\"v\":null}]},{\"group\":\"操作系统\",\"params\":[{\"k\":\"操作系统\",\"searchable\":true,\"global\":true,\"v\":\"Android\"}]},{\"group\":\"主芯片\",\"params\":[{\"k\":\"CPU品牌\",\"searchable\":true,\"global\":true,\"v\":\"骁龙（Snapdragon)\"},{\"k\":\"CPU型号\",\"searchable\":false,\"global\":true,\"v\":\"骁龙617（msm8952）\"},{\"k\":\"CPU核数\",\"searchable\":true,\"global\":true,\"v\":\"八核\"},{\"k\":\"CPU频率\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"GHz\",\"v\":1.5}]},{\"group\":\"存储\",\"params\":[{\"k\":\"内存\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"3GB\"]},{\"k\":\"机身存储\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"16GB\"]}]},{\"group\":\"屏幕\",\"params\":[{\"k\":\"主屏幕尺寸（英寸）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"英寸\",\"v\":5.2},{\"k\":\"分辨率\",\"searchable\":false,\"global\":true,\"v\":\"1920*1080(FHD)\"}]},{\"group\":\"摄像头\",\"params\":[{\"k\":\"前置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":800.0},{\"k\":\"后置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":1300.0}]},{\"group\":\"电池信息\",\"params\":[{\"k\":\"电池容量（mAh）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"mAh\",\"v\":3000.0}]}]";
        Map<String, String> map = JsonUtils.parseMap(s, String.class, String.class);
        System.out.println("map = " + map);

        s = "{\"机身颜色\":[\"白色\",\"金色\",\"玫瑰金\"],\"内存\":[\"3GB\"],\"机身存储\":[\"16GB\"]}";
        Map<String, List<String>> listMap = JsonUtils.nativeRead(s, new TypeReference<Map<String, List<String>>>() {
        });
        System.out.println("listMap = " + listMap);*/

        System.out.println("d = " + (186.0 / 20));
        double ceil = Math.ceil(186.0 / 20);
        System.out.println("ceil = " + ceil);

        String s = "  ";
        boolean b = s.isEmpty();
        System.out.println("b = " + b);

        b = StringUtils.isBlank(s);
        System.out.println("b = " + b);

        b = (s == null);
        System.out.println("b = " + b);

        /*s = null;
        b = s.isEmpty();
        System.out.println("b = " + b);*/
    }

    @Test
    public void loadData(){
        int page = 1;
        int rows = 200;
        int size = 0;
        do {
            //查询spu信息
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);
            List<Spu> spuList = result.getItems();
            if (CollectionUtils.isEmpty(spuList)){
                break;
            }
            //构建城goods
//            List<Goods> goodsList = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());
            List<Goods> goodsList = new ArrayList<>();
            for (Spu spu : spuList) {
                if (spu.getId() != 207 && spu.getId() != 206 && spu.getId() != 218 && spu.getId() != 219) {
                    Goods goods = searchService.buildGoods(spu);
                    goodsList.add(goods);
                }
            }
            //存入索引库
            repository.saveAll(goodsList);

            //翻页
            page++;
            size = spuList.size();
        }while (size == 100);
    }

    @Test
    public void test2(){
        String s = "[{\"group\":\"主体\",\"params\":[{\"k\":\"品牌\",\"searchable\":false,\"global\":true,\"v\":null},{\"k\":\"型号\",\"searchable\":false,\"global\":true,\"v\":\"CAZ-AL10\"},{\"k\":\"上市年份\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"年\",\"v\":2016.0}]},{\"group\":\"基本信息\",\"params\":[{\"k\":\"机身颜色\",\"searchable\":false,\"global\":false,\"options\":[\"香槟金（白）\",\"玫瑰金\"]},{\"k\":\"机身重量（g）\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"g\",\"v\":146},{\"k\":\"机身材质工艺\",\"searchable\":true,\"global\":true,\"v\":null}]},{\"group\":\"操作系统\",\"params\":[{\"k\":\"操作系统\",\"searchable\":true,\"global\":true,\"v\":\"Android\"}]},{\"group\":\"主芯片\",\"params\":[{\"k\":\"CPU品牌\",\"searchable\":true,\"global\":true,\"v\":\"骁龙（Snapdragon)\"},{\"k\":\"CPU型号\",\"searchable\":false,\"global\":true,\"v\":\"骁龙625（MSM8953）\"},{\"k\":\"CPU核数\",\"searchable\":true,\"global\":true,\"v\":\"八核\"},{\"k\":\"CPU频率\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"GHz\",\"v\":2.0}]},{\"group\":\"存储\",\"params\":[{\"k\":\"内存\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"4GB\"]},{\"k\":\"机身存储\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"64GB\"]}]},{\"group\":\"屏幕\",\"params\":[{\"k\":\"主屏幕尺寸（英寸）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"英寸\",\"v\":5.0},{\"k\":\"分辨率\",\"searchable\":false,\"global\":true,\"v\":\"1920*1080(FHD)\"}]},{\"group\":\"摄像头\",\"params\":[{\"k\":\"前置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":800.0},{\"k\":\"后置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":1200.0}]},{\"group\":\"电池信息\",\"params\":[{\"k\":\"电池容量（mAh）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"mAh\",\"v\":3020}]}]";
//        String s = "[{\"group\":\"主体\",\"params\":[{\"k\":\"品牌\",\"searchable\":false,\"global\":true,\"v\":null},{\"k\":\"型号\",\"searchable\":false,\"global\":true,\"v\":\"CAZ-AL10\"},{\"k\":\"上市年份\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"年\",\"v\":2016.0}]}]";

        s = "[{\"group\":\"主体\",\"params\":[{\"k\":\"品牌\",\"searchable\":false,\"global\":true,\"v\":\"锤子（smartisan）\"},{\"k\":\"型号\",\"searchable\":false,\"global\":true,\"v\":\"坚果PRO\"},{\"k\":\"上市年份\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"年\",\"v\":2017.0}]},{\"group\":\"基本信息\",\"params\":[{\"k\":\"机身颜色\",\"searchable\":false,\"global\":false,\"options\":[\"细红线特别版\",\"碳黑色\",\"巧克力色\",\"酒红色\",\"浅金色\"]},{\"k\":\"机身重量（g）\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"g\",\"v\":158},{\"k\":\"机身材质工艺\",\"searchable\":true,\"global\":true,\"v\":null}]},{\"group\":\"操作系统\",\"params\":[{\"k\":\"操作系统\",\"searchable\":true,\"global\":true,\"v\":\"Android\"}]},{\"group\":\"主芯片\",\"params\":[{\"k\":\"CPU品牌\",\"searchable\":true,\"global\":true,\"v\":\"骁龙（Snapdragon)\"},{\"k\":\"CPU型号\",\"searchable\":false,\"global\":true,\"v\":\"骁龙626\"},{\"k\":\"CPU核数\",\"searchable\":true,\"global\":true,\"v\":\"八核\"},{\"k\":\"CPU频率\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"GHz\",\"v\":2.2}]},{\"group\":\"存储\",\"params\":[{\"k\":\"内存\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"4GB\"]},{\"k\":\"机身存储\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"128GB\",\"64GB\"]}]},{\"group\":\"屏幕\",\"params\":[{\"k\":\"主屏幕尺寸（英寸）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"英寸\",\"v\":5.5},{\"k\":\"分辨率\",\"searchable\":false,\"global\":true,\"v\":\"1920*1080(FHD)\"}]},{\"group\":\"摄像头\",\"params\":[{\"k\":\"前置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":1600.0},{\"k\":\"后置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":1300}]},{\"group\":\"电池信息\",\"params\":[{\"k\":\"电池容量（mAh）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"mAh\",\"v\":3500}]}]";

        s = "[{\"group\":\"主体\",\"params\":[{\"k\":\"品牌\",\"searchable\":false,\"global\":true,\"v\":\"华为（HUAWEI）\"},{\"k\":\"型号\",\"searchable\":false,\"global\":true,\"v\":\"ALP-AL00\"},{\"k\":\"上市年份\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"年\",\"v\":2017.0}]},{\"group\":\"基本信息\",\"params\":[{\"k\":\"机身颜色\",\"searchable\":false,\"global\":false,\"options\":[\"亮黑色\",\"樱粉金\",\"香槟金\",\"摩卡金\"]},{\"k\":\"机身重量（g）\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"g\",\"v\":186},{\"k\":\"机身材质工艺\",\"searchable\":true,\"global\":true,\"v\":\"四曲面玻璃后壳\"}]},{\"group\":\"操作系统\",\"params\":[{\"k\":\"操作系统\",\"searchable\":true,\"global\":true,\"v\":\"Android\"}]},{\"group\":\"主芯片\",\"params\":[{\"k\":\"CPU品牌\",\"searchable\":true,\"global\":true,\"v\":\"海思（Hisilicon）\"},{\"k\":\"CPU型号\",\"searchable\":false,\"global\":true,\"v\":\"麒麟970\"},{\"k\":\"CPU核数\",\"searchable\":true,\"global\":true,\"v\":\"八核\"},{\"k\":\"CPU频率\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"GHz\",\"v\":2.36}]},{\"group\":\"存储\",\"params\":[{\"k\":\"内存\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"6GB\"]},{\"k\":\"机身存储\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"128GB\"]}]},{\"group\":\"屏幕\",\"params\":[{\"k\":\"主屏幕尺寸（英寸）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"英寸\",\"v\":5.9},{\"k\":\"分辨率\",\"searchable\":false,\"global\":true,\"v\":\"2560×1440（Quad HD / 2K ）\"}]},{\"group\":\"摄像头\",\"params\":[{\"k\":\"前置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":800.0},{\"k\":\"后置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":1200.0}]},{\"group\":\"电池信息\",\"params\":[{\"k\":\"电池容量（mAh）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"mAh\",\"v\":4000}]}]";

        List<Param> es = JsonUtils.parseList(s, Param.class );
        System.out.println("es = " + es);

    }

    @Test
    public void test3(){
        String s = "[{\"group\":\"主体\",\"params\":[{\"k\":\"品牌\",\"searchable\":false,\"global\":true,\"v\":null},{\"k\":\"型号\",\"searchable\":false,\"global\":true,\"v\":\"G9青春版（全网通版）\"},{\"k\":\"上市年份\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"年\",\"v\":2016.0}]},{\"group\":\"基本信息\",\"params\":[{\"k\":\"机身颜色\",\"searchable\":false,\"global\":false,\"options\":[\"白色\",\"金色\",\"玫瑰金\"]},{\"k\":\"机身重量（g）\",\"searchable\":false,\"global\":true,\"numerical\":true,\"unit\":\"g\",\"v\":143},{\"k\":\"机身材质工艺\",\"searchable\":true,\"global\":true,\"v\":null}]},{\"group\":\"操作系统\",\"params\":[{\"k\":\"操作系统\",\"searchable\":true,\"global\":true,\"v\":\"Android\"}]},{\"group\":\"主芯片\",\"params\":[{\"k\":\"CPU品牌\",\"searchable\":true,\"global\":true,\"v\":\"骁龙（Snapdragon)\"},{\"k\":\"CPU型号\",\"searchable\":false,\"global\":true,\"v\":\"骁龙617（msm8952）\"},{\"k\":\"CPU核数\",\"searchable\":true,\"global\":true,\"v\":\"八核\"},{\"k\":\"CPU频率\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"GHz\",\"v\":1.5}]},{\"group\":\"存储\",\"params\":[{\"k\":\"内存\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"3GB\"]},{\"k\":\"机身存储\",\"searchable\":true,\"global\":false,\"numerical\":false,\"unit\":\"GB\",\"options\":[\"16GB\"]}]},{\"group\":\"屏幕\",\"params\":[{\"k\":\"主屏幕尺寸（英寸）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"英寸\",\"v\":5.2},{\"k\":\"分辨率\",\"searchable\":false,\"global\":true,\"v\":\"1920*1080(FHD)\"}]},{\"group\":\"摄像头\",\"params\":[{\"k\":\"前置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":800.0},{\"k\":\"后置摄像头\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"万\",\"v\":1300.0}]},{\"group\":\"电池信息\",\"params\":[{\"k\":\"电池容量（mAh）\",\"searchable\":true,\"global\":true,\"numerical\":true,\"unit\":\"mAh\",\"v\":3000.0}]}]";

        Map<Long, String> longStringMap = JsonUtils.parseMap(s, Long.class, String.class);
        System.out.println("longStringMap = " + longStringMap);

        s = "{\"机身颜色\":[\"白色\",\"金色\",\"玫瑰金\"],\"内存\":[\"3GB\"],\"机身存储\":[\"16GB\"]}";
        Map<Long, List<String>> longStringMap1 = JsonUtils.nativeRead(s, new TypeReference<Map<Long, List<String>>>() {
        });
        System.out.println("longStringMap1 = " + longStringMap1);
    }

    @Test
    public void test04(){
        String s = "";
        boolean b = s == null;
        System.out.println("b = " + b);

        b = s == "";
        System.out.println("b = " + b);

        s = null;
//        b = StringUtils.equals(s,"");
//        b = s.equals("");
        System.out.println("b = " + b);
    }

    @Test
    public void test05(){
       /* String s = "10";
        Integer integer = Integer.valueOf(s);
        System.out.println("integer = " + integer);*/
        Date date = DateTime.now().plusHours(1).toDate();
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        System.out.println("format = " + format);

    }

}