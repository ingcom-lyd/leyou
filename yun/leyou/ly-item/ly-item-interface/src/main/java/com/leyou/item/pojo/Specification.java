package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
//@Table(name = "tb_spec_group")
@Table(name = "tb_specification")
public class Specification {

//    @Id
//    @KeySql(useGeneratedKeys = true)
//    private Long id;
//
//    private Long cid;
//
//    private String name;
    @Id
    private Long categoryId;
    @Column(name = "specifications")
    private String spec;
}
