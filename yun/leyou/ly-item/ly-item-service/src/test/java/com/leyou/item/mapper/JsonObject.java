package com.leyou.item.mapper;

import lombok.Data;

import java.util.List;

@Data
public class JsonObject {

    private String group;//组名
    private List<Param> params;//param数组
    private Boolean empty;//未知，原来没加，然后空指针异常，在某条数据上发现的
}

@Data
class Param {
    private String k;//param名
    private Boolean searchable;//可否搜索
    private Boolean global;//是否通用属性参数
    private Boolean numerical;//是否是数字类型参数
    private String unit;//数字类型参数的单位，非数字类型可以为空
    private String v;//param值，后面用到

    private List<String> options;//待选项集合
}