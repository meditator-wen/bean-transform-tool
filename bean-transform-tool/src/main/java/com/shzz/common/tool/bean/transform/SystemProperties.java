package com.shzz.common.tool.bean.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Classname SystemProperties
 * @Description TODO
 * @Date 2021/12/24 10:44
 * @Created by wen wang
 */
public class SystemProperties {
    private static final Logger LOG = LoggerFactory.getLogger("CustomeClassLoader");
    public static final int THRESHOLD_SIZE = 1 * 1024 * 1024;
    public static final String CLASS_SIZE_THRESHOLD="com.akfd.generalduty.tool.bean.transform.meta.size.threshold";
    public static final String CLASS_OUTPUT_FLAG="class.output.flag";
    public static final String STRICT_MODE_FLAG = "strict.mode.flag";
    /**
     * 包装类型深拷贝标志，如果源类和目标类对应字段是包装类且类型相同，则直接赋值。实际上还是引用赋值，
     * 但用户一般情况下无法改变所引用的包装类对象值，所以两边对统一包装类对象的引用基本不会出现数据不一致情况
     * 特殊情况下，比如 字段 Integer integer=Integer.valueOf(10); 如果通过反射修改integer 对象的 value 值也可能导致数据拷贝的副本数据被修改
     */

    public static final String WRAPS_TYPE_DEEPY_COPY="wraps.types.deepcopy";
    static {
        System.setProperty(CLASS_SIZE_THRESHOLD,String.valueOf(THRESHOLD_SIZE));
        System.setProperty(CLASS_OUTPUT_FLAG,"true");
        System.setProperty(WRAPS_TYPE_DEEPY_COPY,"false");
        System.setProperty(STRICT_MODE_FLAG, "true");
    }
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
