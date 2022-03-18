package com.shzz.common.tool.code;


/**
 * 错误码接口
 *
 * @author wen wang
 * @date 2021/9/1 22:58
 */
public interface ErrorEncoder {


    /**
     * 得到错误代码
     *
     * @return {@link String}
     */
    String getErrorCode();

    /**
     * 得到轮廓误差
     *
     * @return {@link String}
     */
    String getErrorOutline();

    /**
     * 得到错误链细节
     *
     * @return {@link String}
     */
    String getErrorChainDetail();
}
