package com.shzz.common.tool.bean.transform.asm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Classname ExceptionInfo
 * @Description TODO
 * @Date 2021/3/3 20:41
 * @Created by wen wang
 */
public class ErrorInfoStack {
    private final static Logger LOG = LoggerFactory.getLogger(ErrorInfoStack.class);

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
