package com.shzz.common.tool.bean.transform.asm;

import com.shzz.common.tool.bean.BeanFieldInfo;
import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.SystemProperties;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.context.Context;
import com.shzz.common.tool.bean.transform.asm.strategy.*;
import com.shzz.common.tool.bean.transform.asm.context.TransformTypeContext;
import com.shzz.common.tool.code.BeanTransformException;
import com.shzz.common.tool.bean.transform.asm.strategy.StrategyMode;
import com.shzz.common.tool.code.CommonCode;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter.*;
import static com.shzz.common.tool.bean.transform.asm.strategy.CollectionTypeStrategy.ELEMENT_TRANSFORM_MEDIAN;
import static org.objectweb.asm.Opcodes.*;

/**
 * @Classname TransformUtilGenerate
 * @Description TODO
 * @Date 2021/11/7 20:44
 * @Created by wen wang
 */
public class TransformUtilGenerate {

    private static final Logger LOG = LoggerFactory.getLogger("TransformUtilGenerate");

    public static final String SUPER_CLASS_INTERNAL_NAME = Type.getInternalName(BeanTransFormsHandler.class);
    public static final String OBJECT_CLASS_INTERNAL_NAME = Type.getInternalName(Object.class);
    public static final String EXTENSION_TRANSFORM_CLASS_INTERNAL_NAME = Type.getInternalName(ExtensionObjectTransform.class);
    public static final String BEAN_TRANSFORM_IMPL_CLASS_NAME_PREFIX = "com.shzz.common.tool.bean.transform.asm.BeanTransForms";
    public static final String EXTEND_TRANSFORM_IMPL_CLASS_NAME_PREFIX = "com.shzz.common.tool.bean.transform.asm.ExtendTransform";
    public static final String TRANSFORM_METHOD_NAME = "beanTransforms";
    public static final String EXTEND_TRANSFORM_METHOD_NAME = "extensionObjectTransform";
    public static final String EXTEND_IMPL_FIELD_NAME_SUFFIX = "_convert";
    public static final String TARGET_FIELD_CLASS_FIELD_SUFFIX = "_targetType";
    public static final String SOURCE_FIELD_CLASS_FIELD_SUFFIX = "_sourceType";
    public static final String EXTEND_TRANSFORM_METHOD_DESC = Type.getMethodDescriptor(
            Type.getType(Object.class),
            Type.getType(Object.class),
            Type.getType(boolean.class)
    );

    public static final String TRANSFORM_METHOD_DESC = Type.getMethodDescriptor(
            Type.getType(Object.class),
            Type.getType(Class.class),
            Type.getType(Object.class),
            Type.getType(Class.class)
    );


    //    private final Map<String, Map<String, ExtensionObjectTransform>> extensionObjectTransformMap = new ConcurrentHashMap<>();
//    private final Map<String, Map<String, BeanTransFormsHandler>> beanTransFormsHandlerMap = new ConcurrentHashMap<>();
//    private final Map<String, Map<String, Class>> fieldClassMap = new ConcurrentHashMap<>();
    // public static final Map<StrategyMode, Class<? extends ComplexTypeStrategy>> strategy = new ConcurrentHashMap<>();

    private TransformUtilGenerate() {

    }

    public static <S, T> BeanTransform generate(Class<S> sourceBeanClass, Class<T> targetClass, List<ExtensionObjectTransform> extendsTransformList) throws Exception {
        return generate(sourceBeanClass, targetClass, true, true, extendsTransformList, null);
    }


    public static <S, T> BeanTransform generate(Class<S> sourceBeanClass, Class<T> targetClass, boolean isDeepCopy) throws Exception {
        return generate(sourceBeanClass, targetClass, isDeepCopy, true, null, null);
    }

    public static <S, T> BeanTransform generate(Class<S> sourceBeanClass, Class<T> targetClass, boolean isDeepCopy, List<ExtensionObjectTransform> extendsTransformList) throws Exception {
        return generate(sourceBeanClass, targetClass, isDeepCopy, true, extendsTransformList, null);
    }

    public static <S, T> BeanTransform generate(Class<S> sourceBeanClass, Class<T> targetClass, boolean isDeepCopy, boolean permitBaseTypeInterconvert, List<ExtensionObjectTransform> extendsTransformList) throws Exception {
        return generate(sourceBeanClass, targetClass, isDeepCopy, permitBaseTypeInterconvert, extendsTransformList, null);
    }

    public static <S, T> BeanTransform generate(Class<S> sourceBeanClass, Class<T> targetClass, boolean isDeepCopy, boolean permitBaseTypeInterconvert, List<ExtensionObjectTransform> extendsTransformList, java.lang.reflect.Type[] actualGenericType) throws Exception {
        /**
         * @description:
         * @param sourceBeanClass  源类类型
         * @param targetClass 转换的目标类类型
         * @param isDeepCopy  是否深拷贝，默认是true,
         *                    如果设置为false,如果目标类和源类类型不一致时无法转换，如果一致则引用类型直接赋值，
         * @param permitBaseTypeInterconvert 是否支持不同的原始类型或者包装类型互相转换，
         *                                   比如 double 到 byte  或者Integer 到 short
         * @param extendsTransformList  用户自定义的转换类对象的集合
         *                              如果用户在对应目标类的字段中通过工具包中的注解BeanFieldInfo设置了 extensionObjectTransformImplClass 属性，表示该字段的转换使用用户自定义实现类来完成。用户创建实现类的对象通过extendsTransformList 参数传入，工具框架内部调用对应方法进行转换
         * @param actualGenericType  该参数预留，可传入null.
         *                           如果要转换方法体内部定义的局部变量（非匿名内部类方式定义）
         *                           且变量类型是参数化类型、泛型数组、通配泛型时，内部泛型实参可通过该参数传入
         * @return: com.shzz.common.tool.bean.transform.asm.BeanTransFormsHandler
         * @auther: wen wang
         * @date: 2021/12/8 13:59
         */


        UniversalClassTypeStrategy universalClassTypeStrategy = new UniversalClassTypeStrategy();
        return universalClassTypeStrategy.generate(sourceBeanClass, targetClass, isDeepCopy, permitBaseTypeInterconvert, extendsTransformList, actualGenericType);

    }

    public static String classSimpleNameReconstruct(Class rawType) {

        if (rawType.isArray()) {
            return "ArrayComponent";
        } else {
            return rawType.getSimpleName();
        }

    }

    public static Class loadASMGenerateClass(byte[] bytes, String generateClassname) throws Exception {
        //将二进制流写到本地磁盘上
        FileOutputStream fos = null;
        int lastIndex = generateClassname.lastIndexOf(".");
        String packageTopath=generateClassname.substring(0,lastIndex).replace(".",File.separator);
        String className = generateClassname.substring(lastIndex + 1);
        //String fullPath=TransformUtilGenerate.class.getResource("/")+File.separator+packageTopath+File.separator+className + ".class";
        String fullPath = System.getProperty("user.dir") + File.separator + "generate" + File.separator + packageTopath + File.separator + className + ".class";

        CustomeClassLoader customeClassLoader = new CustomeClassLoader();

        Class geneImplClass = null;
        try {

             // 创建类元信息

            CustomeClassLoader.putClassByte(generateClassname, bytes);
            geneImplClass = customeClassLoader.loadClass(generateClassname);
            File classFile = new File(fullPath);
            // 写到本地文件
            if (SystemProperties.getClassOutputFlag()) {

                if(!classFile.getParentFile().exists()){

                    classFile.getParentFile().mkdirs();
                }
               // classFile.createNewFile();
                fos = new FileOutputStream(classFile);
                fos.write(bytes);
                fos.close();
                LOG.info("Generate classes:{} store into specific path : {} ", generateClassname, classFile.getParentFile().getPath());
            }

        } catch (IOException e) {
            LOG.error(e.toString());
        }finally {
            if(Objects.nonNull(fos)){
                fos.close();
            }
        }
        return geneImplClass;
    }

    public static boolean checkGenerateClassname(String generateClassname) throws Exception {

        if (Objects.isNull(generateClassname)) {
            //todo  错误枚举后续完善
            throw new BeanTransformException(CommonCode.CLASS_NAME_NULL_EXCEPTION.getErrorCode(), CommonCode.CLASS_NAME_NULL_EXCEPTION.getErrorOutline(), "创建类名称为空,请传入正确类型");
        }
        //todo 异常检测
        return true;
    }

}
