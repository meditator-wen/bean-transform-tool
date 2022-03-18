package com.shzz.common.tool.bean.transform;

import com.shzz.common.tool.bean.transform.asm.BeanTransFormsHandler;


/**
 * 扩展对象变换
 *
 * @author wen wang
 * @date 2021/10/15 17:41
 */
public interface ExtensionObjectTransform extends Transform {

    /**
     * 扩展变换, 复杂类型类转换继承该接口。用户可以自定义该接口实现对象转换
     *
     * @param sourceObject 源对象
     * @param deepCopy     深拷贝
     * @return {@link Object}
     * @throws Exception 异常
     */
    public Object extensionObjectTransform(Object sourceObject, boolean deepCopy) throws Exception;

}
