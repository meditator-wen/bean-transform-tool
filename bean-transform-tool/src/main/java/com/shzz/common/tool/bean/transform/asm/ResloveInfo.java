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
 *
 * 解析目标类的字段及其对应的源类字段信息，封装于ResloveInfo类对象
 * 每个字段对应一个 ResloveInfo 对象
 *
 * @author wen wang
 * @date 2021/11/19 17:40
 */
public class ResloveInfo {
    /**
     * 用户自定义拓展类名称
     */
    private String extensionObjectTransformImplClass="";
    /**
     * 是否需要用户扩展实现转换
     */
    private boolean userExtend=false;

    /**
     * 是否自动转换
     */
    private boolean autoTransform=true;

    /**
     * 目标字段名称
     */
    private String targetFieldName;
    /**
     * 目标字段set方法
     */
    private String targetFieldSetFunctionName;
    /**
     * 目标字段set方法是否可用，条件：反射查询对应的set方法 存在，且 是public 关键字修饰
     */
    private boolean targetFieldSetFunctionAvailable = false;
    /**
     * 目标字段set 方法描述符
     */
    private String targetFieldSetFunctionDescriptor;

    /**
     * 目标类某字段对应的源类字段名
     */
    private String sourceFieldName;
    /**
     * 源类字段类型
     */
    private Class<?> sourceFieldType;
    /**
     * 源类字段 Field 对象，反射获取
     */
    private Field sourceField;
    /**
     * 源类字段 get 方法名
     */
    private String sourceFieldGetFunctionName;
    /**
     * 源类字段get方法是否可用，条件：反射查询对应的get方法 存在，且 是public 关键字修饰
     */
    private boolean sourceFieldGetFunctionNameAvailable = false;
    /**
     * 源类字段get方法描述符
     */
    private String sourceFieldGetFunctionDescriptor;

    /**
     * 源字段类型Internal Name
     */
    private String sourceFieldTypeInternalName;

    /**
     *
     * @return {@link String}
     */
    public String getTargetFieldName() {
        return targetFieldName;
    }

    /**
     *
     * @param targetFieldName
     */
    public void setTargetFieldName(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    /**
     * @return {@link String}
     */
    public String getTargetFieldSetFunctionName() {
        return targetFieldSetFunctionName;
    }

    /**
     * @param targetFieldSetFunctionName
     */
    public void setTargetFieldSetFunctionName(String targetFieldSetFunctionName) {
        this.targetFieldSetFunctionName = targetFieldSetFunctionName;
    }

    /**
     * @return boolean
     */
    public boolean isTargetFieldSetFunctionAvailable() {
        return targetFieldSetFunctionAvailable;
    }

    /**
     * @param targetFieldSetFunctionAvailable
     */
    public void setTargetFieldSetFunctionAvailable(boolean targetFieldSetFunctionAvailable) {
        this.targetFieldSetFunctionAvailable = targetFieldSetFunctionAvailable;
    }

    /**
     * @return {@link String}
     */
    public String getSourceFieldName() {
        return sourceFieldName;
    }

    /**
     * @param sourceFieldName 源字段名
     */
    public void setSourceFieldName(String sourceFieldName) {
        this.sourceFieldName = sourceFieldName;
    }

    /**
     * @return {@link String}
     */
    public String getSourceFieldGetFunctionName() {
        return sourceFieldGetFunctionName;
    }

    /**
     * @param sourceFieldGetFunctionName 源领域得到函数名
     */
    public void setSourceFieldGetFunctionName(String sourceFieldGetFunctionName) {
        this.sourceFieldGetFunctionName = sourceFieldGetFunctionName;
    }

    /**
     * @return boolean
     */
    public boolean isSourceFieldGetFunctionNameAvailable() {
        return sourceFieldGetFunctionNameAvailable;
    }

    /**
     * @param sourceFieldGetFunctionNameAvailable 源领域得到可用函数名
     */
    public void setSourceFieldGetFunctionNameAvailable(boolean sourceFieldGetFunctionNameAvailable) {
        this.sourceFieldGetFunctionNameAvailable = sourceFieldGetFunctionNameAvailable;
    }

    /**
     * @return {@link String}
     */
    public String getExtensionObjectTransformImplClass() {
        return extensionObjectTransformImplClass;
    }

    /**
     * @param extensionObjectTransformImplClass
     */
    public void setExtensionObjectTransformImplClass(String extensionObjectTransformImplClass) {
        this.extensionObjectTransformImplClass = extensionObjectTransformImplClass;
    }

    /**
     * @return {@link String}
     */
    public String getTargetFieldSetFunctionDescriptor() {
        return targetFieldSetFunctionDescriptor;
    }

    /**
     * @param targetFieldSetFunctionDescriptor
     */
    public void setTargetFieldSetFunctionDescriptor(String targetFieldSetFunctionDescriptor) {
        this.targetFieldSetFunctionDescriptor = targetFieldSetFunctionDescriptor;
    }


    /**
     * @return {@link String}
     */
    public String getSourceFieldGetFunctionDescriptor() {
        return sourceFieldGetFunctionDescriptor;
    }

    /**
     * @param sourceFieldGetFunctionDescriptor
     */
    public void setSourceFieldGetFunctionDescriptor(String sourceFieldGetFunctionDescriptor) {
        this.sourceFieldGetFunctionDescriptor = sourceFieldGetFunctionDescriptor;
    }

    /**
     * @return {@link String}
     */
    public String getSourceFieldTypeInternalName() {
        return sourceFieldTypeInternalName;
    }

    /**
     * @param sourceFieldTypeInternalName
     */
    public void setSourceFieldTypeInternalName(String sourceFieldTypeInternalName) {
        this.sourceFieldTypeInternalName = sourceFieldTypeInternalName;
    }

    /**
     * @return {@link Class}
     */
    public Class<?> getSourceFieldType() {
        return sourceFieldType;
    }

    /**
     * @param sourceFieldType
     */
    public void setSourceFieldType(Class<?> sourceFieldType) {
        this.sourceFieldType = sourceFieldType;
    }

    /**
     * @return {@link Field}
     */
    public Field getSourceField() {
        return sourceField;
    }

    /**
     * @param sourceField
     */
    public void setSourceField(Field sourceField) {
        this.sourceField = sourceField;
    }

    /**
     * @return boolean
     */
    public boolean isUserExtend() {
        return userExtend;
    }

    /**
     * @param userExtend
     */
    public void setUserExtend(boolean userExtend) {
        this.userExtend = userExtend;
    }

    /**
     * @return boolean
     */
    public boolean isAutoTransform() {
        return autoTransform;
    }

    /**
     * @param autoTransform
     */
    public void setAutoTransform(boolean autoTransform) {
        this.autoTransform = autoTransform;
    }
}
