package com.shzz.common.tool.bean.transform.asm;

import org.objectweb.asm.Label;

/**
 * @Classname LocalVarTable
 * @Description TODO
 * @Date 2021/11/22 9:34
 * @Created by wen wang
 */
public class LocalVariableInfo {
    private String name;
    private String alias;// 变量别名，按照规定命名规则处理
    private String descriptor;
    private String signature;
    private Label start;
    private Label end;
    private int index;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Label getStart() {
        return start;
    }

    public void setStart(Label start) {
        this.start = start;
    }

    public Label getEnd() {
        return end;
    }

    public void setEnd(Label end) {
        this.end = end;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
