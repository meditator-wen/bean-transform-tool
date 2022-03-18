package com.shzz.common.tool.bean.transform.asm.context;

import com.shzz.common.tool.bean.transform.asm.strategy.AbstractComplexTypeStrategy;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;


/**
 * AbstractContext
 * Record the Specific field information that needs to be converted for the target class and the source class
 *
 * @author wen wang
 * @date 2022/1/17 12:10
 */
public abstract class AbstractContext implements Context {


    /**
     * Field flags, i.e. field names
     */
    protected String identify;
    /**
     * Source  field, obtained by reflection
     */
    protected Field sourceField;
    /**
     * target  field, obtained by reflection
     */
    protected Field targetField;
    /**
     * owner Class  of targetField
     */
    protected Class ownerClass;

    /**
     * @return {@link String}
     */
    public String getIdentify() {
        return identify;
    }

    /**
     * @param identify
     */
    public void setIdentify(String identify) {
        this.identify = identify;
    }

    /**
     *
     *
     * @return {@link Field}
     */
    public Field getSourceField() {
        return sourceField;
    }

    /**
     *
     *
     * @param sourceField
     */
    public void setSourceField(Field sourceField) {
        this.sourceField = sourceField;
    }

    /**
     *
     *
     * @return {@link Field}
     */
    public Field getTargetField() {
        return targetField;
    }

    /**
     * @param targetField
     */
    public void setTargetField(Field targetField) {
        this.targetField = targetField;
    }

    /**
     *
     * @return {@link Class}
     */
    public Class getOwnerClass() {
        return ownerClass;
    }

    /**
     *
     * @param ownerClass
     */
    public void setOwnerClass(Class ownerClass) {
        this.ownerClass = ownerClass;
    }


}
