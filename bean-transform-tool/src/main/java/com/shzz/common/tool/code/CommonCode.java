/*
 * Copyright 2022 The bean-transform-tool Project
 *
 * The bean-transform-tool Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.shzz.common.tool.code;


/**
 * 通用错误枚举
 *
 * @author wen wang
 * @date 2022/2/12 21:30
 */
public enum CommonCode {
    /**
     * 集合或者数组最内层元素类型为null
     */
    ELEMENT_TYPE_NULL_EXCEPTION("0xff07", "集合或者数组最内层元素类型为null", "集合或者数组最内层元素类型无法解析"),
    /**
     * 类名为空异常
     */
    CLASS_NAME_NULL_EXCEPTION("0xff06", "类名为空", "请传入正确类名"),
    /**
     * 泛型实参类型不符合要求
     */
    GENERIC_TYPE_UNSUPPORT("0xff05", "泛型实参类型不符合要求", "泛型实参类型不符合要求"),
    /**
     * extendsTransformList参数不符合要求
     */
    EXTENDS_TRANSFORM_ERROR("0xff04", "extendsTransformList参数不符合要求", "extendsTransformList参数为空或缺少指定的转换类对象"),
    /**
     * 注册转换策略异常
     */
    STRATEGY_REGISTER_UNSUPPORT("0xff03", "注册转换策略异常", "注册转换策略优先级不正确"),
    /**
     * 类型不符合要求
     */
    TYPE_UNSUPPORT("0xff02", "类型不符合要求", "类型不符合要求"),
    /**
     * 类型不匹配
     */
    TYPE_MISMATCH("0xff01", "类型不匹配", "转换类型不满足匹配要求");

    /**
     * 错误代码
     */
    private String errorCode = "not specific";
    /**
     * 错误概要
     */
    private String errorOutline = "not specific";
    /**
     * 错误链细节
     */
    private String errorChainDetail = "not specific";

    /**
     * 通用代码
     *
     * @param errorCode        错误代码
     * @param errorOutline     错误概要
     * @param errorChainDetail 错误链细节
     */
    private CommonCode(String errorCode, String errorOutline, String errorChainDetail) {
        this.errorCode = errorCode;
        this.errorChainDetail = errorChainDetail;
        this.errorOutline = errorOutline;
    }

    /**
     * @return {@link String}
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @return {@link String}
     */
    public String getErrorOutline() {
        return errorOutline;
    }

    /**
     * @return {@link String}
     */
    public String getErrorChainDetail() {
        return errorChainDetail;
    }

}
