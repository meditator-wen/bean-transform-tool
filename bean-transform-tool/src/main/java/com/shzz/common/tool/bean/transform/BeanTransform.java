package com.shzz.common.tool.bean.transform;

/**
 * @Classname BeanTransform
 * @Description TODO
 * @Date 2022/1/14 9:51
 * @Created by wen wang
 */
public interface BeanTransform extends Transform{
    /**
     * @param sourceBeanClass:
     * @param sourceBeanObject:
     * @param targetClass:
     * @Description: 转换类接口，
     * TransformUtilGenerate generate 方法返回值是该接口子类对象
     * 接口内泛型方法是为了在代码编译阶段完成类型检查。
     * @Author: wen wang
     * @Date: 2022/2/9 21:58
     * @return: T
     **/
    public  <S,T> T beanTransform(Class<S> sourceBeanClass, S sourceBeanObject, Class<T> targetClass) throws Exception;

}
