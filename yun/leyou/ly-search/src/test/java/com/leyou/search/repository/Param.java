package com.leyou.search.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class Param {
    private String group;

    @JsonIgnore
    private List<Value> params;
}

@Data
class Value{
    private String k;
    private Boolean searchable;
    private Boolean global;
    private Boolean numerical;
    private String unit;
    private String v;

    private List<String> options;
}