package com.shzz.common.tool.bean.transform.asm.context;

import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.strategy.ComplexTypeStrategy;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @Classname RegisterContext
 * @Description TODO
 * @Date 2022/1/10 14:02
 * @Created by wen wang
 */
public interface Context {

    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String fieldNamePrefix) throws Exception;

    public String geneClassName() ;
}
