package com.shzz.common.tool.code;

/**
 * @Classname ErrorEncoder
 * @Description TODO
 * @Date 2020/5/1 20:21
 * @Created by wen wang
 */
public interface ErrorEncoder {
    
    /*
     * @Description 
     * @Date 2020/5/1 22:58
     * @Param 
     * @return 
     **/

    String getErrorCode();
    String getErrorOutline();
    String getErrorChainDetail();
}
