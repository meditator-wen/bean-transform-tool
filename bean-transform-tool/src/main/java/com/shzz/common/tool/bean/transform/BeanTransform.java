package com.shzz.common.tool.bean.transform;


/**
 * bean转换接口类
 *
 * @author wen wang
 * @date 2022/1/14 9:51
 */
public interface BeanTransform extends Transform{

    /**
     * 转换类接口
     * 接口内泛型方法是为了在代码编译阶段完成类型检查
     * @param sourceBeanClass  源bean类
     * @param sourceBeanObject 源bean对象
     * @param targetClass      目标类
     * @return {@link T}       该接口子类对象
     * @throws Exception 异常
     */
    public  <S,T> T beanTransform(Class<S> sourceBeanClass, S sourceBeanObject, Class<T> targetClass) throws Exception;

}
