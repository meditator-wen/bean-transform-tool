package com.shzz.common.tool.bean.transform.asm;

import org.objectweb.asm.Label;


/**
 * 变量定义
 *
 * @author wen wang
 * @date 2021/12/2 12:05
 */
public class VariableDefine {
    /**
     * 名字
     */
    private String name;
    /**
     * 别名，变量别名，按照规定命名规则处理
     */
    private String alias;
    /**
     * 描述符
     */
    private String descriptor;
    /**
     * 签名
     */
    private String signature;
    /**
     * 变量开始标签，变量作用域的初始位置
     */
    private Label start;
    /**
     * 结束标签，即，变量作用域的最终位置
     */
    private Label end;
    /**
     * 在虚拟机栈局部变量表的索引编号
     */
    private int index;
    /**
     * 定义局部变量信息
     */
    private LocalVariableInfo defineLocalVariableInfo=new LocalVariableInfo();


    /**
     * 变量定义
     */
    public VariableDefine (){

    }

    /**
     * 定义
     *
     * @return {@link LocalVariableInfo}
     */
    public LocalVariableInfo define(){

        return defineLocalVariableInfo;
    }

    /**
     * 别名
     *
     * @param alias 别名
     * @return {@link VariableDefine}
     */
    public  VariableDefine alias(String alias){
        defineLocalVariableInfo.setAlias(alias);
        return this;
    }

    /**
     * 名字
     *
     * @param name 名字
     * @return {@link VariableDefine}
     */
    public  VariableDefine name(String name){
        defineLocalVariableInfo.setName(name);
        return this;
    }

    /**
     * 描述符
     *
     * @param descriptor 描述符
     * @return {@link VariableDefine}
     */
    public  VariableDefine descriptor(String descriptor){
        defineLocalVariableInfo.setDescriptor(descriptor);
        return this;
    }

    /**
     * 签名
     *
     * @param signature 签名
     * @return {@link VariableDefine}
     */
    public  VariableDefine signature(String signature){
        defineLocalVariableInfo.setSignature(signature);
        return this;
    }

    /**
     *
     *
     * @param start
     * @return {@link VariableDefine}
     */
    public  VariableDefine start(Label start){
        defineLocalVariableInfo.setStart(start);
        return this;
    }

    /**
     *
     *
     * @param end
     * @return {@link VariableDefine}
     */
    public  VariableDefine end(Label end){
        defineLocalVariableInfo.setEnd(end);
        return this;
    }

    /**
     *
     *
     * @param index
     * @return {@link VariableDefine}
     */
    public  VariableDefine index(int index){
        defineLocalVariableInfo.setIndex(index);
        return this;
    }


}
