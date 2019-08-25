package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {

    BRAND_NOT_FOUND(404,"品牌不存在"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    BRAND_DELETE_ERROR(500,"品牌删除失败"),

    CATEGORY_NOT_FOUND(404,"商品分类没查到"),

    SPEC_NOT_FOUND(404,"商品规格没查到"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格group没查到"),
    SPEC_SAVA_ERROR(500,"商品规格新增失败"),
    SPEC_UPDATE_ERROR(500,"商品规格更新失败"),
    SPEC_DELETE_ERROR(500,"商品规格删除失败"),

    SPEC_PARAM_NOT_FOUND(404,"商品param没查到"),

    GOODS_NOT_FOUND(404,"商品不存在"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_DETAIL_NOT_FOUND(404,"商品详情不存在"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU不存在"),
    GOODS_STOCK_NOT_FOUND(404,"商品STOCK不存在"),
    GOODS_UPDATE_ERROR(500,"商品更新失败"),
    GOODS_ID_CANNOT_BE_NULL(500,"商品id不能为空"),
    GOODS_DELETE_ERROR(500,"商品删除失败"),
    GOODS_DOWN_SHELF_ERROR(500,"商品下架失败"),
    STOCK_NOT_ENOUGH(500,"库存不足"),

    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(400,"无效的文件类型"),

    INVALID_USER_DADA_TYPE(400,"无效的用户数据类型"),
    INVALID_VERIFY_CODE(400,"无效的验证码"),
    INVALID_USERNAME_PASSWORD(400,"无效的用户名和密码"),

    CREATE_TOKEN_ERROR(400,"用户凭证生成失败"),
    UN_AUTHORIZED(403,"未授权"),

    CART_NOT_FOUND(404,"购物车为空"),

    CREATE_ORDER_ERROR(500,"创建订单失败"),
    ORDER_NOT_FOUND(404,"订单不存在"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情不存在"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态不存在"),
    ORDER_STATUS_ERROR(400,"订单状态不正确"),
    UPDATE_ORDER_STATUS_ERROR(500,"更新订单状态失败"),

    WX_PAY_ORDER_FAIL(500,"微信下单失败"),

    INVALID_SIGN_ERROR(400,"无效的签名异常"),
    INVALID_ORDER_PARAM(400,"订单参数异常"),
    ;
    private int code;
    private String msg;
}
