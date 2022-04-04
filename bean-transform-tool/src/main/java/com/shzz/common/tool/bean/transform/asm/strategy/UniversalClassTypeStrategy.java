/*
 * Copyright 2022 The bean-transform-tool Project
 *
 * The bean-transform-tool Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.shzz.common.tool.bean.transform.asm.strategy;

import com.shzz.common.tool.bean.BeanFieldInfo;
import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.SystemProperties;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.*;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.bean.transform.asm.context.Context;
import com.shzz.common.tool.bean.transform.asm.context.TransformTypeContext;
import com.shzz.common.tool.code.BeanTransformException;
import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.*;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.bean.transform.asm.context.Context;
import com.shzz.common.tool.bean.transform.asm.context.TransformTypeContext;
import com.shzz.common.tool.code.CommonCode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter.*;
import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;


/**
 * 通用类型策略，工具框架的核心功能类
 * 用户传入需要转换的目标类和源类类型，UniversalClassTypeStrategy 负责生成对应的转换字节码，
 * 目标类和源类类型的内部字段如果是复杂类型会选择对应处理策略递归生成内部转换类
 *
 * @author wen wang
 * @date 2022/1/15 10:04
 */
public class UniversalClassTypeStrategy implements ComplexTypeStrategy{

    /**
     * 上下文
     */
    AbstractContext context;

    /**
     * 通用类类型策略
     *
     * @param context 上下文
     */
    public UniversalClassTypeStrategy(AbstractContext context){
        this.context=context;
    }

    /**
     * 通用类类型策略
     */
    public UniversalClassTypeStrategy(){

    }

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger("UniversalClassTypeStrategy");

    /**
     * 扩展对象缓存，第一层Map 的key 是 ASM 生成的生成{@link ExtensionObjectTransform} 子类类名
     * 第二层 Map key 是字段名称，value 是 ExtensionObjectTransform 类型的对象，
     */
    private final Map<String, Map<String, ExtensionObjectTransform>> extensionObjectTransformMap = new ConcurrentHashMap<>();
    /**
     * 转换对象缓存
     * 如果字段是第三方实体类，除了Map,Collection,数组，通配泛型、泛型数组、类型变量等复杂数据场景
     * 则会生成BeanTransFormsHandler 转换接口的子类，第一层的key 是 ASM 生成的生成{@link BeanTransFormsHandler} 子类类名
     * 第二层 Map key 是字段名称，value 是 BeanTransFormsHandler 类型的对象，
     */
    private final Map<String, Map<String, BeanTransFormsHandler>> beanTransFormsHandlerMap = new ConcurrentHashMap<>();
    /**
     * 转换类类型缓存
     * 字节码调用方法{@link BeanTransFormsHandler#beanTransforms(Class, Object, Class)}
     * 需要传入 源类和目标类某字段的类型信息，fieldClassMap 缓存解析后的字段类型
     * 第一层的key 是 ASM 生成的生成{@link BeanTransFormsHandler} 子类类名
     * 第二层 Map key 是字段名称，value 是 类型信息
     */
    private final Map<String, Map<String, Class>> fieldClassMap = new ConcurrentHashMap<>();
    /**
     * 策略缓存
     */
    private static final Map<StrategyMode, Class<? extends AbstractComplexTypeStrategy>> strategy = new ConcurrentHashMap<>();
    /**
     * cache 缓存 targetClass 实现了 BeanTransFormsHandler 转换对象的字段信息
     */
    private static final Map<String, Boolean> cache = new ConcurrentHashMap<>();

    /**
     * 判断是否已经存在key值对应的BeanTransFormsHandler Class 信息
     * 实体类中可能包含同类型的不同字段或者数组的组件类型和某字段类型一致时，会使用相同的转换对象。
     *
     * @param key
     * @return boolean
     */
    public static boolean hasBeanTransFormsHandler(String key) {
        return cache.containsKey(key) && cache.get(key);
    }

    /**
     * 生成转换类对象，封装与Map  中，主要针对Collection、Map、Array等复杂类型字段
     * 详见{@link ComplexTypeStrategy#geneTransform(Type, Type, String, String)}
     *
     * @param sourceBeanType
     * @param targetType
     * @param generateClassname
     * @param fieldNamePrefix
     * @return {@link Map}
     * @throws Exception 异常
     */
    @Override
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception {

        if(strategyMatch(sourceBeanType,targetType)){
            Class sourceBeanClass=(Class)sourceBeanType;
            Class targetClass=(Class)targetType;
            // UniversalClassTypeStrategy 生成常规类型转换功能类，不使用 参数generateClassname，使用自定义的命名规则
            Map<String, BeanTransFormsHandler> innerBeanTransFormsHandlerMap = new HashMap<>(4);
            String beanTransFormsFieldName=context.getIdentify()+ TransformUtilGenerate.EXTEND_IMPL_FIELD_NAME_SUFFIX;

            BeanTransFormsHandler beanTransFormsHandler= (BeanTransFormsHandler)generate(sourceBeanClass,targetClass,true,true,null,null);
            innerBeanTransFormsHandlerMap.put(beanTransFormsFieldName,beanTransFormsHandler);
            return  innerBeanTransFormsHandlerMap;
        }else{
            return null;
        }


    }


    /**
     * 详见接口类说明{@link  ComplexTypeStrategy#strategyMatch(Type, Type)}
     *
     * @param sourceBeanType
     * @param targetType
     * @return boolean
     * @throws Exception
     */
    @Override
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws Exception {
        boolean flag=false;
        if((sourceBeanType instanceof Class) &&(targetType instanceof Class)){
            Class sourceBeanClass=(Class)sourceBeanType;
            Class targetClass=(Class)targetType;
            if((!sourceBeanClass.isArray())||(!targetClass.isArray())){
                flag=true;
            }

        }
        return flag;
    }


    /**
     *  生成转换类字节码byte 数组，返回值通过自定义类加载器生成Class 信息
     *
     * @param sourceBeanClass            源bean类
     * @param targetClass                目标类
     * @param generateClassname          生成类名
     * @param isDeepCopy                 深拷贝标志
     * @param permitBaseTypeInterconvert 允许不相同的原始类或者包装类互相转换，比如 Double to Integer、Double to char、double to int
     * @param extendsTransformList       扩展转换列表，用户自定义实现的拓展类，用户拓展实现多个字段均存放于List中一并传入
     * @return byte[]                    class 文件字节数组
     * @throws Exception 异常
     */
    public <S, T> byte[] generateBeanTransformsImplClass(Class<S> sourceBeanClass, Class<T> targetClass, String generateClassname, boolean isDeepCopy,
                                                         boolean permitBaseTypeInterconvert, List<ExtensionObjectTransform> extendsTransformList) throws Exception {

        ClassWriter beanTransformsImplClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        TransformUtilGenerate.checkGenerateClassname(generateClassname);

        String internalName = generateClassname.replace('.', '/');

        // 创建类名
        // LOG.info("context.getClassVersion()= {}", AbstractComplexTypeStrategy.getClassVersion());
        beanTransformsImplClassWriter.visit(AbstractComplexTypeStrategy.getClassVersion(),
                ACC_PUBLIC + ACC_FINAL,
                internalName,
                null,
                TransformUtilGenerate.SUPER_CLASS_INTERNAL_NAME, null);


        if (!extensionObjectTransformMap.isEmpty()) {
            // 通过ASM api 写入字段 拓展类字段
            Map<String, ExtensionObjectTransform> tempMap = extensionObjectTransformMap.get(generateClassname);
            if (!tempMap.isEmpty()) {
                Set<String> keySet = tempMap.keySet();
                for (String geneFieldName : keySet) {

                    FieldVisitor fieldVisitor = beanTransformsImplClassWriter.visitField(ACC_PRIVATE,
                            geneFieldName,
                            BeanTransformsMethodAdapter.EXTENSION_TRANSFORM_INTERFACE_DESC,
                            null,
                            null);

                    fieldVisitor.visitEnd();

                }
            }


        }

        if (!beanTransFormsHandlerMap.isEmpty()) {
            // 通过ASM api 写入字段，字段值是BeanTransFormsHandler转换对象
            Map<String, BeanTransFormsHandler> tempMap = beanTransFormsHandlerMap.get(generateClassname);
            if (!tempMap.isEmpty()) {
                Set<String> keySet = tempMap.keySet();
                for (String geneFieldName : keySet) {

                    FieldVisitor fieldVisitor = beanTransformsImplClassWriter.visitField(ACC_PRIVATE,
                            geneFieldName,
                            BeanTransformsMethodAdapter.BEAN_TRANSFORM_DESC,
                            null,
                            null);

                    fieldVisitor.visitEnd();

                }
            }


        }

        if (!fieldClassMap.isEmpty()) {
            // 通过ASM api 写入字段，字段值是Class 类型，标注的是需要转换的字段的源类和目标类信息
            Map<String, Class> tempMap = fieldClassMap.get(generateClassname);
            if (!tempMap.isEmpty()) {
                Set<String> keySet = tempMap.keySet();
                for (String geneFieldName : keySet) {

                    FieldVisitor fieldVisitor = beanTransformsImplClassWriter.visitField(ACC_PRIVATE,
                            geneFieldName,
                            BeanTransformsMethodAdapter.FIELD_TYPE_DESC,
                            null,
                            null);

                    fieldVisitor.visitEnd();

                }
            }


        }


        /**
         * 构造器方法
         *  Code:
         *       stack=1, locals=1, args_size=1
         *          0: aload_0
         *          1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         *          4: return
         */
        MethodVisitor methodVisitorInit = beanTransformsImplClassWriter.visitMethod(ACC_PUBLIC,
                BeanTransformsMethodAdapter.INIT_METHOD_NAME,
                BeanTransformsMethodAdapter.INIT_METHOD_DESCRIPTOR,
                null,
                null);

        methodVisitorInit.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitorInit.visitMethodInsn(Opcodes.INVOKESPECIAL, TransformUtilGenerate.SUPER_CLASS_INTERNAL_NAME, BeanTransformsMethodAdapter.INIT_METHOD_NAME, BeanTransformsMethodAdapter.INIT_METHOD_DESCRIPTOR, false);
        methodVisitorInit.visitInsn(Opcodes.RETURN);
        methodVisitorInit.visitMaxs(1, 1);
        methodVisitorInit.visitEnd();

        /**
         * 复写抽象方法  {@link BeanTransFormsHandler#beanTransforms(Class, Object, Class)}  }
         *
         */

        MethodVisitor beanTransformsMethodVisitor = beanTransformsImplClassWriter.visitMethod(ACC_PUBLIC + ACC_FINAL, TransformUtilGenerate.TRANSFORM_METHOD_NAME, TransformUtilGenerate.TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});

        BeanTransformsMethodAdapter beanTransformsMethodAdapter = new BeanTransformsMethodAdapter(beanTransformsMethodVisitor,
                sourceBeanClass,
                targetClass,
                isDeepCopy,
                permitBaseTypeInterconvert,
                generateClassname);

        beanTransformsMethodAdapter.visitCode(); //内部已调用visiitEnd

        beanTransformsImplClassWriter.visitEnd();

        byte[] bytes = beanTransformsImplClassWriter.toByteArray();

        return bytes;

    }

    /**
     * 生成转换类对象
     *
     * @param sourceBeanClass            源bean类
     * @param targetClass                目标类
     * @param isDeepCopy                 深拷贝
     * @param permitBaseTypeInterconvert 允许不相同的原始类或者包装类互相转换，比如 Double to Integer、Double to char、double to int
     *                                   如果是false,则只能实现相同的基础类型拷贝
     * @param extendsTransformList       扩展转换列表，用户自定义实现的拓展类，用户拓展实现多个字段均存放于List中一并传入
     * @param actualGenericType          实际泛型类型，预留，暂未使用，转换泛型类型、参数化泛型、通配泛型、泛型数组时需要传入实际泛型类型
     * @return {@link BeanTransform}
     * @throws Exception 异常
     */
    public BeanTransform generate(Class sourceBeanClass, Class targetClass, boolean isDeepCopy, boolean permitBaseTypeInterconvert, List<ExtensionObjectTransform> extendsTransformList, Type[] actualGenericType) throws Exception {

        if (Objects.isNull(sourceBeanClass)) {
            throw new Exception("generate方法 传入的sourceBeanClass 类型参数为空");
        }

        if (Objects.isNull(targetClass)) {
            throw new Exception("generate方法 传入的targetClass 类型参数为空");
        }

        // Map 或者Collection 子类不在 UniversalClassTypeStrategy 处理范围
        TypeTransformAssist.checkClass(targetClass, sourceBeanClass);

        //创建类名称,todo 类名可能重复，需修正
        // String generateClassname = BEAN_TRANSFORM_IMPL_CLASS_NAME_PREFIX + sourceBeanClass.getSimpleName() + "$" + targetClass.getSimpleName();
        String generateClassname = TransformUtilGenerate.BEAN_TRANSFORM_IMPL_CLASS_NAME_PREFIX + TransformUtilGenerate.classSimpleNameReconstruct(sourceBeanClass) + "$" + TransformUtilGenerate.classSimpleNameReconstruct(targetClass);

        /**
         * 解析用户传入的自定义转换类对象，创建和目标类中字段注解 {@link BeanFieldInfo }标注的转换类之间的映射关系
         */

        final Map<String, ExtensionObjectTransform> innerExtensionObjectTransformMap = new HashMap<>(16);
        final Map<String, BeanTransFormsHandler> innerBeanTransFormsHandlerMap = new ConcurrentHashMap<>();
        final Map<String, Class> innerFieldClassMap = new ConcurrentHashMap<>();


        if((!TypeTransformAssist.isBaseType(sourceBeanClass))){
            /**
             * 除去原始类型，包装类型，其他常规类型解析内部字段递归转换，如果字段类型是复杂类型，预先生成对应字段的转换类并缓存
             * 转换类继承接口 {@link Transform}
             *
             */


            Field[] fields = targetClass.getDeclaredFields();

            for (Field field : fields) {

                ResloveInfo resloveInfo = TypeTransformAssist.reslove(field, targetClass, sourceBeanClass);

                if (!TypeTransformAssist.resloveInfoCheck(resloveInfo)) {
                    continue;
                }
                String implClass = resloveInfo.getExtensionObjectTransformImplClass();
                String geneConvertField = field.getName() + TransformUtilGenerate.EXTEND_IMPL_FIELD_NAME_SUFFIX;
                String geneSourceFieldTypeField = field.getName() + TransformUtilGenerate.SOURCE_FIELD_CLASS_FIELD_SUFFIX;
                String geneTargetFieldTypeField = field.getName() + TransformUtilGenerate.TARGET_FIELD_CLASS_FIELD_SUFFIX;
                if (resloveInfo.isUserExtend() && (!(Objects.isNull(implClass) || implClass.isEmpty()))) {

                    if ((Objects.isNull(extendsTransformList)) || (extendsTransformList.isEmpty())) {
                        throw new BeanTransformException(CommonCode.EXTENDS_TRANSFORM_ERROR.getErrorCode(), CommonCode.EXTENDS_TRANSFORM_ERROR.getErrorOutline(), "缺少字段 " + field.getName() + "所要求的 " + implClass + " 转换类对象");
                    }

                    for (ExtensionObjectTransform ext : extendsTransformList) {

                        if (ext.getClass().getName().equals(implClass)) {
                            //
                            /**
                             * 建立字段转换类和字段之间的映射关系
                             * ，注意，这里的 key=field.getName()+"_convert" 是作为ASM生成类 generateClassname  内部字段名称
                             */

                            innerExtensionObjectTransformMap.put(geneConvertField, ext);
                            //generateClassname


                        }
                    }
                    if (!innerExtensionObjectTransformMap.containsKey(geneConvertField)) {
                        LOG.error("目标类{} 的字段{} 标注了转换实现类{}，但传入的extendsTransformList 中查找不到对应的实现类对象",
                                sourceBeanClass.getSimpleName(),
                                field.getName(),
                                implClass);

                        throw new BeanTransformException(CommonCode.EXTENDS_TRANSFORM_ERROR.getErrorCode(), CommonCode.EXTENDS_TRANSFORM_ERROR.getErrorOutline(), "缺少字段 " + field.getName() + "所要求的 " + implClass + " 转换类对象");
                    }
                } else {


                    if ((!resloveInfo.isUserExtend())) {
                        // class 类型，
                        Class fieldClass = field.getType();
                        if ((!TypeTransformAssist.isBaseType(fieldClass)) && (fieldClass != Object.class) && resloveInfo.isAutoTransform()
                                && (!Map.class.isAssignableFrom(fieldClass)) && (!Collection.class.isAssignableFrom(fieldClass))
                                && (!fieldClass.isArray()) && (fieldClass != Date.class)) {

                            // 常规类型转换
                            try {
                                BeanTransFormsHandler beanTransFormsHandler = (BeanTransFormsHandler)generate(resloveInfo.getSourceFieldType(),
                                        fieldClass,
                                        isDeepCopy,
                                        permitBaseTypeInterconvert,
                                        extendsTransformList,
                                        null);
                                innerBeanTransFormsHandlerMap.put(geneConvertField, beanTransFormsHandler);

                                //记录需要转换的字段对应的类型，写方法调用时参数通过 getfield 指令获取，提高效率

                                innerFieldClassMap.put(geneTargetFieldTypeField, field.getType());
                                innerFieldClassMap.put(geneSourceFieldTypeField, resloveInfo.getSourceFieldType());
                            } catch (Exception e) {
                                LOG.error("filed 不合规类型，无法创建自动转换的BeanTransFormsHandler 实例");
                                LOG.error(e.toString());
                                continue;
                            }

                            cache.put(targetClass.getName() + field.getName(), true);

                        } else {
                            Context transformTypeContext = new TransformTypeContext(resloveInfo.getSourceField(), field, targetClass);
                            Map<String, ? extends Transform> tempMap = transformTypeContext.geneTransform(resloveInfo.getSourceField().getGenericType(), field.getGenericType(), field.getName());
                            if (Objects.nonNull(tempMap) && (!tempMap.isEmpty())) {
                                Set<String> keySet= tempMap.keySet();
                                for(String key:keySet){
                                    Transform transform=  tempMap.get(key);
                                    if(ExtensionObjectTransform.class.isAssignableFrom(transform.getClass())){
                                        innerExtensionObjectTransformMap.put(key,(ExtensionObjectTransform)transform);
                                    }

                                }

                            }

                        }


                    }


                }

            }

            // extensionObjectTransformMap 不仅包括用户传入的对象也包括程序内部生成的对象
            extensionObjectTransformMap.put(generateClassname, innerExtensionObjectTransformMap);
            fieldClassMap.put(generateClassname, innerFieldClassMap);
            beanTransFormsHandlerMap.put(generateClassname, innerBeanTransFormsHandlerMap);
        }


        /**
         * 生成转换类(继承接口 {@link BeanTransform })字节码byte数组
         */
        byte[] bytes = generateBeanTransformsImplClass(sourceBeanClass,
                targetClass,
                generateClassname,
                isDeepCopy,
                permitBaseTypeInterconvert,
                extendsTransformList);
        Class beanTransFormsImplClass = TransformUtilGenerate.loadASMGenerateClass(bytes, generateClassname);

        if (beanTransFormsImplClass == null) {
            LOG.error("ASM 生成类 {}无效，为空", generateClassname);
            return null;
        }

        Constructor<?> constructor = beanTransFormsImplClass.getDeclaredConstructor();//默认构造方法；

        // 通过ClassWriter 生成的类已指定实现接口 com.shzz.common.tool.bean.transform.asm.BeanTransFormsHandler，可强转
        BeanTransFormsHandler beanTransFormsHandler = (BeanTransFormsHandler) constructor.newInstance();

        if (!extensionObjectTransformMap.isEmpty()) {
            // 通过ASM api 写入字段
            Map<String, ExtensionObjectTransform> innerMap = extensionObjectTransformMap.get(generateClassname);
            if (!innerMap.isEmpty()) {
                Set<String> keySet = innerMap.keySet();
                for (String geneFieldName : keySet) {
                    ExtensionObjectTransform extensionObjectTransform = innerMap.get(geneFieldName);

                    Field field = beanTransFormsImplClass.getDeclaredField(geneFieldName);
                    field.setAccessible(true);
                    field.set(beanTransFormsHandler, extensionObjectTransform);

                }
            }

        }

        if (!beanTransFormsHandlerMap.isEmpty()) {
            // 通过ASM api 写入字段
            Map<String, BeanTransFormsHandler> innerMap = beanTransFormsHandlerMap.get(generateClassname);
            if (!innerMap.isEmpty()) {
                Set<String> keySet = innerMap.keySet();
                for (String beanTransFormsFieldName : keySet) {
                    BeanTransFormsHandler beanTransFormsHandlerFieldValue = innerMap.get(beanTransFormsFieldName);
                    if (Objects.isNull(beanTransFormsHandlerFieldValue)) {
                        LOG.error("key={}, value null", beanTransFormsFieldName);
                        continue;
                    }
                    Field field = beanTransFormsImplClass.getDeclaredField(beanTransFormsFieldName);
                    field.setAccessible(true);
                    field.set(beanTransFormsHandler, beanTransFormsHandlerFieldValue);

                }

            }


        }

        if (!fieldClassMap.isEmpty()) {
            // 通过ASM api 写入字段
            Map<String, Class> innerMap = fieldClassMap.get(generateClassname);
            if (!innerMap.isEmpty()) {
                Set<String> keySet = innerMap.keySet();
                for (String fieldClassFieldName : keySet) {
                    Class fieldClass = innerMap.get(fieldClassFieldName);

                    Field field = beanTransFormsImplClass.getDeclaredField(fieldClassFieldName);
                    field.setAccessible(true);
                    field.set(beanTransFormsHandler, fieldClass);

                }
            }


        }

        return beanTransFormsHandler;

    }


}
