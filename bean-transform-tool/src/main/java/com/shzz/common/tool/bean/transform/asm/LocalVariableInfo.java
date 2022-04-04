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
     * 开始标签
     */
    private Label start;
    /**
     * 结束标签
     */
    private Label end;
    /**
     * 变量索引
     */
    private int index;


    /**
     * @return {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return {@link String}
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * @param descriptor
     */
    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * @return {@link String}
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * @return {@link Label}
     */
    public Label getStart() {
        return start;
    }

    /**
     * @param start
     */
    public void setStart(Label start) {
        this.start = start;
    }

    /**
     * @return {@link Label}
     */
    public Label getEnd() {
        return end;
    }

    /**
     * @param end
     */
    public void setEnd(Label end) {
        this.end = end;
    }

    /**
     * @return int
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return {@link String}
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }
}
