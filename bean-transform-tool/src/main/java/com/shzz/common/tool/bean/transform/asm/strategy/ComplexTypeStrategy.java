package com.shzz.common.tool.bean.transform.asm.strategy;

import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.Transform;
import org.objectweb.asm.ClassWriter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;


/**
 * 复杂类型策略
 *
 * @author wen wang
 * @date 2021/12/8 13:47
 */
public interface ComplexTypeStrategy extends Serializable {


    /**
     * 基因转换
     *
     * @param sourceBeanType    源bean类型
     * @param targetType        目标类型
     * @param generateClassname 生成类名
     * @param fieldNamePrefix   字段名称前缀
     * @return {@link Map}
     * @throws Exception 异常
     */
  public Map<String, ? extends Transform>  geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception;


    /**
     * 战略匹配
     *
     * @param sourceBeanType 源bean类型
     * @param targetType     目标类型
     * @return boolean
     * @throws Exception 异常
     */
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws  Exception;

}
