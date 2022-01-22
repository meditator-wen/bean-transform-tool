package com.shzz.common.tool.bean.transform.asm.context;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @Classname AbstractContext
 * @Description TODO
 * @Date 2022/1/17 12:10
 * @Created by wen wang
 */
public abstract class AbstractContext implements Context {
    protected String identify;
    protected Field sourceField;
    protected Field targetField;
    protected Class ownerClass;

    public String getIdentify() {
        return identify;
    }

    public void setIdentify(String identify) {
        this.identify = identify;
    }

    public Field getSourceField() {
        return sourceField;
    }

    public void setSourceField(Field sourceField) {
        this.sourceField = sourceField;
    }

    public Field getTargetField() {
        return targetField;
    }

    public void setTargetField(Field targetField) {
        this.targetField = targetField;
    }

    public Class getOwnerClass() {
        return ownerClass;
    }

    public void setOwnerClass(Class ownerClass) {
        this.ownerClass = ownerClass;
    }


}
