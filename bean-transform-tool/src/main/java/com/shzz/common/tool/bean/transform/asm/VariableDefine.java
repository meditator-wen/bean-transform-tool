package com.shzz.common.tool.bean.transform.asm;

import org.objectweb.asm.Label;

/**
 * @Classname VariableDefine
 * @Description TODO
 * @Date 2021/12/2 12:05
 * @Created by wen wang
 */
public class VariableDefine {
    private String name;
    private String alias;// 变量别名，按照规定命名规则处理
    private String descriptor;
    private String signature;
    private Label start;
    private Label end;
    private int index;
    private LocalVariableInfo defineLocalVariableInfo=new LocalVariableInfo();


    public VariableDefine (){

    }

    public LocalVariableInfo define(){

       return defineLocalVariableInfo;
    }
    public  VariableDefine alias(String alias){
        defineLocalVariableInfo.setAlias(alias);
        return this;
    }

    public  VariableDefine name(String name){
        defineLocalVariableInfo.setName(name);
        return this;
    }

    public  VariableDefine descriptor(String descriptor){
        defineLocalVariableInfo.setDescriptor(descriptor);
        return this;
    }
    public  VariableDefine signature(String signature){
        defineLocalVariableInfo.setSignature(signature);
        return this;
    }
    public  VariableDefine start(Label start){
        defineLocalVariableInfo.setStart(start);
        return this;
    }
    public  VariableDefine end(Label end){
        defineLocalVariableInfo.setEnd(end);
        return this;
    }
    public  VariableDefine index(int index){
        defineLocalVariableInfo.setIndex(index);
        return this;
    }


}
