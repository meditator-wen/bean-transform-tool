package com.shzz.common.tool.bean.transform;

import com.shzz.common.tool.bean.transform.asm.BeanTransFormsHandler;

/**
 * @Classname ExtensionObjectTransform
 * @Description TODO
 * @Date 2021/10/15 17:41
 * @Created by wen wang
 */
public interface ExtensionObjectTransform extends Transform {

    /**
     * @description:  针对特殊类对象的转换方法，这里定义的特殊类指的是数组类，Map 子类，Collection 子类
     * @param sourceObject
     * @return:
     * @auther: wen wang
     * @date: 2021/10/15 19:30
     */


    public Object extensionObjectTransform(Object sourceObject, boolean deepCopy) throws Exception;

}
