package com.shzz.common.tool.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * @Classname SourceBeanField
 * @Description TODO
 * @Date 2021/9/13 22:54
 * @Created by wen wang
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanFieldInfo {
    boolean userExtend() default false;  // 拓展类实现类名称
    boolean autoTransform() default true;  // 自动转换
    String extensionObjectTransformImplClass() default "";  // 拓展类实现类名称
    String getFunctionName() default "";  // 字段对应的get 方法名，如果不设置，默认值是 前缀get+字段名称首字母大写+字段名称除首字母外其他字符,如果是boolean 型，前缀是is
    String setFunctionName() default "";// 字段对应的set 方法名，如果不设置，默认值是 set+字段名称首字母大写+字段名称除首字母外其他字符
    String sourceFieldName() default ""; // 本类字段对应的源类字段名称，在字段名不一致的情况下需要制定，默认源类字段和本类字段同名
    String description() default "";  // 字段描述信息

}
