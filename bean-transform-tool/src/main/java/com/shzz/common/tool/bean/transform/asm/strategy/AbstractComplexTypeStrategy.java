package com.shzz.common.tool.bean.transform.asm.strategy;

import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.SystemProperties;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.*;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.bean.transform.asm.context.Context;
import com.shzz.common.tool.code.BeanTransformException;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.*;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.code.CommonCode;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;

import java.lang.reflect.Type;
import java.util.*;

import static com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter.*;
import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.*;
import static org.objectweb.asm.Opcodes.*;
import static com.shzz.common.tool.bean.transform.asm.strategy.StrategyMode.*;


/**
 * 抽象复杂类型策略
 *
 * @author wen wang
 * @date 2021/12/25 23:22
 */
public abstract class AbstractComplexTypeStrategy implements ComplexTypeStrategy {

    private static final Logger LOG = LoggerFactory.getLogger("AbstractComplexTypeStrategy");

    public static final String ELEMENT_TRANSFORM_MEDIAN = "_Elememnt";
    public static final String TARGET_VARIABLE_NAME = "targetVar";
    public static final String TEMP_ELEMENT_VARIABLE_NAME = "tempElement";
    public static final String ITERATOR_VARIABLE_NAME = "iterator";
    public static final String ARRAY_LENGTH_VARIABLE_NAME = "arrayLength";
    public static final String ARRAY_INDEX_VARIABLE_NAME = "index";
    public static final String TRANSFORM_BASETYPE_VAR = "transformBaseTypeVar";

    protected ThreadLocal<AbstractContext> registerContext_local = new ThreadLocal<>();

    protected ThreadLocal<Class> sourceElementType_local = new ThreadLocal<>();
    protected ThreadLocal<Class> targetElementType_local = new ThreadLocal<>();
    protected ThreadLocal<Class> sourceRawType_local = new ThreadLocal<>();
    protected ThreadLocal<Class> targetRawType_local = new ThreadLocal<>();

    // 以下两个字段预留拓展
    protected ThreadLocal<List<Class>> reservedSource_local = new ThreadLocal<>();
    protected ThreadLocal<List<Class>> reservedTarget_local = new ThreadLocal<>();

    protected ThreadLocal<String> namePrefix_Local = new ThreadLocal<>();


    protected ThreadLocal<MethodVisitor> extensTransformMethodVisitor_Local = new ThreadLocal<>();


    /**
     * @param sequence_Local 当地序列
     * @param sourceType     源类型
     * @param targetType     目标类型
     * @throws Exception 异常
     */
    protected void setSequence(ThreadLocal<Integer> sequence_Local, Type sourceType, Type targetType) throws Exception {
//        if(strategyMatch(sourceType, targetType)){
//
//
//        }

        if (Objects.nonNull(sequence_Local.get())) {

            sequence_Local.set(sequence_Local.get() + 1);
        } else {
            sequence_Local.set(Integer.valueOf(1));
        }
    }


    protected Map<String, LocalVariableInfo> defineLocalVar(Label startOfMethodBeanTransformsLable, Label endOfMethodBeanTransformsLable, Class rawType, Class elemType, StrategyMode pattern, String ownerClassInternalName) {


        // 定义所有局部变量
        Map<String, LocalVariableInfo> localVariableInfoMap = new HashMap<>();
        int varNum = 3; //

        // 方法参数
        localVariableInfoMap.putAll(defineMethodParameterVar(startOfMethodBeanTransformsLable, endOfMethodBeanTransformsLable,
                ownerClassInternalName));
        // 方法内部自定义变量
        LocalVariableInfo newVariableInfo = new VariableDefine().alias(TARGET_VARIABLE_NAME).name(TARGET_VARIABLE_NAME)
                .descriptor(org.objectweb.asm.Type.getDescriptor(rawType))
                .signature(org.objectweb.asm.Type.getDescriptor(rawType))
                .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                .define();
        localVariableInfoMap.put(TARGET_VARIABLE_NAME, newVariableInfo);

        LocalVariableInfo tempElementVariableInfo = new VariableDefine().alias(TEMP_ELEMENT_VARIABLE_NAME).name(TEMP_ELEMENT_VARIABLE_NAME)
                .descriptor(org.objectweb.asm.Type.getDescriptor(elemType))
                .signature(org.objectweb.asm.Type.getDescriptor(elemType))
                .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                .define();
        localVariableInfoMap.put(TEMP_ELEMENT_VARIABLE_NAME, tempElementVariableInfo);

        if ((pattern == StrategyMode.COLLECTION_TO_COLLECTION_PATTERN) || (pattern == StrategyMode.COLLECTION_TO_ARRAY_PATTERN)) {
            LocalVariableInfo iteratorVariableInfo = new VariableDefine().alias(ITERATOR_VARIABLE_NAME).name(ITERATOR_VARIABLE_NAME)
                    .descriptor(org.objectweb.asm.Type.getDescriptor(Iterator.class))
                    .signature(org.objectweb.asm.Type.getDescriptor(Iterator.class))
                    .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                    .define();
            localVariableInfoMap.put(ITERATOR_VARIABLE_NAME, iteratorVariableInfo);
        }
        if ((pattern == StrategyMode.ARRAY_TO_COLLECTION_PATTERN) || (pattern == StrategyMode.ARRAY_TO_ARRAY_PATTERN) || (pattern == StrategyMode.COLLECTION_TO_ARRAY_PATTERN)) {
            // 数组元素迭代，定义索引变量
            LocalVariableInfo arrayLengthVariableInfo = new VariableDefine().alias(ARRAY_LENGTH_VARIABLE_NAME).name(ARRAY_LENGTH_VARIABLE_NAME)
                    .descriptor(org.objectweb.asm.Type.getDescriptor(int.class))
                    .signature(org.objectweb.asm.Type.getDescriptor(int.class))
                    .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                    .define();
            localVariableInfoMap.put(ARRAY_LENGTH_VARIABLE_NAME, arrayLengthVariableInfo);

            // 数组元素迭代，定义索引变量
            LocalVariableInfo indexVariableInfo = new VariableDefine().alias(ARRAY_INDEX_VARIABLE_NAME).name(ARRAY_INDEX_VARIABLE_NAME)
                    .descriptor(org.objectweb.asm.Type.getDescriptor(int.class))
                    .signature(org.objectweb.asm.Type.getDescriptor(int.class))
                    .start(new Label()).end(endOfMethodBeanTransformsLable).index(++varNum)
                    .define();
            localVariableInfoMap.put(ARRAY_INDEX_VARIABLE_NAME, indexVariableInfo);


        }

        if (Objects.nonNull(targetElementType_local.get()) && Objects.nonNull(sourceElementType_local.get())) {
            LocalVariableInfo transformBaseTypeVar = new VariableDefine().alias(TRANSFORM_BASETYPE_VAR)
                    .name(TRANSFORM_BASETYPE_VAR)
                    .descriptor(org.objectweb.asm.Type.getDescriptor(targetElementType_local.get()))
                    .signature(org.objectweb.asm.Type.getDescriptor(targetElementType_local.get()))
                    .index(++varNum)
                    .start(new Label())
                    .end(endOfMethodBeanTransformsLable)
                    .define();
            localVariableInfoMap.put(TRANSFORM_BASETYPE_VAR, transformBaseTypeVar);
        }


        return localVariableInfoMap;

    }

    public static List<Class> resolveArrayElenentType(Class arrayType) {
        List<Class> classList = new ArrayList<>();
        if (arrayType.isArray()) {
            classList.add(arrayType);
            Class<?> componentType = arrayType;
            while (componentType.isArray()) {
                componentType = componentType.getComponentType();
                classList.add(componentType);
            }

        }

        return classList;


    }

    public static boolean collectionMatchArrayType(List<Class> arrayTypeList, List<Type> typeList) throws Exception {
        /**
         * @description: 嵌套Collection 和 多维数组转换匹配条件判断
         * 1 层数一致
         * 2 typeList 除了最内层外，其它层级 是参数化的Collection 类型，arrayTypeList除了最内层外，其它层级是数组类型
         * 3 最内层，typeList 是Class 类型，arrayTypeList 最内层不是数组类型
         * 满足以上条件则认为二者具备转换匹配条件
         * @param arrayTypeList
         * @param typeList
         * @return: boolean
         * @auther: wen wang
         * @date: 2021/12/10 16:29
         */
        boolean match = true;
        if (Objects.isNull(arrayTypeList) || Objects.isNull(typeList)) {
            match = false;
            return match;
        }
        if ((arrayTypeList.size() == typeList.size()) && (typeList.size() != 0)) {
            int layers = arrayTypeList.size();
            for (int layer = 0; layer < layers - 1; ++layer) {
                if (!arrayTypeList.get(layer).isArray()) {
                    match = false;
                } else if (!(typeList.get(layer) instanceof ParameterizedType)) {
                    match = false;
                } else if (!Collection.class.isAssignableFrom((Class) ((ParameterizedType) typeList.get(layer)).getRawType())) {
                    match = false;
                }

            }

            if (arrayTypeList.get(layers - 1).isArray()) {
                match = false;
            } else if (!(typeList.get(layers - 1) instanceof Class)) {
                match = false;
            }

            Class arrayEleClass = arrayTypeList.get(layers - 1);
            Class collectionEleClass = (Class) typeList.get(layers - 1);

            boolean matchConditional1 = (TypeTransformAssist.isBaseType(arrayEleClass) && (!TypeTransformAssist.isBaseType(collectionEleClass)));
            boolean matchConditional2 = (!TypeTransformAssist.isBaseType(arrayEleClass) && (TypeTransformAssist.isBaseType(collectionEleClass)));

            if (matchConditional1 || matchConditional2) {
                if (SystemProperties.getStrictModeFlag()) {
                    // 系统配置 strict.mode.flag 如果是严格模式，不转换，抛出异常
                    throw new BeanTransformException(CommonCode.TYPE_MISMATCH.getErrorCode(),
                            CommonCode.TYPE_MISMATCH.getErrorOutline(),
                            "数组元素类型:" + arrayEleClass.getSimpleName() + "， 集合元素类型："
                                    + collectionEleClass.getSimpleName() +
                                    " 无法转换, 如需默认转换，请在代码中设置系统变量strict.mode.flag=false ");

                }

            }


        }
        return match;


    }


    public static Class findCollectionImplementClass(Class rawType) {
        /**
         *  字段参数化类型的原始类型可能是接口或者抽象类， 通过new 指令创建会出错。
         *  需要找到对应接口或者抽象类的可创建型子类类型。如果本身就是可创建型类型，则直接返回该类型
         *  Collection 接口下子接口有三大类
         *  Set   List  Queue, 如果是这几种接口类型,子类实现类可能有多种形式。本工具统一以 HashSet ArrayList  ArrayDeque 来表示
         */
        if ((!Modifier.isAbstract(rawType.getModifiers())) && (!Modifier.isPublic(rawType.getModifiers()))) {
            // 接口或者抽象类修饰符为abstract, 非abstract修饰符 的collection 子类都可直接 new 指令生成新对象
            return rawType;
        } else if (List.class.isAssignableFrom(rawType)) {
            return ArrayList.class;
        } else if (Set.class.isAssignableFrom(rawType)) {
            return HashSet.class;
        } else if (Queue.class.isAssignableFrom(rawType)) {
            return ArrayDeque.class;
        }

        return null;
    }

    public static List<Type> resolveCollectionElenentType(ParameterizedType parameterizedType) throws BeanTransformException {

        // 处理集合类 参数化泛型
        List<Type> typeList = new ArrayList<>();
        if (Collection.class.isAssignableFrom((Class) parameterizedType.getRawType())) {
            Type type = parameterizedType;
            typeList.add(type);

            while ((type instanceof ParameterizedType) &&
                    (Collection.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()))) {
                // 只处理嵌套类是Collection  子类的泛型类型
                Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                type = types[0];
                typeList.add(type);

            }
            // 最内层元素是 泛型变量类型、泛型数组，通配泛型，自动化深拷贝模式暂不予处理，反回空栈，后续考虑拓展
            if (!(type instanceof Class)) {

                throw new BeanTransformException(CommonCode.GENERIC_TYPE_UNSUPPORT.getErrorCode(), CommonCode.GENERIC_TYPE_UNSUPPORT.getErrorOutline(), "集合类型最内层元素非Class 类型：" + type.getTypeName() + "， 可自定义实现转换类");
            } else if ((type == Object.class)) {
                throw new BeanTransformException(CommonCode.GENERIC_TYPE_UNSUPPORT.getErrorCode(), CommonCode.GENERIC_TYPE_UNSUPPORT.getErrorOutline(), "集合类型最内层元素Object类型" + "， 可自定义实现转换类");

            } else if (Map.class.isAssignableFrom(((Class) type))) {
                throw new BeanTransformException(CommonCode.GENERIC_TYPE_UNSUPPORT.getErrorCode(), CommonCode.GENERIC_TYPE_UNSUPPORT.getErrorOutline(), "集合类型最内层元素Object类型" + "， 可自定义实现转换类");

                // todo 其他类型后面完善
            } else if ((((Class) type).isArray())) {
                throw new BeanTransformException(CommonCode.GENERIC_TYPE_UNSUPPORT.getErrorCode(), CommonCode.GENERIC_TYPE_UNSUPPORT.getErrorOutline(), "集合类型最内层元素是数组类型类型:" + type.getTypeName() + "， 可自定义实现转换类");

            }


        }

        return typeList;

    }

    protected void transformByteCode(Map<String, LocalVariableInfo> defineLocalVar, int layer, Class sourceElemType, MethodVisitor mv, String newMethodPrefix) throws Exception {
        // 调用虚方法，转换集合迭代元素，this 参数入栈
        LocalVariableInfo thisVar = defineLocalVar.get("this");
        LocalVariableInfo tempElement = defineLocalVar.get(TEMP_ELEMENT_VARIABLE_NAME);

        LocalVariableInfo transformBaseTypeVar = defineLocalVar.get(TRANSFORM_BASETYPE_VAR);

        String ownerClassInternalName = getOwnerClassInternalName();

        if (sourceElemType != this.sourceElementType_local.get()) {

            mv.visitVarInsn(Opcodes.ALOAD, thisVar.getIndex());
            // 提取 迭代器 next()方法获取的元素对象
            mv.visitVarInsn(Opcodes.ALOAD, tempElement.getIndex());
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    ownerClassInternalName,
                    methodName(newMethodPrefix, (layer + 1)), BeanTransformsMethodAdapter.EXTENSION_TRANSFORM_METHOD_DESC, false);

        } else {

            if (Objects.nonNull(sourceElementType_local.get()) && Objects.nonNull(targetElementType_local.get())) {

                if (TypeTransformAssist.isBaseType(sourceElementType_local.get()) &&
                        TypeTransformAssist.isBaseType(targetElementType_local.get())) {


                    constTypeLoad(targetElementType_local.get(), mv, transformBaseTypeVar.getIndex());
                    typeStoreByteCode(targetElementType_local.get(), mv, transformBaseTypeVar.getIndex());

                    mv.visitLabel(transformBaseTypeVar.getStart());
                    // 基础类型直接转换，不需要调用转换类对象的方法实现转换
                    Label jumpIfNull = new Label();
                    if (TypeTransformAssist.isWrapsOrStringType(sourceElementType_local.get()) &&
                            (sourceElementType_local.get() != targetElementType_local.get())) {

                        mv.visitVarInsn(Opcodes.ALOAD, tempElement.getIndex());
                        mv.visitJumpInsn(Opcodes.IFNULL, jumpIfNull);

                    }

                    try {
                        mv.visitVarInsn(Opcodes.ALOAD, tempElement.getIndex());
                        if ((sourceElementType_local.get() != targetElementType_local.get())) {
                            TypeTransformAssist.baseTypeProcessByteCode(targetElementType_local.get(), sourceElementType_local.get(), mv, true);

                        }
                        typeStoreByteCode(targetElementType_local.get(), mv, transformBaseTypeVar.getIndex());

                    } catch (Exception e) {
                        LOG.error(e.toString());
                    }

                    if (TypeTransformAssist.isWrapsOrStringType(sourceElementType_local.get()) &&
                            (sourceElementType_local.get() != targetElementType_local.get())) {

                        mv.visitLabel(jumpIfNull);

                    }

                } else {
                    String sourceElementTypeFieldName = newMethodPrefix + ELEMENT_TRANSFORM_MEDIAN + TransformUtilGenerate.SOURCE_FIELD_CLASS_FIELD_SUFFIX;
                    String targetElementTypeFieldName = newMethodPrefix + ELEMENT_TRANSFORM_MEDIAN + TransformUtilGenerate.TARGET_FIELD_CLASS_FIELD_SUFFIX;
                    String elementTransformFieldName = newMethodPrefix + ELEMENT_TRANSFORM_MEDIAN + TransformUtilGenerate.EXTEND_IMPL_FIELD_NAME_SUFFIX;
                    //  com.shzz.common.tool.bean.transform.ExtensionObjectTransform 实现类，调用对应字段及方法实现转换
                    mv.visitVarInsn(Opcodes.ALOAD, BeanTransformsMethodAdapter.SELF_OBJECT_VAR_OFFSET);
                    mv.visitFieldInsn(Opcodes.GETFIELD, ownerClassInternalName, elementTransformFieldName, BeanTransformsMethodAdapter.BEAN_TRANSFORM_DESC);
                    mv.visitVarInsn(Opcodes.ALOAD, BeanTransformsMethodAdapter.SELF_OBJECT_VAR_OFFSET);
                    mv.visitFieldInsn(Opcodes.GETFIELD, ownerClassInternalName, sourceElementTypeFieldName, BeanTransformsMethodAdapter.FIELD_TYPE_DESC);
                    mv.visitVarInsn(Opcodes.ALOAD, tempElement.getIndex());
                    mv.visitVarInsn(Opcodes.ALOAD, BeanTransformsMethodAdapter.SELF_OBJECT_VAR_OFFSET);
                    mv.visitFieldInsn(Opcodes.GETFIELD, ownerClassInternalName, targetElementTypeFieldName, BeanTransformsMethodAdapter.FIELD_TYPE_DESC);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, BeanTransformsMethodAdapter.BEAN_TRANSFORM_NAME, BeanTransformsMethodAdapter.BEAN_TRANSFORM_METHOD_NAME, BeanTransformsMethodAdapter.BEAN_TRANSFORM_METHOD_DESC, false);
                    typeStoreByteCode(targetElementType_local.get(), mv, transformBaseTypeVar.getIndex());
                }

            } else {
                throw new BeanTransformException(CommonCode.ELEMENT_TYPE_NULL_EXCEPTION.getErrorCode(),
                        CommonCode.ELEMENT_TYPE_NULL_EXCEPTION.getErrorOutline(),
                        "无法解析 " + newMethodPrefix + " 最内层元素类型");
            }

        }

    }


    protected String getOwnerClassInternalName() {

        String generateClassname = this.registerContext_local.get().geneClassName();

        String generateClassInternalName = generateClassname.replace('.', '/');
        return generateClassInternalName;
    }

    protected Map<String, LocalVariableInfo> defineMethodParameterVar(Label startOfMethodBeanTransformsLable, Label endOfMethodBeanTransformsLable, String classOwnerInternalName) {
        // 定义三个方法参数部
        Map<String, LocalVariableInfo> parameterVariableInfoMap = new HashMap<>();
        String classOwner = "L" + classOwnerInternalName + ";";

        LocalVariableInfo thisVariableInfo = new VariableDefine().name("this").alias("this")
                .start(startOfMethodBeanTransformsLable)
                .end(endOfMethodBeanTransformsLable)
                .signature(null).descriptor(classOwner)
                .index(0)
                .define();
        parameterVariableInfoMap.put("this", thisVariableInfo);


        LocalVariableInfo sourceObjectVariableInfo = new VariableDefine().alias("sourceObject").name("sourceObject")
                .descriptor(org.objectweb.asm.Type.getDescriptor(Object.class))
                .signature(null).start(startOfMethodBeanTransformsLable).end(endOfMethodBeanTransformsLable).index(1)
                .define();
        parameterVariableInfoMap.put("sourceObject", sourceObjectVariableInfo);

        LocalVariableInfo deepCopyVariableInfo = new VariableDefine().alias("deepCopy").name("deepCopy")
                .descriptor(org.objectweb.asm.Type.getDescriptor(boolean.class))
                .signature(null).start(startOfMethodBeanTransformsLable).end(endOfMethodBeanTransformsLable).index(2)
                .define();
        parameterVariableInfoMap.put("deepCopy", deepCopyVariableInfo);
        return parameterVariableInfoMap;
    }

    protected String methodName(String methodPrefix, int layer) {

        return methodPrefix + "Layer" + layer;
    }

    protected boolean constTypeLoad(Class elemType, MethodVisitor mv, int varIndex) {
        if (Objects.isNull(elemType)) {
            return false;
        }

        if (TypeTransformAssist.istiny(elemType)) {

            if (short.class == elemType) {
                mv.visitIntInsn(SIPUSH, 0);

            } else if (byte.class == elemType) {
                mv.visitIntInsn(BIPUSH, 0);
            } else {
                mv.visitInsn(ICONST_0);
            }
//            mv.visitVarInsn(ISTORE,varIndex);

        } else if (long.class == elemType) {
            mv.visitInsn(LCONST_0);
//            mv.visitVarInsn(LSTORE,varIndex);
        } else if (float.class == elemType) {
            mv.visitInsn(FCONST_0);
//            mv.visitVarInsn(FSTORE,varIndex);
        } else if (double.class == elemType) {
            mv.visitInsn(DCONST_0);
//            mv.visitVarInsn(DSTORE,varIndex);
        } else if (TypeTransformAssist.referenceType(elemType)) {
            mv.visitInsn(ACONST_NULL);
//            mv.visitVarInsn(ASTORE,varIndex);
        }
        return true;
    }

    protected boolean arrayElementLoad(Class elemType, MethodVisitor mv) {
        if (Objects.isNull(elemType)) {
            return false;
        }

        if (byte.class == elemType) {
            mv.visitInsn(Opcodes.BALOAD);
        } else if (char.class == elemType) {
            mv.visitInsn(Opcodes.CALOAD);
        } else if (boolean.class == elemType) {
            mv.visitInsn(Opcodes.IALOAD);
        } else if (short.class == elemType) {
            mv.visitInsn(Opcodes.SALOAD);
        } else if (int.class == elemType) {
            mv.visitInsn(Opcodes.IALOAD);
        } else if (long.class == elemType) {
            mv.visitInsn(Opcodes.LALOAD);
        } else if (float.class == elemType) {
            mv.visitInsn(Opcodes.FALOAD);
        } else if (double.class == elemType) {
            mv.visitInsn(Opcodes.DALOAD);
        } else if (TypeTransformAssist.referenceType(elemType)) {
            mv.visitInsn(Opcodes.AALOAD);
        }

        return true;
    }

    protected boolean typeStoreByteCode(Class elemType, MethodVisitor mv, int varIndex) {
        if (Objects.isNull(elemType)) {
            return false;
        }

        if (TypeTransformAssist.istiny(elemType)) {
            mv.visitVarInsn(Opcodes.ISTORE, varIndex);
        } else if (long.class == elemType) {
            mv.visitVarInsn(Opcodes.LSTORE, varIndex);
        } else if (float.class == elemType) {
            mv.visitVarInsn(Opcodes.FSTORE, varIndex);
        } else if (double.class == elemType) {
            mv.visitVarInsn(Opcodes.DSTORE, varIndex);
        } else if (TypeTransformAssist.referenceType(elemType)) {
            mv.visitVarInsn(Opcodes.ASTORE, varIndex);
        }


        return true;

    }

    protected boolean typeLoadByteCode(Class elemType, MethodVisitor mv, int varIndex) {
        if (Objects.isNull(elemType)) {
            return false;
        }

        if (TypeTransformAssist.istiny(elemType)) {
            mv.visitVarInsn(Opcodes.ILOAD, varIndex);
        } else if (long.class == elemType) {
            mv.visitVarInsn(Opcodes.LLOAD, varIndex);
        } else if (float.class == elemType) {
            mv.visitVarInsn(Opcodes.FLOAD, varIndex);
        } else if (double.class == elemType) {
            mv.visitVarInsn(Opcodes.DLOAD, varIndex);
        } else if (TypeTransformAssist.referenceType(elemType)) {
            mv.visitVarInsn(Opcodes.ALOAD, varIndex);
        }


        return true;

    }

    protected boolean arrayElementStore(Class elemType, MethodVisitor mv) {


        if (Objects.isNull(elemType)) {
            return false;
        }
        if (byte.class == elemType) {
            mv.visitInsn(Opcodes.BASTORE);
        } else if (char.class == elemType) {
            mv.visitInsn(Opcodes.CASTORE);
        } else if (boolean.class == elemType) {
            mv.visitInsn(Opcodes.IASTORE);
        } else if (short.class == elemType) {
            mv.visitInsn(Opcodes.SASTORE);
        } else if (int.class == elemType) {
            mv.visitInsn(Opcodes.IASTORE);
        } else if (long.class == elemType) {
            mv.visitInsn(Opcodes.LASTORE);
        } else if (float.class == elemType) {
            mv.visitInsn(Opcodes.FASTORE);
        } else if (double.class == elemType) {
            mv.visitInsn(Opcodes.DASTORE);
        } else if (TypeTransformAssist.referenceType(elemType)) {
            mv.visitInsn(Opcodes.AASTORE);
        }
        return true;
    }

    public abstract void geneInstruction(ClassWriter extensTransformImplClassWriter, Type targetType, Type sourceBeanType, String newMethodPrefix) throws Exception;


    @Override
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception {
        ClassWriter extensTransformImplClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        TransformUtilGenerate.checkGenerateClassname(generateClassname);
        String internalName = generateClassname.replace('.', '/');
        // 创建类
        extensTransformImplClassWriter.visit(getClassVersion(),
                ACC_PUBLIC + ACC_FINAL,
                internalName,
                null,
                TransformUtilGenerate.OBJECT_CLASS_INTERNAL_NAME, new String[]{TransformUtilGenerate.EXTENSION_TRANSFORM_CLASS_INTERNAL_NAME});


        // 复杂类型内部元素类（比如参数化泛型实参、泛型数组实参、通配泛型实参 常规数组组件类）转换类，写入生成类字段,一共三个字段，其中两个字段记录的是集合元素转换类方法所需的类型信息，外部实例化后要对这些字段赋值
        String sourceElementTypeFieldName = fieldNamePrefix + ELEMENT_TRANSFORM_MEDIAN + TransformUtilGenerate.SOURCE_FIELD_CLASS_FIELD_SUFFIX;
        String targetElementTypeFieldName = fieldNamePrefix + ELEMENT_TRANSFORM_MEDIAN + TransformUtilGenerate.TARGET_FIELD_CLASS_FIELD_SUFFIX;
        String elementTransformFieldName = fieldNamePrefix + ELEMENT_TRANSFORM_MEDIAN + TransformUtilGenerate.EXTEND_IMPL_FIELD_NAME_SUFFIX;


        FieldVisitor sourceFieldVisitor = extensTransformImplClassWriter.visitField(ACC_PRIVATE,
                sourceElementTypeFieldName,
                BeanTransformsMethodAdapter.FIELD_TYPE_DESC,
                null,
                null);

        sourceFieldVisitor.visitEnd();

        FieldVisitor targetFieldVisitor = extensTransformImplClassWriter.visitField(ACC_PRIVATE,
                targetElementTypeFieldName,
                BeanTransformsMethodAdapter.FIELD_TYPE_DESC,
                null,
                null);

        targetFieldVisitor.visitEnd();
        FieldVisitor transformFieldVisitor = extensTransformImplClassWriter.visitField(ACC_PRIVATE,
                elementTransformFieldName,
                BeanTransformsMethodAdapter.BEAN_TRANSFORM_DESC,
                null,
                null);

        transformFieldVisitor.visitEnd();


        /**
         * 构造器方法
         *  Code:
         *       stack=1, locals=1, args_size=1
         *          0: aload_0
         *          1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         *          4: return
         */
        MethodVisitor extensTransformImplClassInit = extensTransformImplClassWriter.visitMethod(ACC_PUBLIC,
                BeanTransformsMethodAdapter.INIT_METHOD_NAME,
                BeanTransformsMethodAdapter.INIT_METHOD_DESCRIPTOR,
                null,
                null);

        extensTransformImplClassInit.visitVarInsn(Opcodes.ALOAD, 0);
        extensTransformImplClassInit.visitMethodInsn(Opcodes.INVOKESPECIAL, TransformUtilGenerate.OBJECT_CLASS_INTERNAL_NAME, BeanTransformsMethodAdapter.INIT_METHOD_NAME, BeanTransformsMethodAdapter.INIT_METHOD_DESCRIPTOR, false);
        extensTransformImplClassInit.visitInsn(Opcodes.RETURN);
        extensTransformImplClassInit.visitMaxs(1, 1);
        extensTransformImplClassInit.visitEnd();
        // 实现复杂类型转换方法，基于源类和目标类生成对应转换字节码
        geneInstruction(extensTransformImplClassWriter, targetType, sourceBeanType, fieldNamePrefix);
        // 注意
        extensTransformImplClassWriter.visitEnd();


        if (Objects.isNull(this.targetElementType_local.get()) || Objects.isNull(this.sourceElementType_local.get())) {
            // 执行过geneInstruction 方法后targetElementType  sourceElementType 写入ThreadLocal缓存中
            throw new BeanTransformException("0x00fa", "复杂类型对应的内部元素类型解析失败", "sourceBeanType: " + sourceBeanType.getTypeName() + "  " + sourceBeanType.getClass().getSimpleName() + "  targetType: " + targetType.getTypeName() + "  " + targetType.getClass().getSimpleName());
        }
        // 集合类内部元素转换对象
        BeanTransFormsHandler elementTransForms = null;
        if (!(TypeTransformAssist.isBaseType(sourceElementType_local.get()) &&
                TypeTransformAssist.isBaseType(targetElementType_local.get()))) {
            UniversalClassTypeStrategy universalClassTypeStrategy = new UniversalClassTypeStrategy();
            elementTransForms = (BeanTransFormsHandler) universalClassTypeStrategy.generate(this.sourceElementType_local.get(),
                    this.targetElementType_local.get(),
                    true,
                    true,
                    null, null);
        }

        byte[] classBytes = extensTransformImplClassWriter.toByteArray();
        Class extendClassImpl = TransformUtilGenerate.loadASMGenerateClass(classBytes, generateClassname);
        //默认构造方法；
        Constructor<?> constructor = extendClassImpl.getDeclaredConstructor();
        // 通过ClassWriter 生成的类已指定实现接口 com.shzz.common.tool.bean.transform.ExtensionObjectTransform，可强转
        ExtensionObjectTransform autoCreateExtensionObjectTransform = (ExtensionObjectTransform) constructor.newInstance();

        if (Objects.nonNull(elementTransForms)) {
            // 三个字段赋值
            Field extendClassField1 = extendClassImpl.getDeclaredField(sourceElementTypeFieldName);
            extendClassField1.setAccessible(true);
            extendClassField1.set(autoCreateExtensionObjectTransform, this.sourceElementType_local.get());
            Field extendClassField2 = extendClassImpl.getDeclaredField(targetElementTypeFieldName);
            extendClassField2.setAccessible(true);
            extendClassField2.set(autoCreateExtensionObjectTransform, this.targetElementType_local.get());
            Field extendClassField3 = extendClassImpl.getDeclaredField(elementTransformFieldName);
            extendClassField3.setAccessible(true);
            extendClassField3.set(autoCreateExtensionObjectTransform, elementTransForms);
        }

        Map<String, ExtensionObjectTransform> innerExtensionObjectTransformMap = new HashMap<>(4);

        String geneConvertField = fieldNamePrefix + TransformUtilGenerate.EXTEND_IMPL_FIELD_NAME_SUFFIX;
        innerExtensionObjectTransformMap.put(geneConvertField, autoCreateExtensionObjectTransform);
        clearThreadLocal();
        return innerExtensionObjectTransformMap;
    }

    public StrategyMode chooseStrategyMode(Type sourceBeanType, Type targetType) throws Exception {
        return null;
    }

    public void clearThreadLocal() {
        registerContext_local.remove();
        sourceElementType_local.remove();
        targetElementType_local.remove();
        sourceRawType_local.remove();
        targetRawType_local.remove();
        reservedSource_local.remove();
        reservedTarget_local.remove();
        namePrefix_Local.remove();
        extensTransformMethodVisitor_Local.remove();
    }

    @Override
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws Exception {
        boolean matchStrategy = false;

        if (Objects.nonNull(chooseStrategyMode(sourceBeanType, targetType))) {
            matchStrategy = true;
        }
        return matchStrategy;
    }

    public static int getClassVersion() {
        /**
         * @Description: 根据用户环境获取类字节码主版本号
         * @Author: wen wang
         * @Date: 2022/1/22 15:08
         * @return: int
         **/
        int classVersion = 52;
        int cafebabe = 0xCAFEBABE;
        DataInputStream dataInputStream = null;
        try {
            String className = AbstractComplexTypeStrategy.class.getName();
            dataInputStream = new DataInputStream(ClassLoader.getSystemResourceAsStream(className.replace('.', '/') + ".class"));

            if (dataInputStream.readInt() == cafebabe) {
                int minVersion = dataInputStream.readUnsignedShort();
                int majorVersion = dataInputStream.readUnsignedShort();

                classVersion = majorVersion;
            }
        } catch (Exception e) {
            LOG.error(e.toString());
        } finally {
            try {
                if (Objects.nonNull(dataInputStream)) {
                    dataInputStream.close();
                }
            } catch (IOException e) {
                LOG.error(e.toString());
            }
        }
        return classVersion;
    }


}
