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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 错误信息栈
 *
 * @author wen wang
 * @date 2021/3/3 20:41
 */
public class ErrorInfoStack {
    /**
     * 日志
     */
    private final static Logger LOG = LoggerFactory.getLogger(ErrorInfoStack.class);

    /**
     * 提取异常堆栈信息并打印
     *
     * @param e
     * @return {@link String}
     */
    public static String getExceptionStackInfo(Exception e){

        LOG.error("异常堆栈 ：");
        StackTraceElement[] stackTraceElements=e.getStackTrace();
        StringBuilder stringBuilder=new StringBuilder();
        for(StackTraceElement stackTraceElement:stackTraceElements){
            LOG.error(stackTraceElement.toString());
            stringBuilder.append("\r\n");
            stringBuilder.append(stackTraceElement.toString());
        }

        return stringBuilder.toString();

    }

}
