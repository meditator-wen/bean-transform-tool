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
package com.shzz.common.tool.bean.transform.asm;


import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.SystemProperties;
import com.shzz.common.tool.bean.transform.asm.context.Context;
import com.shzz.common.tool.bean.transform.asm.context.TransformTypeContext;
import com.shzz.common.tool.bean.transform.asm.strategy.UniversalClassTypeStrategy;
import com.shzz.common.tool.code.BeanTransformException;


import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.*;
import static org.objectweb.asm.Opcodes.ASM9;

/**
 * bean转换方法适配器
 *
 * @author wen wang
 * @date 2021/12/6 10:04
 */
public class BeanTransformsMethodAdapter extends MethodVisitor {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger("BeanTransformsMethodAdapter");

    /**
     * init 构造方法名称
     */
    public static final String INIT_METHOD_NAME = "<init>";
    /**
     * init方法描述符
     */
    public static final String INIT_METHOD_DESCRIPTOR = "()V";
    /**
     * 局部变量前缀
     */
    public static final String LOCAL_VAR_PREFIX = "localVar_";
    /**
     * 原始类型checkcast 转换类
     */
    public static final String LOCAL_SOURCE_CAST_VAR_NAME = "localVar_sourceCast";

    /**
     * this 变量索引
     */
    public static final int SELF_OBJECT_VAR_OFFSET = 0;
    /**
     * 扩展转换接口名称，intername name
     */
    public static final String EXTENSION_TRANSFORM_INTERFACE_NAME = org.objectweb.asm.Type.getInternalName(ExtensionObjectTransform.class);
    /**
     * ExtensionObjectTransform
     */
    public static final String EXTENSION_TRANSFORM_INTERFACE_DESC = org.objectweb.asm.Type.getDescriptor(ExtensionObjectTransform.class);
    /**
     * BeanTransFormsHandler 类描述信息
     */
    public static final String BEAN_TRANSFORM_DESC = org.objectweb.asm.Type.getDescriptor(BeanTransFormsHandler.class);
    /**
     * BeanTransFormsHandler 类名
     */
    public static final String BEAN_TRANSFORM_NAME = org.objectweb.asm.Type.getInternalName(BeanTransFormsHandler.class);
    /**
     * Class类描述信息
     */
    public static final String FIELD_TYPE_DESC = org.objectweb.asm.Type.getDescriptor(Class.class);
    /**
     * Date类型内部名称
     */
    public static final String DATA_TYPE_INTERNAL_NAME = org.objectweb.asm.Type.getInternalName(Date.class);
    /**
     * Date类型类型init方法描述符
     */
    public static final String DATA_TYPE_INIT_METHOD_DESCRIPTOR = "(J)V";
    /**
     * extensionObjectTransform 方法名
     * {@link ExtensionObjectTransform#extensionObjectTransform(Object, boolean)}
     */
    public static final String EXTENSION_TRANSFORM_METHOD_NAME = "extensionObjectTransform";
    /**
     * bean转换内部方法名
     */
    public static final String BEAN_TRANSFORM_METHOD_NAME = "beanTransforms";
    /**
     * bean转换外部接口方法名，带泛型
     */
    public static final String PUBLIC_BEAN_TRANSFORM_METHOD_NAME = "beanTransform";
    /**
     * extensionObjectTransform 方法描述符
     * {@link ExtensionObjectTransform#extensionObjectTransform(Object, boolean)}
     */
    public static final String EXTENSION_TRANSFORM_METHOD_DESC = "(Ljava/lang/Object;Z)Ljava/lang/Object;";
    /**
     * bean转换方法描述符
     * {@link BeanTransFormsHandler#beanTransforms(Class, Object, Class)}
     */
    public static final String BEAN_TRANSFORM_METHOD_DESC = "(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;";
    /**
     * 预留字段，暂未使用
     */
    public static final String GENERIC_TRANSFORM_METHOD_DESC = "<S:Ljava/lang/Object;T:Ljava/lang/Object;>(Ljava/lang/Class<TS;>;TS;Ljava/lang/Class<TT;>;)TS;";
    /**
     * 源类类型
     */
    private final Class<?> sourceBeanClass;
    /**
     * 目标类
     */
    private final Class<?> targetClass;
    /**
     * 深拷贝标志
     */
    private final boolean isDeepCopy;
    /**
     * ASM生成类类名
     */
    private final String generateClassname;
    /**
     * 是否允许不相同的基类互相转换
     */
    private final boolean permitBaseTypeInterconvert;
    /**
     * 方法内部变量索引序列
     */
    private volatile int varOffset = 4;

    /**
     * 递归次数
     */
    private int recursionTimes = 1;

    /**
     * castsource bean变量信息
     */
    LocalVariableInfo castsourceBeanVariableInfo = null;

    //
    // visitCode 方法会遍历该map并写入以下属性

    /**
     * 局部变量缓存
     * 记录方法体中创建的变量列表信息，method  code 属性 中的LocalVariableTable 属性，具体请查看java虚拟机说明
     * visitCode 方法会遍历该map并写入以下属性
     */
    private final Map<String, LocalVariableInfo> localVariableMap = new ConcurrentHashMap<>();

    /**
     * 方法起始标签
     */
    private final Label startOfMethodBeanTransformsLable = new Label();
    /**
     * 方法结束标签
     */
    private final Label endOfMethodBeanTransformsLable = new Label();
    /**
     * 转换代码其实标签，方法体前面会定义变量，实际转换的代码逻辑标签位在变量定义之后
     */
    Label transformStart = new Label();
    /**
     * 每个字段转换代码的终止位置
     */
    private final Label fieldProcessTerminate = new Label();

    /**
     * 方法退出标签
     */
    private Map<Integer, Label> recurisionExitLabel = new ConcurrentHashMap<>(64);
    /**
     * 跳转的标签位置
     */
    private Map<Integer, Label> nextFieldJumpLabel = new ConcurrentHashMap<>(64);

    /**
     * bean转换方法适配器
     *
     * @param mv                          mv
     * @param sourceBeanClass             源类
     * @param targetClass                 目标类
     * @param isDeepCopy                  深拷贝
     * @param permitWrapsTypeInterconvert 是否允许基础类型互转
     * @param generateClassname           生成类名
     */
    public BeanTransformsMethodAdapter(MethodVisitor mv,
                                       Class<?> sourceBeanClass,
                                       Class<?> targetClass,
                                       boolean isDeepCopy,
                                       boolean permitWrapsTypeInterconvert,
                                       String generateClassname
    ) {
        super(ASM9, mv);
        this.sourceBeanClass = sourceBeanClass;
        this.targetClass = targetClass;
        this.isDeepCopy = isDeepCopy;
        this.generateClassname = generateClassname;
        this.permitBaseTypeInterconvert = permitWrapsTypeInterconvert;

    }


    /**
     * 通过源类对象get 方法获取的字段值存储于变量中，在递归处理时内部方法需要查询上层获取的字段值，找到存储的
     *
     * @param recursions             递归
     * @param tempSourceObjectVarNum 临时var num源对象
     * @return int
     */
    public int findSourceObjectIndex(int recursions, int tempSourceObjectVarNum) {
        //第一次调用取beanTransforms方法参数的sourceObject 对象变量，变量编号为1
        // public abstract Object beanTransforms(Class sourceBeanClass,Object sourceBeanObject, Class targetClass) throws Exception;

        int sourceObjectIndex = 2; //默认值
        if (recursions > 1) {
            // 定位变量别名后缀计算规则 (recursions - tempSourceObjectVarNum) + "_"+tempSourceObjectVarNum
            String sourceObjectVarAlias = LOCAL_VAR_PREFIX + (recursions - tempSourceObjectVarNum) + "_" + tempSourceObjectVarNum;
            sourceObjectIndex = localVariableMap.get(sourceObjectVarAlias).getIndex();
        }

        return sourceObjectIndex;

    }

    /**
     * 新临时变量
     *
     * @param sourceClassInternalName 源类内部名称
     * @param resloveInfo             解决信息
     * @param startVarOffSetRecursion 开始var设置递归
     * @param preSourceObjectVarNum   var num pre源对象
     * @param recursions              递归
     * @return {@link LocalVariableInfo}
     */
    private synchronized LocalVariableInfo newTempVar(String sourceClassInternalName,
                                                      ResloveInfo resloveInfo,
                                                      int startVarOffSetRecursion,
                                                      int preSourceObjectVarNum,
                                                      int recursions) {

        // 通过get 方法获取的变量，先存入局部变量表，供下轮递归使用（只针对fieldType是自定义类型的情况）

        // 2022-01-29 新增，为了提高效率，sourceObject参数 先转成对应类对象存在新变量中，每次加载转换后的变量，避免多次转换
        mv.visitVarInsn(Opcodes.ALOAD, castsourceBeanVariableInfo.getIndex());

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sourceClassInternalName, resloveInfo.getSourceFieldGetFunctionName(), resloveInfo.getSourceFieldGetFunctionDescriptor(), false);
        int newTempSourceObjectVarNum = startVarOffSetRecursion;
        // 本次递归的第二个，编号为1
        String tempSourceObjectVarAlias = LOCAL_VAR_PREFIX + recursions + "_" + (newTempSourceObjectVarNum);

        LocalVariableInfo tempSourceObjectVarInfo = new VariableDefine().alias(tempSourceObjectVarAlias).name(tempSourceObjectVarAlias).descriptor(org.objectweb.asm.Type.getDescriptor(resloveInfo.getSourceFieldType()))
                .signature(org.objectweb.asm.Type.getDescriptor(resloveInfo.getSourceFieldType())).start(new Label()).end(endOfMethodBeanTransformsLable).index(varOffset++)
                .define();
        localVariableMap.put(tempSourceObjectVarAlias, tempSourceObjectVarInfo);
        mv.visitVarInsn(Opcodes.ASTORE, tempSourceObjectVarInfo.getIndex()); // 下轮递归使用该变量值

        mv.visitLabel(tempSourceObjectVarInfo.getStart()); // 变量打标签
        return tempSourceObjectVarInfo;

    }


    /**
     * 生成转化代码字节码的核心函数，上层调用着{@link BeanTransformsMethodAdapter#visitCode()}
     *
     * @param sourceBeanClass        源类
     * @param targetClass            目标类
     * @param recursions             递归层级，备注，原设计是直接递归生成复杂字段的转换字节码，后面设计调整，改成生成复杂字段的转换类对象，上层调用转换对象的转换方法
     * @param tempSourceObjectVarNum 临时对象变量索引
     */
    private synchronized void visitCodeRecursion(Class<?> sourceBeanClass,
                                                 Class<?> targetClass,
                                                 int recursions,
                                                 int tempSourceObjectVarNum) {


        // 以下情况在递归调用visitCodeRecursion前已做处理，本次递归直接将null 入栈，返回
        if ((targetClass == null) || targetClass.isAssignableFrom(Object.class)) {

            mv.visitInsn(Opcodes.ACONST_NULL);
            return;

        } else if ((sourceBeanClass == null) || (sourceBeanClass == Object.class)) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        } else if ((sourceBeanClass.isArray()) || targetClass.isArray()) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        } else if (TypeTransformAssist.isBaseType(targetClass) && TypeTransformAssist.isBaseType(sourceBeanClass)) {

            mv.visitVarInsn(Opcodes.ALOAD, findSourceObjectIndex(recursions, tempSourceObjectVarNum));
            mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceBeanClass));

            Class targetClassMap = null;
            if (TypeTransformAssist.isPrimitiveType(targetClass)) {
                // 接口方法返回Object,如果是原始类型，无法直接ARETURN 指令返回，需要转成包装类。
                targetClassMap = TypeTransformAssist.typeMap(targetClass);

            } else {
                targetClassMap = targetClass;
            }
            try {
                TypeTransformAssist.baseTypeProcessByteCode(targetClassMap, sourceBeanClass, mv, isDeepCopy);

            } catch (Exception e) {
                ErrorInfoStack.getExceptionStackInfo(e);
            }

            return;

        } else if (TypeTransformAssist.isBaseType(targetClass) && (!TypeTransformAssist.isBaseType(sourceBeanClass))) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        } else if (TypeTransformAssist.isBaseType(sourceBeanClass) && (!TypeTransformAssist.isBaseType(targetClass))) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }


        int startVarOffSetRecursion = 0; // 本轮递归，非基础类型引用变量名初始后缀编号

        // 第一个方法内局部变量(目标类对象，通过new 创建)定义位置。 命名规则 "localVar_"+recursions+"_"+varOffSet，内层嵌套的代码块需要访问上层已定义的变量
        String recursionFunctionVarNameAtStart = LOCAL_VAR_PREFIX + recursions + "_" + (startVarOffSetRecursion++);

        LocalVariableInfo localVariableInfo = new VariableDefine().alias(recursionFunctionVarNameAtStart)
                .name(recursionFunctionVarNameAtStart)
                .descriptor(org.objectweb.asm.Type.getDescriptor(targetClass))
                .signature(org.objectweb.asm.Type.getDescriptor(targetClass))
                .index(varOffset++)
                .start(new Label())
                .end(endOfMethodBeanTransformsLable)
                .define();
        localVariableMap.put(localVariableInfo.getAlias(), localVariableInfo);


        /**
         *     The internal name of a class is its fully qualified name (as returned by Class.getName(), where '.' are
         *     replaced by '/')
         *     TODO 增加保护逻辑，异常抛出
         */

        // String targetClassInternalName=sourceBeanClass.getName().replace('.', '/');
        String targetClassInternalName = org.objectweb.asm.Type.getType(targetClass).getInternalName(); // 与注释描述写法等效
        String sourceClassInternalName = org.objectweb.asm.Type.getType(sourceBeanClass).getInternalName(); // 与注释描述写法等效
        /**
         * 引用类型，则创建目标中间类变量供下轮转换使用
         */

        try {
            Constructor constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            if (!Modifier.isPublic(constructor.getModifiers())) {
                LOG.warn("targetClass={} 无 public 构造方法，无法调用 new 创建对象 ", targetClass.getSimpleName());
                mv.visitInsn(Opcodes.ACONST_NULL);
                return;
            }
        } catch (NoSuchMethodException e) {
            LOG.warn("targetClass={} 无构造方法，无法调用 new 创建对象 ", targetClass.getSimpleName());
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }


        mv.visitTypeInsn(Opcodes.NEW, targetClassInternalName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, targetClassInternalName, INIT_METHOD_NAME, INIT_METHOD_DESCRIPTOR, false);
        mv.visitVarInsn(Opcodes.ASTORE, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
        //第一个变量位置打标签，创建的目标类对象
        mv.visitLabel(localVariableMap.get(recursionFunctionVarNameAtStart).getStart());


        // 第二个方法内局部变量(源类对象，通过sourceObject 变量 cast 转换)定义位置。 命名规则 "localVar_"+recursions+"_"+varOffSet，内层嵌套的代码块需要访问上层已定义的变量
        String castSourceVarName = LOCAL_VAR_PREFIX + recursions + "_" + (startVarOffSetRecursion++);

        //LocalVariableInfo
        castsourceBeanVariableInfo = new VariableDefine().alias(castSourceVarName).descriptor(org.objectweb.asm.Type.getDescriptor(sourceBeanClass))
                .name(castSourceVarName).signature(org.objectweb.asm.Type.getDescriptor(sourceBeanClass))
                .start(new Label()).end(endOfMethodBeanTransformsLable)
                .index(varOffset++).define();
        localVariableMap.put(castsourceBeanVariableInfo.getAlias(), castsourceBeanVariableInfo);
        mv.visitVarInsn(Opcodes.ALOAD, findSourceObjectIndex(recursions, tempSourceObjectVarNum));

        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceBeanClass));

        mv.visitVarInsn(Opcodes.ASTORE, castsourceBeanVariableInfo.getIndex());
        mv.visitLabel(castsourceBeanVariableInfo.getStart());

        Field[] targetBeanFields = targetClass.getDeclaredFields();
        if (Objects.isNull(targetBeanFields)) {
            mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
            return;
        }


        List<Field> effectiveFieldList = new ArrayList<>(); // 浅拷贝模式下统一字节码处理


        //  浅拷贝模式直接对象赋值，转换异常直接抛出，深拷贝需要特殊处理，引用类型字段需要重建新对象赋值
        int num = 0;
        for (Field field : targetBeanFields) {
            if (Objects.isNull(field)) {
                LOG.warn("第{}个字段反射为空，略过", num);
                ++num;
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) {
                LOG.warn("第{}个字段S是final 字段，略过", num);
                ++num;
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                LOG.warn("第{}个字段是static  字段，略过", num);
                ++num;
                continue;
            }
            field.setAccessible(true);
            effectiveFieldList.add(field);
            ++num;
        }
        if (effectiveFieldList.isEmpty()) {
            mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
            return;
        }

        int fieldBranchs = effectiveFieldList.size();
        Label[] fieldBranchEntranceLabel = new Label[fieldBranchs];
        Label[] fieldBranchExitLabel = new Label[fieldBranchs];
        Label recurisonExist = new Label();
        recurisionExitLabel.put(recursions, recurisonExist);
        for (int pointer = 0; pointer < fieldBranchs; ++pointer) {
            //各个字段转换代码的起始位置标签定义，每次进入一个字段处理单元预先打标签，便于后面处理if 分支做跳转
            fieldBranchEntranceLabel[pointer] = new Label();
            fieldBranchExitLabel[pointer] = new Label();

        }
        int fieldOffset = 0;
        for (Field iterField : effectiveFieldList) {

            ResloveInfo resloveInfo = TypeTransformAssist.reslove(iterField, targetClass, sourceBeanClass);
            if (!TypeTransformAssist.resloveInfoCheck(resloveInfo)) {
                continue;
            }

            // 浅拷贝模式下，如果源类和目标类 对应字段是同一类型，则可以执行，否则只能深拷贝处理
            String iterFiledClassName = iterField.getType().getName();
            String sourceFiledClassName = resloveInfo.getSourceFieldType().getName();
            if ((!isDeepCopy) && (!TypeTransformAssist.isBaseType(iterField.getType())) && (!(iterField.getType().isAssignableFrom(resloveInfo.getSourceFieldType())))) {
                LOG.warn("浅拷贝模式。目标类字段{} {} 与源类对应字段{} {} 非父子类关系，无法浅拷贝，忽略该字段," +
                                "如有需要可重新设置 BeanTransFormsHandler generate isDeepCopy 参数为true",
                        iterFiledClassName,
                        iterField.getName(),
                        sourceFiledClassName,
                        resloveInfo.getSourceFieldName());
                continue;

            }


            if ((!permitBaseTypeInterconvert) && TypeTransformAssist.isBaseType(iterField.getType())
                    && (!(iterFiledClassName.equals(sourceFiledClassName)))) {
                LOG.warn("指定基础类型不允许相互转换模式。目标类字段{} {} 与源类对应字段{} {} 类型不一致，不做转换，忽略该字段," +
                                "如有需要可重新设置 BeanTransFormsHandler generate 方法 permitBaseTypeInterconvert 参数为true",
                        iterFiledClassName,
                        iterField.getName(),
                        sourceFiledClassName,
                        resloveInfo.getSourceFieldName());
                continue;
            }

            if (TypeTransformAssist.isBaseType(iterField.getType()) && (!TypeTransformAssist.isBaseType(resloveInfo.getSourceFieldType()))) {
                LOG.warn("目标类字段{} {} 是原始类型或包装类性,源类对应字段{} {} 类型非包装类或者原始类型,不予转换，忽略该字段,",
                        iterFiledClassName,
                        iterField.getName(),
                        sourceFiledClassName,
                        resloveInfo.getSourceFieldName());
                continue;
            }

            //调用目标类字段对应的源类字段的get 方法

            // 字段的get  方法，无参数，字节码指令参数不入栈
            if (TypeTransformAssist.resloveInfoCheck(resloveInfo)) {
                Type filedGenericType = iterField.getGenericType();
                String generateClassInternalName = generateClassname.replace('.', '/');

                // 数组类对象，参数化泛型、类型变量、泛型数组、通配泛型
                boolean flag = (filedGenericType instanceof GenericArrayType) ||
                        (filedGenericType instanceof TypeVariable) ||
                        (filedGenericType instanceof WildcardType) ||
                        (filedGenericType instanceof ParameterizedType) ||
                        ((filedGenericType instanceof Class) && (((Class) filedGenericType).isArray()));


                if (resloveInfo.isUserExtend() || flag) {
                    mv.visitLabel(fieldBranchEntranceLabel[fieldOffset]);
                    LocalVariableInfo tempSourceObjectVarInfo = newTempVar(sourceClassInternalName, resloveInfo, startVarOffSetRecursion++, tempSourceObjectVarNum, recursions);
                    mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
                    // 使用自定义拓展类转换
                    mv.visitVarInsn(Opcodes.ALOAD, SELF_OBJECT_VAR_OFFSET);
                    // 从缓存字段中加载转换类对象。(需要在创建转换类时预先写入该字段值)
                    mv.visitFieldInsn(Opcodes.GETFIELD, generateClassInternalName, iterField.getName() + EXTEND_IMPL_FIELD_NAME_SUFFIX, EXTENSION_TRANSFORM_INTERFACE_DESC);
                    // sourceObject 参数入栈
                    mv.visitVarInsn(Opcodes.ALOAD, tempSourceObjectVarInfo.getIndex());
                    if (isDeepCopy) {
                        // boolean true 常量入栈
                        mv.visitInsn(Opcodes.ICONST_1);
                    } else {
                        // boolean false 常量入栈
                        mv.visitInsn(Opcodes.ICONST_0);
                    }
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EXTENSION_TRANSFORM_INTERFACE_NAME, EXTENSION_TRANSFORM_METHOD_NAME, EXTENSION_TRANSFORM_METHOD_DESC, true);

                    mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(iterField.getType()));
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, targetClassInternalName, resloveInfo.getTargetFieldSetFunctionName(), resloveInfo.getTargetFieldSetFunctionDescriptor(), false);


                } else {

                    if ((!isDeepCopy) || (TypeTransformAssist.isBaseType(iterField.getType()) &&
                            TypeTransformAssist.isBaseType(resloveInfo.getSourceFieldType()))) {

                        mv.visitLabel(fieldBranchEntranceLabel[fieldOffset]);
                        Label jumpIfNull = new Label();
                        if (TypeTransformAssist.isWrapsOrStringType(resloveInfo.getSourceFieldType()) &&
                                (SystemProperties.getWrapsTypeDeepyCopyFlag() || (iterField.getType() != resloveInfo.getSourceFieldType()))) {

                            //   2022-01-29 新增，为了提高效率，sourceObject参数 先转成对应类对象存在新变量中，每次加载转换后的变量，避免多次转换
                            mv.visitVarInsn(Opcodes.ALOAD, castsourceBeanVariableInfo.getIndex());
                            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sourceClassInternalName, resloveInfo.getSourceFieldGetFunctionName(), resloveInfo.getSourceFieldGetFunctionDescriptor(), false);
                            mv.visitJumpInsn(Opcodes.IFNULL, jumpIfNull);
                        }

                        //变量targetObjectVar 入栈，调用set方法，基础类型的源类字段
                        mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
                        // 2022-01-29 新增，为了提高效率，sourceObject参数 先转成对应类对象存在新变量中，每次加载转换后的变量，避免多次转换
                        mv.visitVarInsn(Opcodes.ALOAD, castsourceBeanVariableInfo.getIndex());

                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sourceClassInternalName, resloveInfo.getSourceFieldGetFunctionName(), resloveInfo.getSourceFieldGetFunctionDescriptor(), false);

                        // 基础类型的源类字段和目标类字段做转换处理
                        if (TypeTransformAssist.isBaseType(iterField.getType()) &&
                                TypeTransformAssist.isBaseType(resloveInfo.getSourceFieldType())) {
                            try {
                                TypeTransformAssist.baseTypeProcessByteCode(iterField.getType(), resloveInfo.getSourceFieldType(), mv, isDeepCopy);
                            } catch (Exception e) {
                                LOG.error(e.toString());
                            }
                        }

                        // 第二步，set 函数调用指令获取给本类字段赋值,(上一次递归有可能插入了null 常量，可以直接赋值给目标对象字段)
                        if ((!isDeepCopy) && (iterField.getType().isAssignableFrom(resloveInfo.getSourceFieldType()))) {
                            //源类字段和目标类字段有继承关系时，做浅拷贝模式要先强制转换为父类型然后目标类对象set 方法
                            mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(iterField.getType()));
                        }

                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, targetClassInternalName, resloveInfo.getTargetFieldSetFunctionName(), resloveInfo.getTargetFieldSetFunctionDescriptor(), false);

                        if (TypeTransformAssist.isWrapsOrStringType(resloveInfo.getSourceFieldType()) &&
                                (SystemProperties.getWrapsTypeDeepyCopyFlag() || (iterField.getType() != resloveInfo.getSourceFieldType()))) {

                            mv.visitLabel(jumpIfNull);
                            if (fieldOffset < fieldBranchs - 1) {
                                mv.visitJumpInsn(Opcodes.GOTO, fieldBranchEntranceLabel[fieldOffset + 1]);
                            } else {
                                mv.visitJumpInsn(Opcodes.GOTO, fieldBranchExitLabel[fieldOffset]);
                            }

                        }

                    } else if (isDeepCopy && (!TypeTransformAssist.isBaseType(iterField.getType())) &&
                            (!TypeTransformAssist.isBaseType(resloveInfo.getSourceFieldType()))) {
                        /**
                         * 其他自定义引用类型字段，深拷贝模式下需要递归处理
                         * 基础类型字段，Object 对象不做处理
                         *
                         */

                        mv.visitLabel(fieldBranchEntranceLabel[fieldOffset]);
                        LocalVariableInfo tempSourceObjectVarInfo = newTempVar(sourceClassInternalName, resloveInfo, startVarOffSetRecursion++, tempSourceObjectVarNum, recursions);


                        Label dataFieldjumpIfNull = new Label();
                        boolean dateTypeFlag = false;
                        boolean isNormalType = ((filedGenericType instanceof Class) &&
                                (!((Class) filedGenericType).isArray())) &&
                                (!(Map.class.isAssignableFrom(((Class) filedGenericType)))) &&
                                (!(Collection.class.isAssignableFrom(((Class) filedGenericType)))) &&
                                ((filedGenericType) != Object.class);
                        if (isNormalType) {
                            // 自定义嵌套类字段调用生成的字段转换类,如果是Map 或者Collection 接口子类，比如fastjson,Object 类型等,不自动转换，用户自定义拓展类转换
                            Class fieldType = (Class) filedGenericType;

                            if ((fieldType == Date.class) && (resloveInfo.getSourceFieldType() == Date.class)) {
                                // Date 类型直接调用 getTime()方法转换，不解析Date 类型字段依次赋值
                                dateTypeFlag = true;
                                mv.visitVarInsn(Opcodes.ALOAD, tempSourceObjectVarInfo.getIndex());
                                mv.visitJumpInsn(Opcodes.IFNULL, dataFieldjumpIfNull);
                                mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
                                mv.visitTypeInsn(Opcodes.NEW, DATA_TYPE_INTERNAL_NAME);
                                mv.visitInsn(Opcodes.DUP);
                                mv.visitVarInsn(Opcodes.ALOAD, tempSourceObjectVarInfo.getIndex());
                                //Date  getTime() 方法调用
                                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DATA_TYPE_INTERNAL_NAME, "getTime", "()J", false);
                                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, DATA_TYPE_INTERNAL_NAME, INIT_METHOD_NAME, DATA_TYPE_INIT_METHOD_DESCRIPTOR, false);

                            } else if (resloveInfo.isAutoTransform() && UniversalClassTypeStrategy.hasBeanTransFormsHandler(targetClass.getName() + iterField.getName())) {

                                // 调用递归创建的转换类对象，这些对象在实例化过程中已经赋值给对应字段，这里直接通过字段getfield指令 获取，然后调用其转换方法
                                mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
                                mv.visitVarInsn(Opcodes.ALOAD, SELF_OBJECT_VAR_OFFSET);
                                mv.visitFieldInsn(Opcodes.GETFIELD, generateClassInternalName, iterField.getName() + EXTEND_IMPL_FIELD_NAME_SUFFIX, BEAN_TRANSFORM_DESC);
                                mv.visitVarInsn(Opcodes.ALOAD, SELF_OBJECT_VAR_OFFSET);
                                mv.visitFieldInsn(Opcodes.GETFIELD, generateClassInternalName, iterField.getName() + SOURCE_FIELD_CLASS_FIELD_SUFFIX, FIELD_TYPE_DESC);
                                mv.visitVarInsn(Opcodes.ALOAD, tempSourceObjectVarInfo.getIndex());
                                mv.visitVarInsn(Opcodes.ALOAD, SELF_OBJECT_VAR_OFFSET);
                                mv.visitFieldInsn(Opcodes.GETFIELD, generateClassInternalName, iterField.getName() + TARGET_FIELD_CLASS_FIELD_SUFFIX, FIELD_TYPE_DESC);
                                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, BEAN_TRANSFORM_NAME, BEAN_TRANSFORM_METHOD_NAME, BEAN_TRANSFORM_METHOD_DESC, false);
                                // mv.visitMethodInsn(Opcodes.INVOKESPECIAL,BEAN_TRANSFORM_NAME, PUBLIC_BEAN_TRANSFORM_METHOD_NAME, GENERIC_TRANSFORM_METHOD_DESC, false);
                            } else {
                                LOG.warn("target class {} field: {} {}，autoTransform={}, 不满足自动转换条件,不予转换，可设置autoTransform=true 或者配置自定义转换类", targetClass.getSimpleName(), iterField.getType().getSimpleName(), iterField.getName(), resloveInfo.isAutoTransform());
//                                mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
//                                mv.visitInsn(Opcodes.ACONST_NULL);
                                continue;

                            }

                        } else {
                            // 其他情况不予转换，null 常量入栈,比如Map 或者Collection 接口子类。不自动转换，用户可以自定义拓展类转换
                            LOG.warn("target class {} field: {} {}，userExtend={}, 不满足自动转换条件,不予转换，请实现拓展类转换", targetClass.getSimpleName(), iterField.getType().getSimpleName(), iterField.getName(), resloveInfo.isUserExtend());
//                            mv.visitInsn(Opcodes.ACONST_NULL);
                            continue;

                        }

                        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(iterField.getType()));
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, targetClassInternalName, resloveInfo.getTargetFieldSetFunctionName(), resloveInfo.getTargetFieldSetFunctionDescriptor(), false);//                   mv.visitJumpInsn(Opcodes.GOTO,fieldBranchExitLabel[fieldOffset]);

                        if (dateTypeFlag) {
                            mv.visitLabel(dataFieldjumpIfNull);
                            if (fieldOffset < fieldBranchs - 1) {
                                mv.visitJumpInsn(Opcodes.GOTO, fieldBranchEntranceLabel[fieldOffset + 1]);
                            } else {
                                mv.visitJumpInsn(Opcodes.GOTO, fieldBranchExitLabel[fieldOffset]);
                            }
                        }


                    } else {

                        LOG.warn("自动转换模式不兼容：(调用源类 {} 字段{} ,目标类 {} 字段 {} ),请自定义拓展类转换",
                                sourceBeanClass.getSimpleName(),
                                resloveInfo.getSourceFieldName(),
                                targetClass.getSimpleName(),
                                resloveInfo.getTargetFieldName());
                        continue;
                    }


                }

                mv.visitLabel(fieldBranchExitLabel[fieldOffset]);
            } else {

                LOG.warn("调用源类 {} 字段{} ，目标类 {} 字段 {}， 无法转换，忽略",
                        sourceBeanClass.getSimpleName(),
                        resloveInfo.getSourceFieldName(),
                        targetClass.getSimpleName(),
                        resloveInfo.getTargetFieldName());
                continue;
            }

            ++fieldOffset;

        }


        //targetFieldObject 变量入栈，作为上层targetObject set 方法的入参
        mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());

    }

    /**
     * 覆写MethodVisitor visitCode 方法，生成转换函数字节码
     * 转换函数：{@link BeanTransFormsHandler#beanTransforms(Class, Object, Class)}
     * <code>
     *     public abstract Object beanTransforms(Class sourceBeanClass,Object sourceBeanObject, Class targetClass) throws Exception
     * </code>
     */
    @Override
    public void visitCode() {

        mv.visitCode();

        // 方法起始位置打标签
        mv.visitLabel(startOfMethodBeanTransformsLable);

        /**
         *   以下四个是方法体参数变量
         *  public abstract Object beanTransforms(Class sourceBeanClass,Object sourceBeanObject, Class targetClass) throws Exception;
         */

        // 方法内局部变量定义位置。别名 命名规则 "localVar_"+recursions+"_"+varOffSet，内层嵌套的代码块需要访问上层已定义的变量
        // beanTransforms 四个参数变量信息加入 map 缓存，按照命名规则，初次进入方法recursions=0，startVarOffSetRecursion 从0-3 依次编号
        String beanTransFormsHandlerinternalName = generateClassname.replace('.', '/');
        LocalVariableInfo thisVariableInfo = new VariableDefine().index(0).name("this")
                .alias(LOCAL_VAR_PREFIX + 0 + "_" + 0).start(startOfMethodBeanTransformsLable).end(endOfMethodBeanTransformsLable)
                .signature(null).descriptor("L" + beanTransFormsHandlerinternalName + ";")
                .define();
        localVariableMap.put(thisVariableInfo.getAlias(), thisVariableInfo);


        LocalVariableInfo sourceBeanClassVariableInfo = new LocalVariableInfo();
        sourceBeanClassVariableInfo.setAlias(LOCAL_VAR_PREFIX + 0 + "_" + 1);
        sourceBeanClassVariableInfo.setName("sourceBeanClass");
        sourceBeanClassVariableInfo.setDescriptor("Ljava/lang/Class;");
        //签名保持和描述符一致
        sourceBeanClassVariableInfo.setSignature("Ljava/lang/Object;");
        sourceBeanClassVariableInfo.setStart(startOfMethodBeanTransformsLable);
        sourceBeanClassVariableInfo.setEnd(endOfMethodBeanTransformsLable);
        sourceBeanClassVariableInfo.setIndex(1);
        localVariableMap.put(sourceBeanClassVariableInfo.getAlias(), sourceBeanClassVariableInfo);


        LocalVariableInfo sourceBeanObjectVariableInfo = new LocalVariableInfo();
        sourceBeanObjectVariableInfo.setAlias(LOCAL_VAR_PREFIX + 0 + "_" + 2);
        sourceBeanObjectVariableInfo.setName("sourceBeanObject");
        sourceBeanObjectVariableInfo.setDescriptor("Ljava/lang/Object;");

        //签名保持和描述符一致
        sourceBeanObjectVariableInfo.setSignature(null);
        sourceBeanObjectVariableInfo.setStart(startOfMethodBeanTransformsLable);
        sourceBeanObjectVariableInfo.setEnd(endOfMethodBeanTransformsLable);
        sourceBeanObjectVariableInfo.setIndex(2);
        localVariableMap.put(sourceBeanObjectVariableInfo.getAlias(), sourceBeanObjectVariableInfo);

        LocalVariableInfo targetClassVariableInfo = new LocalVariableInfo();
        targetClassVariableInfo.setAlias(LOCAL_VAR_PREFIX + 0 + "_" + 3);
        targetClassVariableInfo.setName("targetClass");
        targetClassVariableInfo.setDescriptor("Ljava/lang/Class;");

        //签名保持和描述符一致
        targetClassVariableInfo.setSignature("Ljava/lang/Object;");
        targetClassVariableInfo.setStart(startOfMethodBeanTransformsLable);
        targetClassVariableInfo.setEnd(endOfMethodBeanTransformsLable);
        targetClassVariableInfo.setIndex(3);
        localVariableMap.put(targetClassVariableInfo.getAlias(), targetClassVariableInfo);

        int recur = recursionTimes++;

        try {
            TypeTransformAssist.checkClass(targetClass, sourceBeanClass);
        } catch (BeanTransformException e) {
            LOG.error(e.toString());
            return;
        }

        /**
         * 递归函数处理目标类字段转换，字段类型为自定义类或者有拓展接口实现转换的内部递归调用该函数
         * 后两个参数是和字段拓展类相关，首次递归不需要，可设置为null
         */

        mv.visitVarInsn(Opcodes.ALOAD, 2);

        mv.visitJumpInsn(Opcodes.IFNONNULL, transformStart);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitLabel(transformStart);

        visitCodeRecursion(sourceBeanClass, targetClass, recur, 2);
        // 所有字段处理结束位置打标签， return 之前
        mv.visitLabel(fieldProcessTerminate);

        mv.visitInsn(Opcodes.ARETURN);

        mv.visitLabel(endOfMethodBeanTransformsLable);

        // 定义局部变量表, visitLocalVarivale设置为静态公共方法
        visitLocalVarivale(localVariableMap, mv);
        // 方法末位位置打标签
        visitMaxs(1, localVariableMap.size());

        visitEnd();


    }

    /**
     * 写入变量
     *
     * @param localVariableMap
     * @param mv
     */
    public static void visitLocalVarivale(Map<String, LocalVariableInfo> localVariableMap, MethodVisitor mv) {

        // 通过字节码写入变量，（除了异常4个方法参数，代码提中缓存的变量也记录在map 中）
        Set<Map.Entry<String, LocalVariableInfo>> entries = localVariableMap.entrySet();
        for (Map.Entry<String, LocalVariableInfo> entry : entries) {
            LocalVariableInfo localVariableInfo = entry.getValue();
            mv.visitLocalVariable(localVariableInfo.getName(),
                    localVariableInfo.getDescriptor(),
                    localVariableInfo.getSignature(),
                    localVariableInfo.getStart(),
                    localVariableInfo.getEnd(),
                    localVariableInfo.getIndex());

        }


    }

    /**
     * 访问框架
     *
     * @param type     the type of this stack map frame
     * @param numLocal the number of local variables in the visited frame.
     * @param local    the local variable types in this frame
     * @param numStack the number of operand stack elements in the visited frame.
     * @param stack    the operand stack types in this frame. This array must not be modified. Its
     *                 content has the same format as the "local" array.
     */
    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    /**
     *
     * @param opcode
     */
    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }

    /**
     * @param opcode
     * @param operand
     */
    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
    }

    /**
     * @param opcode
     * @param var
     */
    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }

    /**
     * @param opcode
     * @param label
     */
    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
    }

    /**
     * @param label
     */
    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
    }

    /**
     * @param value
     */
    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(value);
    }

    /**
     * @param var
     * @param increment
     */
    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
    }

    /**
     * @param name       名字
     * @param descriptor 描述符
     * @param signature  签名
     * @param start      开始
     * @param end        结束
     * @param index      指数
     */
    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {


    }

    /**
     * 自动计算，传参大于1 即可
     * @param maxStack
     * @param maxLocals
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        mv.visitMaxs(maxStack, maxLocals);

    }

    /**
     * 方法终止
     */
    @Override
    public void visitEnd() {
        mv.visitEnd();
    }

    /**
     * 局部变量数赋值，同步方法
     * @param varOffset
     */
    public synchronized void setVarOffset(int varOffset) {
        this.varOffset = varOffset;
    }

    /**
     *
     * @return {@link MethodVisitor}
     */
    public MethodVisitor getMethodVisitor() {
        return mv;
    }

    /**
     *
     * @return {@link Class}
     */
    public Class<?> getSourceBeanClass() {
        return sourceBeanClass;
    }

    /**
     *
     * @return {@link Class}
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     *
     * @return boolean
     */
    public boolean isDeepCopy() {
        return isDeepCopy;
    }

    /**
     *
     * @return {@link String}
     */
    public String getGenerateClassname() {
        return generateClassname;
    }

    /**
     *
     * @return boolean
     */
    public boolean isPermitBaseTypeInterconvert() {
        return permitBaseTypeInterconvert;
    }

    /**
     *
     * @return int
     */
    public int getVarOffset() {
        return varOffset;
    }

    /**
     *
     * @return int
     */
    public int getRecursionTimes() {
        return recursionTimes;
    }

    /**
     *
     * @return {@link Map}
     */
    public Map<String, LocalVariableInfo> getLocalVariableMap() {
        return localVariableMap;
    }

    /**
     *
     * @return {@link Label}
     */
    public Label getStartOfMethodBeanTransformsLable() {
        return startOfMethodBeanTransformsLable;
    }

    /**
     *
     * @return {@link Label}
     */
    public Label getEndOfMethodBeanTransformsLable() {
        return endOfMethodBeanTransformsLable;
    }

    /**
     * @return {@link Label}
     */
    public Label getTransformStart() {
        return transformStart;
    }

    /**
     * @return {@link Label}
     */
    public Label getFieldProcessTerminate() {
        return fieldProcessTerminate;
    }

    /**
     * @return {@link Map}
     */
    public Map<Integer, Label> getRecurisionExitLabel() {
        return recurisionExitLabel;
    }
}
