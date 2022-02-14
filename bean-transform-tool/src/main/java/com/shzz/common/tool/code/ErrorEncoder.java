package com.shzz.common.tool.code;

/**
 * @Classname ErrorEncoder
 * @Description TODO
 * @Date 2021/9/1 22:58
 * @Created by wen wang
 */
public interface ErrorEncoder {
    
    /*
     * @Description
     * @Date 2021/9/1 22:58
     * @Param 
     * @return 
     **/

    String getErrorCode();
    String getErrorOutline();
    String getErrorChainDetail();
}
