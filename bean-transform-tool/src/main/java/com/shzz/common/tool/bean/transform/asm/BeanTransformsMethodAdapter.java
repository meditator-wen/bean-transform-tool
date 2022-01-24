package com.shzz.common.tool.bean.transform.asm;

/**
 * @Classname BeanTransformsMethodAdapterRefactor
 * @Description TODO
 * @Date 2021/12/6 10:04
 * @Created by wen wang
 */

import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.SystemProperties;
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

public class BeanTransformsMethodAdapter extends MethodVisitor {
    private static final Logger LOG = LoggerFactory.getLogger("BeanTransformsMethodAdapter");

    public static final String INIT_METHOD_NAME = "<init>";
    public static final String INIT_METHOD_DESCRIPTOR = "()V";
    public static final String LOCAL_VAR_PREFIX = "localVar_";

    public static final int SELF_OBJECT_VAR_OFFSET = 0;
    public static final String EXTENSION_TRANSFORM_INTERFACE_NAME = org.objectweb.asm.Type.getInternalName(ExtensionObjectTransform.class);
    public static final String EXTENSION_TRANSFORM_INTERFACE_DESC = org.objectweb.asm.Type.getDescriptor(ExtensionObjectTransform.class);
    public static final String BEAN_TRANSFORM_DESC = org.objectweb.asm.Type.getDescriptor(BeanTransFormsHandler.class);
    public static final String BEAN_TRANSFORM_NAME = org.objectweb.asm.Type.getInternalName(BeanTransFormsHandler.class);
    public static final String FIELD_TYPE_DESC = org.objectweb.asm.Type.getDescriptor(Class.class);
    public static final String DATA_TYPE_INTERNAL_NAME = org.objectweb.asm.Type.getInternalName(Date.class);
    public static final String DATA_TYPE_INIT_METHOD_DESCRIPTOR = "(J)V";
    public static final String EXTENSION_TRANSFORM_METHOD_NAME = "extensionObjectTransform";
    public static final String BEAN_TRANSFORM_METHOD_NAME = "beanTransforms";
    public static final String PUBLIC_BEAN_TRANSFORM_METHOD_NAME = "beanTransform";
    public static final String EXTENSION_TRANSFORM_METHOD_DESC = "(Ljava/lang/Object;Z)Ljava/lang/Object;";
    public static final String BEAN_TRANSFORM_METHOD_DESC = "(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;";
    public static final String GENERIC_TRANSFORM_METHOD_DESC = "<S:Ljava/lang/Object;T:Ljava/lang/Object;>(Ljava/lang/Class<TS;>;TS;Ljava/lang/Class<TT;>;)TS;";
    private final Class<?> sourceBeanClass;
    private final Class<?> targetClass;
    private final boolean isDeepCopy;
    private final String generateClassname;
    private final boolean permitBaseTypeInterconvert;
    /**
     * varOffset 变量编号，初始值为beanTransforms方法参数的最后一个变量(targetClass)编号之后，即4，beanTransforms方法内每次创建变量时该值累加，
     * public abstract Object beanTransforms(Class sourceBeanClass,Object sourceBeanObject, Class targetClass) throws Exception;
     */
    private volatile int varOffset = 4;

    private int recursionTimes = 1; // 递归调用 visitCodeRecursion

    // 录方法体中创建的变量列表信息，method  code 属性 中的LocalVariableTable 属性，具体请查看java虚拟机说明
    // visitCode 方法会遍历该map并写入以下属性
    private final Map<String, LocalVariableInfo> localVariableMap = new ConcurrentHashMap<>();

    private final Label startOfMethodBeanTransformsLable = new Label();
    private final Label endOfMethodBeanTransformsLable = new Label();
    Label transformStart = new Label();
    private final Label fieldProcessTerminate = new Label();

    private Map<Integer, Label> recurisionExitLabel = new ConcurrentHashMap<>(64);
    // 跳转的字段位置
    private Map<Integer, Label> nextFieldJumpLabel = new ConcurrentHashMap<>(64);

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

    private synchronized LocalVariableInfo newTempVar(String sourceClassInternalName,
                                                      ResloveInfo resloveInfo,
                                                      int startVarOffSetRecursion,
                                                      int preSourceObjectVarNum,
                                                      int recursions) {

        // 通过get 方法获取的变量，先存入局部变量表，供下轮递归使用（只针对fieldType是自定义类型的情况）
        // 先创建目标对象

        mv.visitVarInsn(Opcodes.ALOAD, findSourceObjectIndex(recursions, preSourceObjectVarNum));

        if (recursions <= 1) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceBeanClass)); //类型检查
        }

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sourceClassInternalName, resloveInfo.getSourceFieldGetFunctionName(), resloveInfo.getSourceFieldGetFunctionDescriptor(), false);

        int newTempSourceObjectVarNum = startVarOffSetRecursion;
        String tempSourceObjectVarAlias = LOCAL_VAR_PREFIX + recursions + "_" + (newTempSourceObjectVarNum); // 本次递归的第二个，编号为1

        LocalVariableInfo tempSourceObjectVarInfo = new VariableDefine()
                .alias(tempSourceObjectVarAlias)
                .name(tempSourceObjectVarAlias)
                .descriptor(org.objectweb.asm.Type.getDescriptor(resloveInfo.getSourceFieldType()))
                .signature(org.objectweb.asm.Type.getDescriptor(resloveInfo.getSourceFieldType()))
                .start(new Label())
                .end(endOfMethodBeanTransformsLable)
                .index(varOffset++)
                .define();
        localVariableMap.put(tempSourceObjectVarAlias, tempSourceObjectVarInfo);
        mv.visitVarInsn(Opcodes.ASTORE, tempSourceObjectVarInfo.getIndex()); // 下轮递归使用该变量值

        mv.visitLabel(tempSourceObjectVarInfo.getStart()); // 变量打标签
        return tempSourceObjectVarInfo;

    }



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
            mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceBeanClass)); //类型检查

            Class targetClassMap=null;
            if(TypeTransformAssist.isPrimitiveType(targetClass)){
                // 接口方法返回Object,如果是原始类型，无法直接ARETURN 指令返回，需要转成包装类。
                targetClassMap= TypeTransformAssist.typeMap(targetClass);

            }else{
                targetClassMap= targetClass;
            }
            try {
                TypeTransformAssist.baseTypeProcessByteCode(targetClassMap,sourceBeanClass, mv, isDeepCopy);
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

        Field[] targetBeanFields = targetClass.getDeclaredFields();
        if (Objects.isNull(targetBeanFields)) {
            mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
            return;
        }


        List<Field> effectiveFieldList = new ArrayList<>(); // 浅拷贝模式下统一字节码处理

        // 依次给目标对象字段赋值，涉及嵌套类的字段，递归调用，
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
            if (Objects.isNull(resloveInfo)) {
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

//            if ((!isDeepCopy) && TypeTransformAssist.isWrapsType(iterField.getType())
//                    && (!(iterFiledClassName.equals(sourceFiledClassName)))) {
//                LOG.warn("浅拷贝模式，Wraps类型。目标类字段{} {} 与源类对应字段{} {} 类型不一致，无法浅拷贝，忽略该字段," +
//                                "如有需要可重新设置 BeanTransFormsHandler generate方法 isDeepCopy 参数为true",
//                        iterFiledClassName,
//                        iterField.getName(),
//                        sourceFiledClassName,
//                        resloveInfo.getSourceFieldName());
//                continue;
//            }

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
            if ((!Objects.isNull(resloveInfo.getSourceFieldGetFunctionName()))
                    && (!Objects.isNull(resloveInfo.getSourceFieldGetFunctionDescriptor()))
                    && (resloveInfo.isSourceFieldGetFunctionNameAvailable())) {


                Type filedGenericType = iterField.getGenericType();

                String generateClassInternalName = generateClassname.replace('.', '/');
                boolean flag = (filedGenericType instanceof GenericArrayType) ||
                        (filedGenericType instanceof TypeVariable) ||
                        (filedGenericType instanceof WildcardType) ||
                        (filedGenericType instanceof ParameterizedType) ||
                        ((filedGenericType instanceof Class) && (((Class) filedGenericType).isArray()));


                if (resloveInfo.isUserExtend()||flag) {
                    mv.visitLabel(fieldBranchEntranceLabel[fieldOffset]);
                    LocalVariableInfo tempSourceObjectVarInfo = newTempVar(sourceClassInternalName, resloveInfo, startVarOffSetRecursion++, tempSourceObjectVarNum, recursions);
                    mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
                    // 使用自定义拓展类转换


                    mv.visitVarInsn(Opcodes.ALOAD, SELF_OBJECT_VAR_OFFSET);
                    // 从缓存字段中加载转换类对象。(需要在创建转换类时预先写入该字段值)
                    mv.visitFieldInsn(Opcodes.GETFIELD, generateClassInternalName, iterField.getName() + EXTEND_IMPL_FIELD_NAME_SUFFIX, EXTENSION_TRANSFORM_INTERFACE_DESC);
                    mv.visitVarInsn(Opcodes.ALOAD, tempSourceObjectVarInfo.getIndex()); // sourceObject 参数入栈
                    if (isDeepCopy) {
                        mv.visitInsn(Opcodes.ICONST_1); // boolean true 常量入栈
                    } else {
                        mv.visitInsn(Opcodes.ICONST_0);// boolean false 常量入栈
                    }
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EXTENSION_TRANSFORM_INTERFACE_NAME, EXTENSION_TRANSFORM_METHOD_NAME, EXTENSION_TRANSFORM_METHOD_DESC, true);

                    mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(iterField.getType()));
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, targetClassInternalName, resloveInfo.getTargetFieldSetFunctionName(), resloveInfo.getTargetFieldSetFunctionDescriptor(), false);//                   mv.visitJumpInsn(Opcodes.GOTO,fieldBranchExitLabel[fieldOffset]);


                } else {

                    if ((!isDeepCopy) || (TypeTransformAssist.isBaseType(iterField.getType()) &&
                            TypeTransformAssist.isBaseType(resloveInfo.getSourceFieldType()))) {

                        mv.visitLabel(fieldBranchEntranceLabel[fieldOffset]);
                        Label jumpIfNull = new Label();
                        if (TypeTransformAssist.isWrapsOrStringType(resloveInfo.getSourceFieldType()) &&
                                (SystemProperties.getWrapsTypeDeepyCopyFlag()||(iterField.getType() != resloveInfo.getSourceFieldType()))) {
                            // (iterField.getType() != resloveInfo.getSourceFieldType())
                            mv.visitVarInsn(Opcodes.ALOAD, findSourceObjectIndex(recursions, tempSourceObjectVarNum));
                            if (recursions <= 1) {
                                mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceBeanClass)); //类型检查
                            }
                            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sourceClassInternalName, resloveInfo.getSourceFieldGetFunctionName(), resloveInfo.getSourceFieldGetFunctionDescriptor(), false);
                            mv.visitJumpInsn(Opcodes.IFNULL, jumpIfNull);
                        }

                        //变量targetObjectVar 入栈，调用set方法，基础类型的源类字段
                        mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
                        mv.visitVarInsn(Opcodes.ALOAD, findSourceObjectIndex(recursions, tempSourceObjectVarNum));

                        if (recursions <= 1) {
                            mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceBeanClass)); //类型检查
                        }

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
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, targetClassInternalName, resloveInfo.getTargetFieldSetFunctionName(), resloveInfo.getTargetFieldSetFunctionDescriptor(), false);//                   mv.visitJumpInsn(Opcodes.GOTO,fieldBranchExitLabel[fieldOffset]);

                        if (TypeTransformAssist.isWrapsOrStringType(resloveInfo.getSourceFieldType()) &&
                                (SystemProperties.getWrapsTypeDeepyCopyFlag()||(iterField.getType() != resloveInfo.getSourceFieldType()))) {
                            //&&(iterField.getType() != resloveInfo.getSourceFieldType())
                            //
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
                         * 其他自定义引用类型字段、数组类对象，参数化泛型、类型变量、泛型数组、通配泛型等，深拷贝模式下需要递归处理
                         * 基础类型字段，Object 对象不做处理
                         *  数组类对象，参数化泛型、类型变量、泛型数组、通配泛型等 请继承{@link ExtensionObjectTransform } 自行实现，具体操作请参见 beanTransforms 方法说明
                         *
                         */


                        mv.visitLabel(fieldBranchEntranceLabel[fieldOffset]);
                        LocalVariableInfo tempSourceObjectVarInfo = newTempVar(sourceClassInternalName, resloveInfo, startVarOffSetRecursion++, tempSourceObjectVarNum, recursions);


                        Label dataFieldjumpIfNull = new Label();
                        boolean dateTypeFlag = false;
                        if (((filedGenericType instanceof Class) &&
                                (!((Class) filedGenericType).isArray())) &&
                                (!(Map.class.isAssignableFrom(((Class) filedGenericType)))) &&
                                (!(Collection.class.isAssignableFrom(((Class) filedGenericType)))) &&
                                ((filedGenericType) != Object.class)) {
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

                            } else if (resloveInfo.isAutoTransform()) {
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
                                mv.visitVarInsn(Opcodes.ALOAD, localVariableMap.get(recursionFunctionVarNameAtStart).getIndex());
                                mv.visitInsn(Opcodes.ACONST_NULL);

                            }

                        } else {
                            // 其他情况不予转换，null 常量入栈,比如Map 或者Collection 接口子类。不自动转换，用户可以自定义拓展类转换
                            LOG.warn("target class {} field: {} {}，userExtend={}, 不满足自动转换条件,不予转换，请实现拓展类转换", targetClass.getSimpleName(), iterField.getType().getSimpleName(), iterField.getName(), resloveInfo.isUserExtend());
                            mv.visitInsn(Opcodes.ACONST_NULL);

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
                        // 其他情况不予转换，null 常量入栈
                        // 其他情况不予转换，null 常量入栈,比如Map 或者Collection 接口子类。不自动转换，用户可以自定义拓展类转换
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
                // 调用方法信息不足，无法插入的get 方法字节码
                LOG.warn("调用源类 {} 字段{} get方法信息不满足要求，无法插入的get 方法字节码 给目标类 {} 字段 {} 赋值",
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

    @Override
    public void visitCode() {
        /**
         * 创建方法字节码，复写抽象BeanTransFormsHandler方法
         *    public abstract Object beanTransforms(Class sourceBeanClass,Object sourceBeanObject, Class targetClass) throws Exception
         *
         */
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
        LocalVariableInfo thisVariableInfo = new VariableDefine()
                .index(0)
                .name("this")
                .alias(LOCAL_VAR_PREFIX + 0 + "_" + 0)
                .start(startOfMethodBeanTransformsLable)
                .end(endOfMethodBeanTransformsLable)
                .signature(null)
                .descriptor("L" + beanTransFormsHandlerinternalName + ";")
                .define();
        localVariableMap.put(thisVariableInfo.getAlias(), thisVariableInfo);


        LocalVariableInfo sourceBeanClassVariableInfo = new LocalVariableInfo();
        sourceBeanClassVariableInfo.setAlias(LOCAL_VAR_PREFIX + 0 + "_" + 1);
        sourceBeanClassVariableInfo.setName("sourceBeanClass");
        sourceBeanClassVariableInfo.setDescriptor("Ljava/lang/Class;");
        sourceBeanClassVariableInfo.setSignature("Ljava/lang/Object;"); //签名保持和描述符一致
        sourceBeanClassVariableInfo.setStart(startOfMethodBeanTransformsLable);
        sourceBeanClassVariableInfo.setEnd(endOfMethodBeanTransformsLable);
        sourceBeanClassVariableInfo.setIndex(1);
        localVariableMap.put(sourceBeanClassVariableInfo.getAlias(), sourceBeanClassVariableInfo);


        LocalVariableInfo sourceBeanObjectVariableInfo = new LocalVariableInfo();
        sourceBeanObjectVariableInfo.setAlias(LOCAL_VAR_PREFIX + 0 + "_" + 2);
        sourceBeanObjectVariableInfo.setName("sourceBeanObject");
        sourceBeanObjectVariableInfo.setDescriptor("Ljava/lang/Object;");
        sourceBeanObjectVariableInfo.setSignature(null); //签名保持和描述符一致
        sourceBeanObjectVariableInfo.setStart(startOfMethodBeanTransformsLable);
        sourceBeanObjectVariableInfo.setEnd(endOfMethodBeanTransformsLable);
        sourceBeanObjectVariableInfo.setIndex(2);
        localVariableMap.put(sourceBeanObjectVariableInfo.getAlias(), sourceBeanObjectVariableInfo);

        LocalVariableInfo targetClassVariableInfo = new LocalVariableInfo();
        targetClassVariableInfo.setAlias(LOCAL_VAR_PREFIX + 0 + "_" + 3);
        targetClassVariableInfo.setName("targetClass");
        targetClassVariableInfo.setDescriptor("Ljava/lang/Class;");
        targetClassVariableInfo.setSignature("Ljava/lang/Object;"); //签名保持和描述符一致
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

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {


    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        mv.visitMaxs(maxStack, maxLocals);

    }

    @Override
    public void visitEnd() {
        mv.visitEnd();
    }

    public synchronized void setVarOffset(int varOffset) {
        this.varOffset = varOffset;
    }

    public MethodVisitor getMethodVisitor() {
        return mv;
    }

    public Class<?> getSourceBeanClass() {
        return sourceBeanClass;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public boolean isDeepCopy() {
        return isDeepCopy;
    }

    public String getGenerateClassname() {
        return generateClassname;
    }

    public boolean isPermitBaseTypeInterconvert() {
        return permitBaseTypeInterconvert;
    }

    public int getVarOffset() {
        return varOffset;
    }

    public int getRecursionTimes() {
        return recursionTimes;
    }

    public Map<String, LocalVariableInfo> getLocalVariableMap() {
        return localVariableMap;
    }

    public Label getStartOfMethodBeanTransformsLable() {
        return startOfMethodBeanTransformsLable;
    }

    public Label getEndOfMethodBeanTransformsLable() {
        return endOfMethodBeanTransformsLable;
    }

    public Label getTransformStart() {
        return transformStart;
    }

    public Label getFieldProcessTerminate() {
        return fieldProcessTerminate;
    }

    public Map<Integer, Label> getRecurisionExitLabel() {
        return recurisionExitLabel;
    }
}
