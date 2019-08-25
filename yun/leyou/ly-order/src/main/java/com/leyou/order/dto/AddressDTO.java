package com.leyou.order.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String name;//收货人姓名
    private String phone;//电话
    private String state;//省份
    private String city;//城市
    private String district;//区
    private String address;//街区地址
    private String zipCode;//邮政编码
    private Boolean isDefault;
}
