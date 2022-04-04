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
package com.shzz.common.tool.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;


/**
 * userExtend 标注是否用户自定义转换，默认为false
 * autoTransform  标注是否自动转换
 * extensionObjectTransformImplClass 自定义转换实现类名称，继承ExtensionObjectTransform接口
 * getFunctionName  字段对应的get 方法名，如果不设置，
 * 默认值是 前缀get+字段名称首字母大写+字段名称除首字母外其他字符,
 * 如果是boolean 型，默认前缀是is， 不是get(idea 生成boolean get方法的默认规则)
 *  setFunctionName  字段对应的set 方法名，如果不设置，
 *  默认值是 set+字段名称首字母大写+字段名称除首字母外其他字符
 *  sourceFieldName  本类字段对应的源类字段名称，在字段名不一致的情况下需要制定，默认源类字段和本类字段同名
 *  description   字段描述信息
 *
 * @author wen wang
 * @date 2021/9/13 22:54
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanFieldInfo {
    /**
     * 标注是否用户自定义转换，默认为false，
     * 为true 说明该字段由用户自定义实现，且需要同步设置extensionObjectTransformImplClass 属性，标志拓展类的全路径，工具内部反射生成对象
     * @return boolean
     */
    boolean userExtend() default false;

    /**
     * 自动转换
     *
     * @return boolean
     */
    boolean autoTransform() default true;

    /**
     * 扩展对象变换impl类
     *
     * @return {@link String}
     */
    String extensionObjectTransformImplClass() default "";

    /**
     * 把函数名
     *
     * @return {@link String}
     */
    String getFunctionName() default "";

    /**
     * 设置函数名
     *
     * @return {@link String}
     */
    String setFunctionName() default "";

    /**
     * 源字段名
     *
     * @return {@link String}
     */
    String sourceFieldName() default "";

    /**
     * 描述
     *
     * @return {@link String}
     */
    String description() default "";

}
