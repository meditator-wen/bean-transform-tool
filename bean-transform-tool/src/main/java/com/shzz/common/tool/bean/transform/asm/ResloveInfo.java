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
package com.shzz.common.tool.bean.transform.asm;

import java.lang.reflect.Field;
import java.lang.reflect.Type;


/**
 * 解决信息
 *
 * @author wen wang
 * @date 2021/11/19 17:40
 */
public class ResloveInfo {
    /**
     * 扩展对象变换impl类
     */
    private String extensionObjectTransformImplClass="";
    /**
     * 用户扩展
     */
    private boolean userExtend=false;

    /**
     * 自动转换
     */
    private boolean autoTransform=true;

    /**
     * 目标字段名称
     */
    private String targetFieldName;
    /**
     * 目标字段设置函数名
     */
    private String targetFieldSetFunctionName;
    /**
     * 目标字段集函数可用
     */
    private boolean targetFieldSetFunctionAvailable = false;
    /**
     * 目标字段集函数描述符
     */
    private String targetFieldSetFunctionDescriptor;

    /**
     * 源字段名
     */
    private String sourceFieldName;
    /**
     * 源字段类型
     */
    private Class<?> sourceFieldType;
    /**
     * 源领域
     */
    private Field sourceField;
    /**
     * 源领域得到函数名
     */
    private String sourceFieldGetFunctionName;
    /**
     * 源领域得到可用函数名
     */
    private boolean sourceFieldGetFunctionNameAvailable = false;
    /**
     * 源领域得到函数描述符
     */
    private String sourceFieldGetFunctionDescriptor;

    /**
     * 源字段类型内部名字
     */
    private String sourceFieldTypeInternalName;

    /**
     * 得到目标字段名称
     *
     * @return {@link String}
     */
    public String getTargetFieldName() {
        return targetFieldName;
    }

    /**
     * 设置目标字段名称
     *
     * @param targetFieldName 目标字段名称
     */
    public void setTargetFieldName(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    /**
     * 获取目标字段设置函数名
     *
     * @return {@link String}
     */
    public String getTargetFieldSetFunctionName() {
        return targetFieldSetFunctionName;
    }

    /**
     * 目标字段集合函数名
     *
     * @param targetFieldSetFunctionName 目标字段设置函数名
     */
    public void setTargetFieldSetFunctionName(String targetFieldSetFunctionName) {
        this.targetFieldSetFunctionName = targetFieldSetFunctionName;
    }

    /**
     * 目标字段集函数可用吗
     *
     * @return boolean
     */
    public boolean isTargetFieldSetFunctionAvailable() {
        return targetFieldSetFunctionAvailable;
    }

    /**
     * 目标字段集合函数可用
     *
     * @param targetFieldSetFunctionAvailable 目标字段集函数可用
     */
    public void setTargetFieldSetFunctionAvailable(boolean targetFieldSetFunctionAvailable) {
        this.targetFieldSetFunctionAvailable = targetFieldSetFunctionAvailable;
    }

    /**
     * 得到源字段名
     *
     * @return {@link String}
     */
    public String getSourceFieldName() {
        return sourceFieldName;
    }

    /**
     * 设置源字段名
     *
     * @param sourceFieldName 源字段名
     */
    public void setSourceFieldName(String sourceFieldName) {
        this.sourceFieldName = sourceFieldName;
    }

    /**
     * 得到源领域得到函数名字
     *
     * @return {@link String}
     */
    public String getSourceFieldGetFunctionName() {
        return sourceFieldGetFunctionName;
    }

    /**
     * 设置源字段获取函数名
     *
     * @param sourceFieldGetFunctionName 源领域得到函数名
     */
    public void setSourceFieldGetFunctionName(String sourceFieldGetFunctionName) {
        this.sourceFieldGetFunctionName = sourceFieldGetFunctionName;
    }

    /**
     * 源领域得到函数名
     *
     * @return boolean
     */
    public boolean isSourceFieldGetFunctionNameAvailable() {
        return sourceFieldGetFunctionNameAvailable;
    }

    /**
     * 设置源字段获取函数名
     *
     * @param sourceFieldGetFunctionNameAvailable 源领域得到可用函数名
     */
    public void setSourceFieldGetFunctionNameAvailable(boolean sourceFieldGetFunctionNameAvailable) {
        this.sourceFieldGetFunctionNameAvailable = sourceFieldGetFunctionNameAvailable;
    }

    /**
     * 得到扩展对象变换impl类
     *
     * @return {@link String}
     */
    public String getExtensionObjectTransformImplClass() {
        return extensionObjectTransformImplClass;
    }

    /**
     * 设置扩展对象变换impl类
     *
     * @param extensionObjectTransformImplClass 扩展对象变换impl类
     */
    public void setExtensionObjectTransformImplClass(String extensionObjectTransformImplClass) {
        this.extensionObjectTransformImplClass = extensionObjectTransformImplClass;
    }

    /**
     * 获取目标字段设置函数描述符
     *
     * @return {@link String}
     */
    public String getTargetFieldSetFunctionDescriptor() {
        return targetFieldSetFunctionDescriptor;
    }

    /**
     * 目标字段集合函数描述符
     *
     * @param targetFieldSetFunctionDescriptor 目标字段集函数描述符
     */
    public void setTargetFieldSetFunctionDescriptor(String targetFieldSetFunctionDescriptor) {
        this.targetFieldSetFunctionDescriptor = targetFieldSetFunctionDescriptor;
    }


    /**
     * 获得源字段描述符函数
     *
     * @return {@link String}
     */
    public String getSourceFieldGetFunctionDescriptor() {
        return sourceFieldGetFunctionDescriptor;
    }

    /**
     * 设置源字段获取函数描述符
     *
     * @param sourceFieldGetFunctionDescriptor 源领域得到函数描述符
     */
    public void setSourceFieldGetFunctionDescriptor(String sourceFieldGetFunctionDescriptor) {
        this.sourceFieldGetFunctionDescriptor = sourceFieldGetFunctionDescriptor;
    }

    /**
     * 源字段类型内部名称
     *
     * @return {@link String}
     */
    public String getSourceFieldTypeInternalName() {
        return sourceFieldTypeInternalName;
    }

    /**
     * 设置源字段类型内部名字
     *
     * @param sourceFieldTypeInternalName 源字段类型内部名字
     */
    public void setSourceFieldTypeInternalName(String sourceFieldTypeInternalName) {
        this.sourceFieldTypeInternalName = sourceFieldTypeInternalName;
    }

    /**
     * 得到源字段类型
     *
     * @return {@link Class}
     */
    public Class<?> getSourceFieldType() {
        return sourceFieldType;
    }

    /**
     * 设置源字段类型
     *
     * @param sourceFieldType 源字段类型
     */
    public void setSourceFieldType(Class<?> sourceFieldType) {
        this.sourceFieldType = sourceFieldType;
    }

    /**
     * 得到来源字段
     *
     * @return {@link Field}
     */
    public Field getSourceField() {
        return sourceField;
    }

    /**
     * 设置源字段
     *
     * @param sourceField 源领域
     */
    public void setSourceField(Field sourceField) {
        this.sourceField = sourceField;
    }

    /**
     * 用户扩展
     *
     * @return boolean
     */
    public boolean isUserExtend() {
        return userExtend;
    }

    /**
     * 设置用户扩展
     *
     * @param userExtend 用户扩展
     */
    public void setUserExtend(boolean userExtend) {
        this.userExtend = userExtend;
    }

    /**
     * 是自动转换
     *
     * @return boolean
     */
    public boolean isAutoTransform() {
        return autoTransform;
    }

    /**
     * 设置自动变换
     *
     * @param autoTransform 自动转换
     */
    public void setAutoTransform(boolean autoTransform) {
        this.autoTransform = autoTransform;
    }
}
