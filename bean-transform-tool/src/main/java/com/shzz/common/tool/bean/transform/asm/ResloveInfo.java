package com.shzz.common.tool.bean.transform.asm;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @Classname FieldInfo
 * @Description TODO
 * @Date 2021/11/19 17:40
 * @Created by wen wang
 */
public class ResloveInfo {
    private String extensionObjectTransformImplClass="";
    private boolean userExtend=false;

    private boolean autoTransform=true;

    private String targetFieldName;
    private String targetFieldSetFunctionName;
    private boolean targetFieldSetFunctionAvailable = false;
    private String targetFieldSetFunctionDescriptor;

    private String sourceFieldName;
    private Class<?> sourceFieldType;
    private Field sourceField;
    private String sourceFieldGetFunctionName;
    private boolean sourceFieldGetFunctionNameAvailable = false;
    private String sourceFieldGetFunctionDescriptor;

    private String sourceFieldTypeInternalName;

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public void setTargetFieldName(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    public String getTargetFieldSetFunctionName() {
        return targetFieldSetFunctionName;
    }

    public void setTargetFieldSetFunctionName(String targetFieldSetFunctionName) {
        this.targetFieldSetFunctionName = targetFieldSetFunctionName;
    }

    public boolean isTargetFieldSetFunctionAvailable() {
        return targetFieldSetFunctionAvailable;
    }

    public void setTargetFieldSetFunctionAvailable(boolean targetFieldSetFunctionAvailable) {
        this.targetFieldSetFunctionAvailable = targetFieldSetFunctionAvailable;
    }

    public String getSourceFieldName() {
        return sourceFieldName;
    }

    public void setSourceFieldName(String sourceFieldName) {
        this.sourceFieldName = sourceFieldName;
    }

    public String getSourceFieldGetFunctionName() {
        return sourceFieldGetFunctionName;
    }

    public void setSourceFieldGetFunctionName(String sourceFieldGetFunctionName) {
        this.sourceFieldGetFunctionName = sourceFieldGetFunctionName;
    }

    public boolean isSourceFieldGetFunctionNameAvailable() {
        return sourceFieldGetFunctionNameAvailable;
    }

    public void setSourceFieldGetFunctionNameAvailable(boolean sourceFieldGetFunctionNameAvailable) {
        this.sourceFieldGetFunctionNameAvailable = sourceFieldGetFunctionNameAvailable;
    }

    public String getExtensionObjectTransformImplClass() {
        return extensionObjectTransformImplClass;
    }

    public void setExtensionObjectTransformImplClass(String extensionObjectTransformImplClass) {
        this.extensionObjectTransformImplClass = extensionObjectTransformImplClass;
    }

    public String getTargetFieldSetFunctionDescriptor() {
        return targetFieldSetFunctionDescriptor;
    }

    public void setTargetFieldSetFunctionDescriptor(String targetFieldSetFunctionDescriptor) {
        this.targetFieldSetFunctionDescriptor = targetFieldSetFunctionDescriptor;
    }


    public String getSourceFieldGetFunctionDescriptor() {
        return sourceFieldGetFunctionDescriptor;
    }

    public void setSourceFieldGetFunctionDescriptor(String sourceFieldGetFunctionDescriptor) {
        this.sourceFieldGetFunctionDescriptor = sourceFieldGetFunctionDescriptor;
    }

    public String getSourceFieldTypeInternalName() {
        return sourceFieldTypeInternalName;
    }

    public void setSourceFieldTypeInternalName(String sourceFieldTypeInternalName) {
        this.sourceFieldTypeInternalName = sourceFieldTypeInternalName;
    }

    public Class<?> getSourceFieldType() {
        return sourceFieldType;
    }

    public void setSourceFieldType(Class<?> sourceFieldType) {
        this.sourceFieldType = sourceFieldType;
    }

    public Field getSourceField() {
        return sourceField;
    }

    public void setSourceField(Field sourceField) {
        this.sourceField = sourceField;
    }

    public boolean isUserExtend() {
        return userExtend;
    }

    public void setUserExtend(boolean userExtend) {
        this.userExtend = userExtend;
    }

    public boolean isAutoTransform() {
        return autoTransform;
    }

    public void setAutoTransform(boolean autoTransform) {
        this.autoTransform = autoTransform;
    }
}
