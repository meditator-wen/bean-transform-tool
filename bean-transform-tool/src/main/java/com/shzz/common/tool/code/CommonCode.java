package com.shzz.common.tool.code;

/**
 * @Classname CommonCode
 * @Description errorChainDetail
 * @Date 2022/2/12 21:30
 * @Created by wen wang
 */
public enum CommonCode {
    ELEMENT_TYPE_NULL_EXCEPTION("0xff07", "集合或者数组最内层元素类型为null", "集合或者数组最内层元素类型无法解析"),
    CLASS_NAME_NULL_EXCEPTION("0xff06", "类名为空", "请传入正确类名"),
    GENERIC_TYPE_UNSUPPORT("0xff05", "泛型实参类型不符合要求", "泛型实参类型不符合要求"),
    EXTENDS_TRANSFORM_ERROR("0xff04", "extendsTransformList参数不符合要求", "extendsTransformList参数为空或缺少指定的转换类对象"),
    STRATEGY_REGISTER_UNSUPPORT("0xff03", "注册转换策略异常", "注册转换策略优先级不正确"),
    TYPE_UNSUPPORT("0xff02", "类型不符合要求", "类型不符合要求"),
    TYPE_MISMATCH("0xff01", "类型不匹配", "转换类型不满足匹配要求");

    private String errorCode = "not specific";
    private String errorOutline = "not specific";
    private String errorChainDetail = "not specific";

    private CommonCode(String errorCode, String errorOutline, String errorChainDetail) {
        this.errorCode = errorCode;
        this.errorChainDetail = errorChainDetail;
        this.errorOutline = errorOutline;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorOutline() {
        return errorOutline;
    }

    public String getErrorChainDetail() {
        return errorChainDetail;
    }

}
