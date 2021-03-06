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

import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.BeanTransFormsHandler;
import com.shzz.common.tool.bean.transform.asm.LocalVariableInfo;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.VariableDefine;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.bean.transform.asm.context.Context;
import com.shzz.common.tool.bean.transform.asm.context.TransformTypeContext;
import org.objectweb.asm.*;
//import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter.*;
import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.*;
import static com.shzz.common.tool.bean.transform.asm.strategy.StrategyMode.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Map 类型转换策略
 *
 * @author wen wang
 * @date 2022/1/10 20:54
 */
public class MapTypeStrategy extends AbstractComplexTypeStrategy {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger("MapTypeStrategy");
    /**
     * 键设置变量名
     */
    public static final String KEY_SET_VARIABLE_NAME = "sourceMapKeySet";
    /**
     * 键设置迭代器变量名称
     */
    public static final String KEY_SET_ITERATOR_VARIABLE_NAME = "keySetIterator";
    /**
     * 临时关键变量名
     */
    public static final String TEMP_KEY_VARIABLE_NAME = "tempKey";
    /**
     * 临时值变量名
     */
    public static final String TEMP_VALUE_VARIABLE_NAME = "tempValue";
    /**
     * 源映射变量名
     */
    public static final String SOURCE_MAP_VARIABLE_NAME = "sourceMapVar";
    /**
     * 目标类Map 字段 key值 类型
     */
    private Type targetKeyType;
    /**
     * 目标类Map 字段 key值 RawClass
     */
    private Class targetKeyRawClass;
    /**
     * 源类 Map 字段 key值 Type
     */
    private Type sourceKeyType;
    /**
     * 源类 Map 字段 key值 RawClass
     */
    private Class sourceKeyRawClass;
    /**
     *   目标类Map 字段 value值 Type
     */
    private Type targetValueType;
    /**
     *   目标类Map 字段 value值 RawClass
     */
    private Class targetValueRawClass;
    /**
     *   源类 Map 字段 value值 Type
     */
    private Type sourceValueType;
    /**
     *   源类 Map 字段 value值 RawClass
     */
    private Class sourceValueRawClass;
    /**
     *  目标类Map 字段RawClass
     */
    private Class targetMapRawClass;
    /**
     * 源类Map 字段RawClass
     */
    private Class sourceMapRawClass;
    /**
     * 上下文信息，记录需要转换的两个字段信息
     */
    private AbstractContext registerContext;
    /**
     * 基于源类 Map 字段和目标类Map 字段 key 值生成的转换的类描述信息
     */
    private String keyTransformClassDescription;
    /**
     * 基于源类 Map 字段和目标类Map 字段 value 值生成的转换的类描述信息
     */
    private String valueTransformClassDescription;
    /**
     * 基于源类 Map 字段和目标类Map 字段 生成的转换的类类名，（包路径中"." 替换成 "/" 转成）
     */
    private String internalName;

    /**
     *  Map 类型转换类命名编号，如果有多层嵌套Map或者多个Map类型 字段,编号依次增加
     */
    public static ThreadLocal<Integer> sequence_Local = new ThreadLocal<>();


    /**
     * 映射类型策略
     *
     * @param context 上下文
     */
    public MapTypeStrategy(AbstractContext context) {
        registerContext = context;
        // 如果同一线程再次创建MapTypeStrategy 序号累加
        // LOG.info("new MapTypeStrategy source field={}, target field={}",context.getSourceField().getName(),context.getTargetField().getName());
        try {

            setSequence(sequence_Local, context.getSourceField().getGenericType(), context.getTargetField().getGenericType());


        } catch (Exception e) {
            LOG.error(e.toString());
        }


    }


    /**
     * 解析Map 类型内部key 和v alued的类型
     *
     * @param type 类型
     * @return {@link Class}
     */
    private Class resloveKeyAndValueType(Type type) {
        Class resloveRawClass = null;
        if (type instanceof ParameterizedType) {
            //目标类map 字段的key 或 value 是参数化类型
            resloveRawClass = (Class) ((ParameterizedType) type).getRawType();
            if (!(Map.class.isAssignableFrom(resloveRawClass) || Collection.class.isAssignableFrom(resloveRawClass))) {
                // key 或value 是参数化类型情况则只处理Map 或者Collection 参数化类型，其他的比如通配泛型、泛型类型变量，不予处理
                resloveRawClass = null;
            }
        } else if (type instanceof Class) {
            resloveRawClass = (Class) type;
        }
        return resloveRawClass;

    }

    /**
     * 解析Map 类型
     *
     * @param targetType 目标类型
     * @param sourceType 源类型
     * @return boolean
     * @throws Exception 异常
     */
    private boolean resloveMap(Type targetType, Type sourceType) throws Exception {
        boolean resloveSuccess = true;
        if (strategyMatch(targetType, sourceType)) {
            // 满足strategyMatch条件，目标类与源类为Map 类型
            ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
            ParameterizedType sourceParameterizedType = (ParameterizedType) sourceType;

            targetMapRawClass = (Class) targetParameterizedType.getRawType();
            sourceMapRawClass = (Class) sourceParameterizedType.getRawType();

            // 以下数组均为二维
            Type[] targetActualTypes = targetParameterizedType.getActualTypeArguments();
            Type[] sourceActualTypes = sourceParameterizedType.getActualTypeArguments();
            targetKeyType = targetActualTypes[0];
            sourceKeyType = sourceActualTypes[0];
            targetValueType = targetActualTypes[1];
            sourceValueType = sourceActualTypes[1];

            targetKeyRawClass = resloveKeyAndValueType(targetKeyType);
            sourceKeyRawClass = resloveKeyAndValueType(sourceKeyType);
            targetValueRawClass = resloveKeyAndValueType(targetValueType);
            sourceValueRawClass = resloveKeyAndValueType(sourceValueType);

            if (Objects.isNull(targetKeyRawClass) || Objects.isNull(sourceKeyRawClass) ||
                    Objects.isNull(targetValueRawClass) || Objects.isNull(sourceValueRawClass)) {
                resloveSuccess = false;
            }


        }
        return resloveSuccess;

    }

    /**
     * 主类内部名称
     *
     * @return {@link String}
     */
    @Override
    public String getOwnerClassInternalName() {
        String generateClassname = registerContext.geneClassName() + sequence_Local.get().toString();
        String generateClassInternalName = generateClassname.replace('.', '/');
        return generateClassInternalName;
    }

    /**
     * 生成转换指令
     * 详见{@link AbstractComplexTypeStrategy#geneInstruction(ClassWriter, Type, Type, String)}
     *
     * @param extensTransformImplClassWriter
     * @param targetType
     * @param sourceBeanType
     * @param newMethodPrefix
     * @throws Exception 异常
     */
    @Override
    public void geneInstruction(ClassWriter extensTransformImplClassWriter, Type targetType, Type sourceBeanType, String newMethodPrefix) throws Exception {

        MethodVisitor mv = extensTransformImplClassWriter.visitMethod(Opcodes.ACC_PUBLIC + ACC_FINAL, EXTEND_TRANSFORM_METHOD_NAME, EXTEND_TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});
        mv.visitCode();
        final Label startOfMethodLable = new Label();

        final Label endOfMethodLable = new Label();
        // 定义变量
        Map<String, LocalVariableInfo> localVar = new HashMap<>();
        localVar.putAll(defineMethodLocalVar(startOfMethodLable, endOfMethodLable));

        // 方法起始位置打标签
        mv.visitLabel(startOfMethodLable);
        LocalVariableInfo sourceObjectVar = localVar.get("sourceObject");

        LocalVariableInfo targetMap = localVar.get(TARGET_VARIABLE_NAME);
        LocalVariableInfo sourceMap = localVar.get(SOURCE_MAP_VARIABLE_NAME);
        mv.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
        Label transformStart = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, transformStart);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitLabel(transformStart);

        /**
         *   非空分支字节码,
         *   考虑到后期开源，增加英文注释(英文较水，凑合着看)
         */
        // find  subCalss of Map interface ,the subCalss can be instantiated
        Class targetClasImpl = findImplementMapClass(targetMapRawClass);
        mv.visitTypeInsn(Opcodes.NEW, org.objectweb.asm.Type.getInternalName(targetClasImpl));
        mv.visitInsn(Opcodes.DUP);

        if (TreeMap.class == targetClasImpl) {
            // TreeMap  class has 4 consuructs,but no construct contains map capacity parameter,
            // instantiate TreeMap like this：  new TreeMap（）

            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, org.objectweb.asm.Type.getInternalName(targetClasImpl), INIT_METHOD_NAME, "()V", false);
        } else {
            // other implement class of Map can be instantiated with capacity parameter
            mv.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            // source Map object, get Map  size
            mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(Map.class));
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Map.class), "size", "()I", true);

            //  to avoid inner array scale up,please set double size when instantiate Map Object
            mv.visitLdcInsn(Integer.valueOf(1));
            mv.visitInsn(Opcodes.ISHL);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, org.objectweb.asm.Type.getInternalName(targetClasImpl), INIT_METHOD_NAME, "(I)V", false);

        }
        mv.visitVarInsn(Opcodes.ASTORE, targetMap.getIndex());
        mv.visitLabel(targetMap.getStart());

        LocalVariableInfo keySetVar = localVar.get(KEY_SET_VARIABLE_NAME);
        mv.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceMapRawClass));
        mv.visitVarInsn(ASTORE, sourceMap.getIndex());
        mv.visitLabel(sourceMap.getStart());

        LocalVariableInfo tempKeyVar = localVar.get(TEMP_KEY_VARIABLE_NAME);
        LocalVariableInfo tempValueVar = localVar.get(TEMP_VALUE_VARIABLE_NAME);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(Opcodes.ASTORE, tempKeyVar.getIndex());
        mv.visitLabel(tempKeyVar.getStart());
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(Opcodes.ASTORE, tempValueVar.getIndex());
        mv.visitLabel(tempValueVar.getStart());

        //Map  java.util.Set<K> keySet();
        mv.visitVarInsn(ALOAD, sourceMap.getIndex());
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Map.class), "keySet", "()Ljava/util/Set;", true);
        mv.visitVarInsn(Opcodes.ASTORE, keySetVar.getIndex());
        mv.visitLabel(keySetVar.getStart());
        LocalVariableInfo keySetIteratorVar = localVar.get(KEY_SET_ITERATOR_VARIABLE_NAME);
        mv.visitVarInsn(ALOAD, keySetVar.getIndex());

        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "iterator", "()Ljava/util/Iterator;", true);
        mv.visitVarInsn(Opcodes.ASTORE, keySetIteratorVar.getIndex());
        mv.visitLabel(keySetIteratorVar.getStart());


        // while loop ,keySet  iteration
        Label whileJump = new Label();
        Label iteratorGotoLabel = new Label();
        mv.visitLabel(iteratorGotoLabel); // hasNext jump label
        mv.visitVarInsn(Opcodes.ALOAD, keySetIteratorVar.getIndex());
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "hasNext", "()Z", true);
        mv.visitJumpInsn(Opcodes.IFEQ, whileJump);
        mv.visitVarInsn(Opcodes.ALOAD, keySetIteratorVar.getIndex());
        // invoke Iterator next method
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "next", "()Ljava/lang/Object;", true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceKeyRawClass));


        mv.visitVarInsn(Opcodes.ASTORE, tempKeyVar.getIndex());

        //  V get(Object key);
        mv.visitVarInsn(Opcodes.ALOAD, sourceMap.getIndex());
        mv.visitVarInsn(Opcodes.ALOAD, tempKeyVar.getIndex());
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Map.class), "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceValueRawClass));
        mv.visitVarInsn(Opcodes.ASTORE, tempValueVar.getIndex());


        //invoke  Map  put method
        mv.visitVarInsn(Opcodes.ALOAD, targetMap.getIndex());

        /**
         *  transform source Map key and value into target Map key and value,then ,invoke  put() method
         */

        mv.visitVarInsn(Opcodes.ALOAD, SELF_OBJECT_VAR_OFFSET);
        mv.visitFieldInsn(Opcodes.GETFIELD, internalName, keyTransformFieldName(), keyTransformClassDescription);
        // transform key object
        if (keyTransformClassDescription == BEAN_TRANSFORM_DESC) {
            /**
             * invoke {@link BeanTransFormsHandler}  beanTransforms method
             * public abstract Object beanTransforms(Class sourceBeanClass,Object sourceBeanObject, Class targetClass) throws Exception;
             */
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(Opcodes.ALOAD, tempKeyVar.getIndex());
            mv.visitInsn(ACONST_NULL);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, BEAN_TRANSFORM_NAME, BEAN_TRANSFORM_METHOD_NAME, BEAN_TRANSFORM_METHOD_DESC, false);
        } else if (keyTransformClassDescription == EXTENSION_TRANSFORM_INTERFACE_DESC) {
            /**
             * invoke {@link ExtensionObjectTransform} extensionObjectTransform method
             *  public Object extensionObjectTransform(Object sourceObject, boolean deepCopy) throws Exception;
             */
            mv.visitVarInsn(Opcodes.ALOAD, tempKeyVar.getIndex());
            mv.visitInsn(Opcodes.ICONST_1); // boolean true 常量入栈
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EXTENSION_TRANSFORM_INTERFACE_NAME, EXTENSION_TRANSFORM_METHOD_NAME, EXTENSION_TRANSFORM_METHOD_DESC, true);

        }
        // object chechcast targetKeyRawClass
        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(targetKeyRawClass));


        // transform value object
        mv.visitVarInsn(Opcodes.ALOAD, SELF_OBJECT_VAR_OFFSET);
        mv.visitFieldInsn(Opcodes.GETFIELD, internalName, valueTransformFieldName(), valueTransformClassDescription);
        if (valueTransformClassDescription == BEAN_TRANSFORM_DESC) {

            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(Opcodes.ALOAD, tempValueVar.getIndex());
            mv.visitInsn(ACONST_NULL);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, BEAN_TRANSFORM_NAME, BEAN_TRANSFORM_METHOD_NAME, BEAN_TRANSFORM_METHOD_DESC, false);
        } else if (valueTransformClassDescription == EXTENSION_TRANSFORM_INTERFACE_DESC) {
            mv.visitVarInsn(Opcodes.ALOAD, tempValueVar.getIndex());
            mv.visitInsn(Opcodes.ICONST_1); // boolean true 常量入栈
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EXTENSION_TRANSFORM_INTERFACE_NAME, EXTENSION_TRANSFORM_METHOD_NAME, EXTENSION_TRANSFORM_METHOD_DESC, true);
        }
        // object chechcast targetValueRawClass
        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(targetValueRawClass));

        //   V put(K key, V value);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Map.class), "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
        mv.visitInsn(POP); // POP return value

        //jump  whileJump label
        mv.visitJumpInsn(Opcodes.GOTO, iteratorGotoLabel);
        mv.visitLabel(whileJump);

        // 退出循环，返回结果
        mv.visitVarInsn(Opcodes.ALOAD, targetMap.getIndex());
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(endOfMethodLable);
        // 定义局部变量表
        visitLocalVarivale(localVar, mv);
        // 方法末位位置打标签
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }


    /**
     * find the implement class of interface or abstract class
     *
     * @param original 原始接口类或者抽象类
     * @return {@link Class}
     */
    private Class findImplementMapClass(Class original) {

        if ((!Modifier.isAbstract(original.getModifiers())) && (!Modifier.isPublic(original.getModifiers()))) {
            return original;
        } else {
            if (SortedMap.class.isAssignableFrom(original)) {
                // new TreeMap();
                return TreeMap.class;
            } else if (ConcurrentMap.class.isAssignableFrom(original)) {
                // new ConcurrentHashMap(0);
                return ConcurrentHashMap.class;
            } else if (Dictionary.class.isAssignableFrom(original)) {
                //  new Hashtable<>(16);
                return Hashtable.class;
            } else {
                //  new HashMap(16);
                return HashMap.class;
            }
        }

    }

    /**
     * 定义方法局部变量
     *
     * @param startOfMethodBeanTransformsLable 方法体开始标签
     * @param endOfMethodBeanTransformsLable   年底bean标签转换方法
     * @return {@link Map}
     */
    protected Map<String, LocalVariableInfo> defineMethodLocalVar(Label startOfMethodBeanTransformsLable, Label endOfMethodBeanTransformsLable) {


        // 定义所有局部变量
        Map<String, LocalVariableInfo> localVariableInfoMap = new HashMap<>();
        // 除去函数参数3个(编号0、1、2)，defineMethodParameterVar 方法创建三个参数变量
        // 方法内部变量编号从3开始累加
        int varNum = 3; //

        // internalName 在调用前置函数geneTransform()时生成，如果为空，则通过 getOwnerClassInternalName() 生成
        if (internalName.isEmpty()) {
            internalName = getOwnerClassInternalName();
        }
        // 方法参数
        localVariableInfoMap.putAll(defineMethodParameterVar(startOfMethodBeanTransformsLable, endOfMethodBeanTransformsLable,
                internalName));

        // 方法内部自定义变量
        LocalVariableInfo newVariableInfo = new VariableDefine().alias(TARGET_VARIABLE_NAME).name(TARGET_VARIABLE_NAME)
                .descriptor(org.objectweb.asm.Type.getDescriptor(targetMapRawClass))
                .signature(org.objectweb.asm.Type.getDescriptor(targetMapRawClass))
                .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                .define();
        localVariableInfoMap.put(TARGET_VARIABLE_NAME, newVariableInfo);

        LocalVariableInfo sourceMapVariableInfo = new VariableDefine().alias(SOURCE_MAP_VARIABLE_NAME).name(SOURCE_MAP_VARIABLE_NAME)
                .descriptor(org.objectweb.asm.Type.getDescriptor(sourceMapRawClass))
                .signature(org.objectweb.asm.Type.getDescriptor(sourceMapRawClass))
                .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                .define();
        localVariableInfoMap.put(SOURCE_MAP_VARIABLE_NAME, sourceMapVariableInfo);

        LocalVariableInfo iteratorVariableInfo = new VariableDefine().alias(KEY_SET_ITERATOR_VARIABLE_NAME).name(KEY_SET_ITERATOR_VARIABLE_NAME)
                .descriptor(org.objectweb.asm.Type.getDescriptor(Iterator.class))
                .signature(org.objectweb.asm.Type.getDescriptor(Iterator.class))
                .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                .define();
        localVariableInfoMap.put(KEY_SET_ITERATOR_VARIABLE_NAME, iteratorVariableInfo);


        LocalVariableInfo keySetVariableInfo = new VariableDefine().alias(KEY_SET_VARIABLE_NAME).name(KEY_SET_VARIABLE_NAME)
                .descriptor(org.objectweb.asm.Type.getDescriptor(Set.class))
                .signature(org.objectweb.asm.Type.getDescriptor(Set.class))
                .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                .define();
        localVariableInfoMap.put(KEY_SET_VARIABLE_NAME, keySetVariableInfo);

        LocalVariableInfo tempKeyVariableName = new VariableDefine().alias(TEMP_KEY_VARIABLE_NAME).name(TEMP_KEY_VARIABLE_NAME)
                .descriptor(org.objectweb.asm.Type.getDescriptor(sourceKeyRawClass))
                .signature(org.objectweb.asm.Type.getDescriptor(sourceKeyRawClass))
                .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                .define();
        localVariableInfoMap.put(TEMP_KEY_VARIABLE_NAME, tempKeyVariableName);

        LocalVariableInfo tempValueVariableName = new VariableDefine().alias(TEMP_VALUE_VARIABLE_NAME).name(TEMP_VALUE_VARIABLE_NAME)
                .descriptor(org.objectweb.asm.Type.getDescriptor(sourceValueRawClass))
                .signature(org.objectweb.asm.Type.getDescriptor(sourceValueRawClass))
                .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                .define();
        localVariableInfoMap.put(TEMP_VALUE_VARIABLE_NAME, tempValueVariableName);


        return localVariableInfoMap;

    }

    /**
     * Map 类型 key 值类型转换字段名
     *
     * @return {@link String}
     */
    private String keyTransformFieldName() {
        return registerContext.getIdentify() + EXTEND_IMPL_FIELD_NAME_SUFFIX + "_key";
    }

    /**
     * Map 类型 value 值类型转换字段名
     *
     * @return {@link String}
     */
    private String valueTransformFieldName() {
        return registerContext.getIdentify() + EXTEND_IMPL_FIELD_NAME_SUFFIX + "_value";
    }

    /**
     * Map类型对应的转换类作为上层类中一个字段，mapTransformFieldName生成具体的字段名
     *
     * @return {@link String}
     */
    private String mapTransformFieldName() {
        return registerContext.getIdentify() + EXTEND_IMPL_FIELD_NAME_SUFFIX;
    }

    /**
     * 策略匹配
     *
     * @param targetValueType 目标类是Map类型，targetValueType 表示Map 的value类型
     * @param sourceKeyType   源类是Map类型，sourceKeyType 表示Map 的key类型
     * @return boolean
     */
    private boolean mapMatch(Type targetValueType, Type sourceKeyType) {


        return false;
    }

    /**
     * 通过ClassWriter 生成类字段
     *
     * @param classWriter
     * @param transform          转换类对象，调用该方法前需要先生成对应字段的转换对象
     * @param transformFieldName 转换类在上层类所属的字段的名称
     * @return {@link String}
     */
    protected static String writeField(ClassWriter classWriter, Transform transform, String transformFieldName) {

        String transformFieldTypeDesc = "";
        if (transform instanceof BeanTransFormsHandler) {
            transformFieldTypeDesc = BEAN_TRANSFORM_DESC;
            classWriter.visitField(ACC_PRIVATE,
                    transformFieldName,
                    BEAN_TRANSFORM_DESC,
                    null,
                    null).visitEnd();
        } else if (transform instanceof ExtensionObjectTransform) {
            transformFieldTypeDesc = EXTENSION_TRANSFORM_INTERFACE_DESC;
            classWriter.visitField(ACC_PRIVATE,
                    transformFieldName,
                    EXTENSION_TRANSFORM_INTERFACE_DESC,
                    null,
                    null).visitEnd();
        }
        return transformFieldTypeDesc;
    }

    /**
     * 重写父类方法
     * 详见{@link AbstractComplexTypeStrategy#geneTransform(Type, Type, String, String)}
     *
     * @param sourceBeanType    源bean类型
     * @param targetType        目标类型
     * @param generateClassname 生成类名
     * @param fieldNamePrefix   字段名称前缀
     * @return {@link Map}
     * @throws Exception 异常
     */
    @Override
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception {
        // 传入类名基础上增加编号
        generateClassname = generateClassname + "$" + sequence_Local.get().toString();
        if (!resloveMap(targetType, sourceBeanType)) {
            LOG.warn("sourceBeanType：{}   {}，targetType：{}   {}，不满足map 类型转换条件，默认空值转换，请自定义com.shzz.common.tool.bean.transform.ExtensionObjectTransform 接口实现转换", sourceBeanType.getTypeName(), sourceBeanType.getClass().getSimpleName(), targetType.getTypeName(), targetType.getClass().getSimpleName());
            return new DefaultComplexTypeStrategy(registerContext).geneTransform(sourceBeanType, targetType, generateClassname, fieldNamePrefix);
        }
        //todo 增加保护

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        checkGenerateClassname(generateClassname);
        internalName = generateClassname.replace('.', '/');

        // 创建类
        classWriter.visit(getClassVersion(),
                ACC_PUBLIC + ACC_FINAL,
                internalName,
                null,
                OBJECT_CLASS_INTERNAL_NAME, new String[]{EXTENSION_TRANSFORM_CLASS_INTERNAL_NAME});


        // 生成 Map  key  value 的转换对象

        TransformTypeContext keyTransformTypeContext = new TransformTypeContext(((TransformTypeContext) registerContext));
        Transform keyTransform = (Transform) keyTransformTypeContext.geneTransform(targetKeyType, sourceKeyType, fieldNamePrefix).values().toArray()[0];
        TransformTypeContext valueTransformTypeContext = new TransformTypeContext(((TransformTypeContext) registerContext));
        Transform valueTransform = (Transform) valueTransformTypeContext.geneTransform(targetValueType, sourceValueType, fieldNamePrefix).values().toArray()[0];

        //  map 转换类的字段中记录  key 和 value 转换对象 ，类文件先生成字段信息，实例化后给对应字段赋值
        keyTransformClassDescription = writeField(classWriter, keyTransform, keyTransformFieldName());
        valueTransformClassDescription = writeField(classWriter, valueTransform, valueTransformFieldName());

        // 构造函数
        MethodVisitor extensTransformImplClassInit = classWriter.visitMethod(ACC_PUBLIC,
                INIT_METHOD_NAME,
                INIT_METHOD_DESCRIPTOR,
                null,
                null);

        extensTransformImplClassInit.visitVarInsn(Opcodes.ALOAD, 0);
        extensTransformImplClassInit.visitMethodInsn(Opcodes.INVOKESPECIAL, OBJECT_CLASS_INTERNAL_NAME, INIT_METHOD_NAME, INIT_METHOD_DESCRIPTOR, false);
        extensTransformImplClassInit.visitInsn(Opcodes.RETURN);
        extensTransformImplClassInit.visitMaxs(1, 1);
        extensTransformImplClassInit.visitEnd();

        /**
         * 复杂类型转换接口{@link com.shzz.common.tool.bean.transform.ExtensionObjectTransform} 方法实现，基于源类和目标类生成对应转换字节码
         * public Object extensionObjectTransform(Object sourceObject, boolean deepCopy) throws Exception;
         */
        geneInstruction(classWriter, targetType, sourceBeanType, fieldNamePrefix);
        classWriter.visitEnd();

        byte[] classBytes = classWriter.toByteArray();
        Class extendClassImpl = loadASMGenerateClass(classBytes, generateClassname);
        Constructor<?> constructor = extendClassImpl.getDeclaredConstructor();//默认构造方法；
        // 通过ClassWriter 生成的类已指定实现接口 com.shzz.common.tool.bean.transform.ExtensionObjectTransform，可强转
        ExtensionObjectTransform extensionObjectTransform = (ExtensionObjectTransform) constructor.newInstance();

        Field keyTransformField = extendClassImpl.getDeclaredField(keyTransformFieldName());
        keyTransformField.setAccessible(true);
        keyTransformField.set(extensionObjectTransform, keyTransform);

        Field valueTransformField = extendClassImpl.getDeclaredField(valueTransformFieldName());
        valueTransformField.setAccessible(true);
        valueTransformField.set(extensionObjectTransform, valueTransform);
        Map<String, ExtensionObjectTransform> innerExtensionObjectTransformMap = new HashMap<>(4);

        innerExtensionObjectTransformMap.put(mapTransformFieldName(), extensionObjectTransform);
        clearThreadLocal();
        return innerExtensionObjectTransformMap;
    }

    /**
     * 重写父类方法
     * 详见{@link AbstractComplexTypeStrategy#strategyMatch(Type, Type)}
     *
     * @param sourceBeanType 源bean类型
     * @param targetType     目标类型
     * @return boolean
     * @throws Exception 异常
     */
    @Override
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws Exception {
        boolean match = false;

        if ((sourceBeanType instanceof ParameterizedType) && (targetType instanceof ParameterizedType)) {
            Class sourceBeanRawClass = (Class) ((ParameterizedType) sourceBeanType).getRawType();
            Class targetRawClass = (Class) ((ParameterizedType) targetType).getRawType();
            if (Map.class.isAssignableFrom(sourceBeanRawClass) && Map.class.isAssignableFrom(targetRawClass)) {
                match = true;
            }

        }

        if (!match) {

            // 每次构造方法创建对象sequence会累加1，如果最终不匹配对应策略则回到原值

            if (Objects.nonNull(sequence_Local.get())) {

                sequence_Local.set(sequence_Local.get() - 1);
            }

        }
        return match;
    }

    /**
     * 清理threadlocal
     */
    @Override
    public void clearThreadLocal(){
        super.clearThreadLocal();

    }
}
