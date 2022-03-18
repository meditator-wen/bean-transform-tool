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
     * 得到异常堆栈信息
     *
     * @param e e
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
