package com.shzz.common.tool.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * @Classname SourceBeanField
 * @Description
 *
 * userExtend 标注是否用户自定义转换，默认为false
 * autoTransform  标注是否自动转换
 * extensionObjectTransformImplClass 自定义转换实现类名称，继承ExtensionObjectTransform接口
 * getFunctionName  字段对应的get 方法名，如果不设置，
 * 默认值是 前缀get+字段名称首字母大写+字段名称除首字母外其他字符,
 * 如果是boolean 型，默认前缀是is， 不是get(idea 生成boolean get方法的默认规则)
 *  setFunctionName  字段对应的set 方法名，如果不设置，
 *  默认值是 set+字段名称首字母大写+字段名称除首字母外其他字符
 *  sourceFieldName  本类字段对应的源类字段名称，在字段名不一致的情况下需要制定，默认源类字段和本类字段同名
 *  description   字段描述信息
 * @Date 2021/9/13 22:54
 * @Created by wen wang
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanFieldInfo {
    boolean userExtend() default false;

    boolean autoTransform() default true;

    String extensionObjectTransformImplClass() default "";

    String getFunctionName() default "";

    String setFunctionName() default "";

    String sourceFieldName() default "";

    String description() default "";

}
