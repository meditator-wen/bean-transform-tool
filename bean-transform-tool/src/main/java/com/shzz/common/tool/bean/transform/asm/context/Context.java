package com.shzz.common.tool.bean.transform.asm.context;

import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.strategy.ComplexTypeStrategy;

import java.lang.reflect.Type;
import java.util.Map;


/**
 * AbstractContext super interface
 *
 * @author wen wang
 * @date 2022/1/10 14:02
 */
public interface Context {

    /**
     * generate transform class
     *
     * @param sourceBeanType
     * @param targetType
     * @param fieldNamePrefix A field of a complex type in the owner class will generate the conversion class object of the field
     *                        and store it in a field of the conversion class corresponding to the owner class.
     *                        The name prefix of the field should be consistent with the field name of the complex type.
     * @return {@link Map}     key is field name of the conversion class,
     * value is Object of conversion class,
     * and The parent interface is {@link Transform}
     * @throws Exception
     */
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String fieldNamePrefix) throws Exception;

    /**
     * @return {@link String}
     */
    public String geneClassName();
}
