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
 * @Classname ComplexTypeStrategy
 * @Description TODO
 * @Date 2021/12/8 13:47
 * @Created by wen wang
 */
public interface ComplexTypeStrategy extends Serializable {

 // public void geneInstruction(ClassWriter extensTransformImplClassWriter, Type targetType, Type sourceBeanType,String newMethodPrefix)  throws Exception ;

  //public Map<String, ? extends  Transform>  geneTransform(java.lang.reflect.Type sourceBeanType, java.lang.reflect.Type targetType, String generateClassname, String fieldNamePrefix, TransformUtilGenerate  transformUtilGenerate) throws Exception;
  public Map<String, ? extends Transform>  geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception;


  public boolean strategyMatch(Type sourceBeanType, Type targetType) throws  Exception;

}
