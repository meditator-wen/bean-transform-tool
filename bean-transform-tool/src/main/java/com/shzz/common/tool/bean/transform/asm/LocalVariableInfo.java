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

import org.objectweb.asm.Label;


/**
 * 局部变量信息
 *
 * @author wen wang
 * @date 2021/11/22 9:34
 */
public class LocalVariableInfo {
    /**
     * 名字
     */
    private String name;
    /**
     * 变量别名，按照规定命名规则处理
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
     * 开始
     */
    private Label start;
    /**
     * 结束
     */
    private Label end;
    /**
     * 指数
     */
    private int index;


    /**
     * 得到名字
     *
     * @return {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * 集名称
     *
     * @param name 名字
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取描述符
     *
     * @return {@link String}
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * 组描述符
     *
     * @param descriptor 描述符
     */
    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * 得到签名
     *
     * @return {@link String}
     */
    public String getSignature() {
        return signature;
    }

    /**
     * 设置签名
     *
     * @param signature 签名
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * 开始
     *
     * @return {@link Label}
     */
    public Label getStart() {
        return start;
    }

    /**
     * 设置开始
     *
     * @param start 开始
     */
    public void setStart(Label start) {
        this.start = start;
    }

    /**
     * 会结束
     *
     * @return {@link Label}
     */
    public Label getEnd() {
        return end;
    }

    /**
     * 设置结束
     *
     * @param end 结束
     */
    public void setEnd(Label end) {
        this.end = end;
    }

    /**
     * 得到指数
     *
     * @return int
     */
    public int getIndex() {
        return index;
    }

    /**
     * 设置索引
     *
     * @param index 指数
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 得到别名
     *
     * @return {@link String}
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 设置别名
     *
     * @param alias 别名
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }
}
