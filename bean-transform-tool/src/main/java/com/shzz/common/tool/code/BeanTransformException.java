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
package com.shzz.common.tool.code;


/**
 * bean转换异常
 *
 * @author wen wang
 * @date 2020/6/10 18:21
 */
public class BeanTransformException extends Exception implements ErrorEncoder{


    /**
     * 错误代码
     */
    private String errorCode="not specific";
    /**
     * 错误轮廓
     */
    private String errorOutline="not specific";
    /**
     * 错误链细节
     */
    private String errorChainDetail="not specific";

    /**
     * bean转换异常
     *
     * @param errorEncoder 错误编码器
     */
    public BeanTransformException(ErrorEncoder  errorEncoder){
        errorCode=errorEncoder.getErrorCode();
        errorOutline=errorEncoder.getErrorOutline();
        errorChainDetail=errorEncoder.getErrorChainDetail();


    }

    /**
     * bean转换异常
     *
     * @param ex 前女友
     */
    public BeanTransformException(Exception  ex){
        super(ex.getCause());

    }

    /**
     * bean转换异常
     *
     * @param errorCode        错误代码
     * @param errorOutline     错误轮廓
     * @param errorChainDetail 错误链细节
     */
    public BeanTransformException(String errorCode, String errorOutline, String errorChainDetail){
        this.errorChainDetail=errorChainDetail;
        this.errorCode=errorCode;
        this.errorOutline=errorOutline;

    }

    /**
     * bean转换异常
     *
     * @param errorCode        错误代码
     * @param errorOutline     错误轮廓
     * @param errorChainDetail 错误链细节
     * @param ex               前女友
     */
    public BeanTransformException(String errorCode, String errorOutline, String errorChainDetail, Exception ex){
        super(ex.getCause());
        this.errorChainDetail=errorChainDetail;
        this.errorCode=errorCode;
        this.errorOutline=errorOutline;

    }


    /**
     * 得到错误代码
     *
     * @return {@link String}
     */
    @Override
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 得到轮廓误差
     *
     * @return {@link String}
     */
    @Override
    public String getErrorOutline() {
        return errorOutline;
    }

    /**
     * 得到错误链细节
     *
     * @return {@link String}
     */
    @Override
    public String getErrorChainDetail() {
        return errorChainDetail;
    }

    /**
     * 字符串
     *
     * @return {@link String}
     */
    @Override
    public String toString() {
        return "TdeBussinessException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorOutline='" + errorOutline + '\'' +
                ", errorChainDetail='" + errorChainDetail + '\'' +
                '}';
    }
}
