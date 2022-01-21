package com.shzz.common.tool.bean.transform;

/**
 * @Classname BeanTransform
 * @Description TODO
 * @Date 2022/1/14 9:51
 * @Created by wen wang
 */
public interface BeanTransform extends Transform{
    public  <S,T> T beanTransform(Class<S> sourceBeanClass, S sourceBeanObject, Class<T> targetClass) throws Exception;

}
