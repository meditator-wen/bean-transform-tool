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
package com.shzz.common.tool.bean.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 系统属性
 *
 * @author wen wang
 * @date 2021/12/24 10:44
 */
public class SystemProperties {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger("CustomeClassLoader");
    /**
     * 阈值大小
     */
    public static final int THRESHOLD_SIZE = 1 * 1024 * 1024;
    /**
     * 班级规模阈值
     */
    public static final String CLASS_SIZE_THRESHOLD="com.akfd.generalduty.tool.bean.transform.meta.size.threshold";
    /**
     * 类输出标志
     */
    public static final String CLASS_OUTPUT_FLAG="class.output.flag";
    /**
     * 严格模式国旗
     */
    public static final String STRICT_MODE_FLAG = "strict.mode.flag";


    /**
     * 包装类型deepy副本
     */
    public static final String WRAPS_TYPE_DEEPY_COPY="wraps.types.deepcopy";

    static {
        System.setProperty(CLASS_SIZE_THRESHOLD,String.valueOf(THRESHOLD_SIZE));
        System.setProperty(CLASS_OUTPUT_FLAG,"true");
        /**
         * 包装类型深拷贝标志，如果源类和目标类对应字段是包装类且类型相同，则直接赋值。实际上还是引用赋值，
         * 但用户一般情况下无法改变所引用的包装类对象值，所以两边对统一包装类对象的引用基本不会出现数据不一致情况
         * 特殊情况下，比如 字段 Integer integer=Integer.valueOf(10); 如果通过反射修改integer 对象的 value 值也可能导致数据拷贝的副本数据被修改
         */
        System.setProperty(WRAPS_TYPE_DEEPY_COPY,"false");
        System.setProperty(STRICT_MODE_FLAG, "true");
    }

    /**
     * 得到班级规模阈值
     *
     * @return int
     */
    public static int getClassSizeThreshold(){
        int sizeThreshold=THRESHOLD_SIZE;

        String config=System.getProperty(CLASS_SIZE_THRESHOLD);;
        try {
            sizeThreshold=Integer.parseInt(config);
        } catch (NumberFormatException e) {
            LOG.error("系统配置：{} value {} 无法转换为int,使用默认值 50*1024*1024B ",CLASS_SIZE_THRESHOLD,config);
        }
        return  sizeThreshold;
    }

    /**
     * 得到严格模式国旗
     *
     * @return boolean
     */
    public static boolean getStrictModeFlag() {
        boolean flag = false;

        String config = System.getProperty(STRICT_MODE_FLAG);
        ;
        try {
            flag = Boolean.parseBoolean(config);
        } catch (NumberFormatException e) {
            LOG.error("系统配置：{} value {} 无法转换为boolean,使用默认值 false ", STRICT_MODE_FLAG, config);
        }
        return flag;
    }

    /**
     * 得到类输出标志
     *
     * @return boolean
     */
    public static boolean getClassOutputFlag(){
        boolean flag=false;

        String config=System.getProperty(CLASS_OUTPUT_FLAG);;
        try {
            flag=Boolean.parseBoolean(config);
        } catch (NumberFormatException e) {
            LOG.error("系统配置：{} value {} 无法转换为boolean,使用默认值 false ",CLASS_OUTPUT_FLAG,config);
        }
        return  flag;
    }

    /**
     * 包装类型deepy复制标志
     *
     * @return boolean
     */
    public static boolean getWrapsTypeDeepyCopyFlag(){
        boolean flag=false;

        String config=System.getProperty(WRAPS_TYPE_DEEPY_COPY);;
        try {
            flag=Boolean.parseBoolean(config);
        } catch (NumberFormatException e) {
            LOG.error("系统配置：{} value {} 无法转换为boolean,使用默认值 false ",WRAPS_TYPE_DEEPY_COPY,config);
        }
        return  flag;
    }

}
