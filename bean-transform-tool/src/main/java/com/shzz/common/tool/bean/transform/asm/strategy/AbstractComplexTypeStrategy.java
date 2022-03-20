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
 * 复杂类型处理策略的抽象类，实现了部分公共方法，定义了公共字段
 *
 * @author wen wang
 * @date 2021/12/25 23:22
 */
public abstract class AbstractComplexTypeStrategy implements ComplexTypeStrategy {

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger("AbstractComplexTypeStrategy");

    /**
     * 转换类字段名称后缀
     */
    public static final String ELEMENT_TRANSFORM_MEDIAN = "_Elememnt";
    /**
     * 目标变量名
     */
    public static final String TARGET_VARIABLE_NAME = "targetVar";
    /**
     * 临时元素变量名，临时变量存储 Map、Collection、Array 迭代的内部元素对象
     */
    public static final String TEMP_ELEMENT_VARIABLE_NAME = "tempElement";
    /**
     * 迭代器变量名
     */
    public static final String ITERATOR_VARIABLE_NAME = "iterator";
    /**
     * 数组长度变量名称
     */
    public static final String ARRAY_LENGTH_VARIABLE_NAME = "arrayLength";
    /**
     * 数组索引变量名
     */
    public static final String ARRAY_INDEX_VARIABLE_NAME = "index";
    /**
     * 变换后临时变量，比如源类Collection 最内层元素 转换为目标类最内层元素时先生成临时变量保存。
     */
    public static final String TRANSFORM_BASETYPE_VAR = "transformBaseTypeVar";

    /**
     * 上下文注册，使用ThreadLoacl 存储，线程隔离
     */
    protected ThreadLocal<AbstractContext> registerContext_local = new ThreadLocal<>();

    /**
     * 源类字段中，如果是Collection、Map、Array类型，解析字段的内部元素类型，使用ThreadLoacl 存储，线程隔离
     */
    protected ThreadLocal<Class> sourceElementType_local = new ThreadLocal<>();
    /**
     * 目标类字段中，如果是Collection、Map、Array类型，解析字段的内部元素类型，使用ThreadLoacl 存储，线程隔离
     */
    protected ThreadLocal<Class> targetElementType_local = new ThreadLocal<>();
    /**
     * 源生类型地方
     */
    protected ThreadLocal<Class> sourceRawType_local = new ThreadLocal<>();
    /**
     * 当地目标原始类型
     */
    protected ThreadLocal<Class> targetRawType_local = new ThreadLocal<>();

    /**
     * 字段预留拓展
     */
    protected ThreadLocal<List<Class>> reservedSource_local = new ThreadLocal<>();
    /**
     * 字段预留拓展
     */
    protected ThreadLocal<List<Class>> reservedTarget_local = new ThreadLocal<>();

    /**
     * 名称前缀
     */
    protected ThreadLocal<String> namePrefix_Local = new ThreadLocal<>();


    /**
     * 保留递归传递MethodVisitor
     */
    protected ThreadLocal<MethodVisitor> extensTransformMethodVisitor_Local = new ThreadLocal<>();


    /**
     *
     *
     * @param sequence_Local 递归产生的序列编号，作为类名的后缀
     * @param sourceType     源类型
     * @param targetType     目标类型
     * @throws Exception 异常
     */
    protected void setSequence(ThreadLocal<Integer> sequence_Local, Type sourceType, Type targetType) throws Exception {


        if (Objects.nonNull(sequence_Local.get())) {

            sequence_Local.set(sequence_Local.get() + 1);
        } else {
            sequence_Local.set(Integer.valueOf(1));
        }
    }


    /**
     * 定义局部变量
     *
     * @param startOfMethodBeanTransformsLable 方法体作用域开始标签
     * @param endOfMethodBeanTransformsLable   方法体作用域结束标签
     * @param rawType                          原始类型
     * @param elemType                         元素类型
     * @param pattern                          模式
     * @param ownerClassInternalName           所有者类名称，使用Internal name,即，包路径中"."替换为"/"
     * @return {@link Map}
     */
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

    /**
     * 解析数组组件类型
     *
     * @param arrayType 数组类型
     * @return {@link List}
     */
    public static List<Class> resolveArrayElementType(Class arrayType) {
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

    /**
     *
     * 嵌套Collection 和 多维数组转换匹配条件判断
     *  1 层数一致
     *  2 typeList 除了最内层外，其它层级 是参数化的Collection 类型，arrayTypeList除了最内层外，其它层级是数组类型
     *  3 最内层，typeList 是Class 类型，arrayTypeList 最内层不是数组类型
     *  满足以上条件则认为二者具备转换匹配条件
     * @param arrayTypeList 数组类型列表
     * @param typeList      类型列表
     * @return boolean
     * @throws Exception 异常
     */
    public static boolean collectionMatchArrayType(List<Class> arrayTypeList, List<Type> typeList) throws Exception {

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


    /**
     * 字段参数化类型的原始类型可能是接口或者抽象类， 通过new 指令创建会出错。
     * 需要找到对应接口或者抽象类的可创建型子类类型。如果本身就是可创建型类型，则直接返回该类型
     * Collection 接口下子接口有三大类
     * Set   List  Queue, 如果是这几种接口类型,子类实现类可能有多种形式。本工具统一以 HashSet ArrayList  ArrayDeque 来表示
     *
     * @param rawType 原始类型
     * @return {@link Class}
     */
    public static Class findCollectionImplementClass(Class rawType) {

        if ((!Modifier.isAbstract(rawType.getModifiers())) && (Modifier.isPublic(rawType.getModifiers()))) {
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

    /**
     * 解析集合类内部参数化泛型，多层集合循环解析
     *
     * @param parameterizedType 参数化类型
     * @return {@link List}
     * @throws BeanTransformException bean转换异常
     */
    public static List<Type> resolveCollectionElenentType(ParameterizedType parameterizedType) throws BeanTransformException {

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

    /**
     * 对象转换的字节码生成函数
     *
     * @param defineLocalVar  定义局部变量
     * @param layer           层
     * @param sourceElemType  源类字段是Collection、Map、Array,内层元素的类型
     * @param mv              mv
     * @param newMethodPrefix 新方法前缀，多层集合或者数组每层会单独产生转换方法
     * @throws Exception 异常
     */
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
                        typeLoadByteCode(sourceElementType_local.get(), mv, tempElement.getIndex());
                        // mv.visitVarInsn(Opcodes.ALOAD, tempElement.getIndex());
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


    /**
     * 所有者类的InternalName
     *
     * @return {@link String}
     */
    protected String getOwnerClassInternalName() {

        String generateClassname = this.registerContext_local.get().geneClassName();

        String generateClassInternalName = generateClassname.replace('.', '/');
        return generateClassInternalName;
    }

    /**
     * 定义方法参数变量
     *
     * @param startOfMethodBeanTransformsLable 方法体作用域的开始标签
     * @param endOfMethodBeanTransformsLable   方法体作用域的结束标签
     * @param classOwnerInternalName           类所有者InternalName
     * @return {@link Map}
     */
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

    /**
     * 方法名称
     *
     * @param methodPrefix 方法前缀
     * @param layer        层
     * @return {@link String}
     */
    protected String methodName(String methodPrefix, int layer) {

        return methodPrefix + "Layer" + layer;
    }

    /**
     * const 类型入栈指令
     *
     * @param elemType 元素类型
     * @param mv       MethodVisitor访问器，和调用者保持一致
     * @param varIndex 局部变量的索引编号
     * @return boolean
     */
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

    /**
     * 数组元素加载
     *
     * @param elemType 元素类型
     * @param mv       MethodVisitor访问器，和调用者保持一致
     * @return boolean
     */
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

    /**
     * 变量存储字节码生成函数
     *
     * @param elemType 元素类型
     * @param mv       MethodVisitor访问器，和调用者保持一致
     * @param varIndex 局部变量的索引编号
     * @return boolean
     */
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

    /**
     *
     *
     * @param elemType 元素类型
     * @param mv       MethodVisitor访问器，和调用者保持一致
     * @param varIndex 局部变量的索引编号
     * @return boolean
     */
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

    /**
     * 数组元素存储，不同类型指令不同
     *
     * @param elemType 元素类型
     * @param mv       MethodVisitor访问器，和调用者保持一致
     * @return boolean
     */
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

    /**
     * 生成转换函数的字节码指令
     *
     * @param extensTransformImplClassWriter ClassWriter，由调用者传入，保持一致，用于产生MethodVisitor
     * @param targetType                     目标类型
     * @param sourceBeanType                 源bean类型
     * @param newMethodPrefix                新方法前缀
     * @throws Exception 异常
     */
    public abstract void geneInstruction(ClassWriter extensTransformImplClassWriter, Type targetType, Type sourceBeanType, String newMethodPrefix) throws Exception;


    /**
     * 需要实现类覆写该方法
     * 生成转换类对象，封装与Map  中，主要针对Collection、Map、Array等复杂类型字段
     * key 值是该对象对应的上层owner类字段名称
     * 比如 复杂字段
     * <code>
     *    class MySourceClass{
     *     private List<List<ListElement>> nestList
     *     }
     *
     *     class MyTagretClass{
     *           private List<List<ListElement>> nestList
     *     }
     * </code>
     *  MySourceClass 与 MyTagretClass 转换会生成转换类 A，
     *  内部nestList 字段会单独生成一个转换类字节码文件B，
     *  这个转换类B被加载后反射生成对象C存储于A 对象的某个字段中，这个字段的名称和key保持一致。
     * value 转换类对象，先生成转换类字节码，加载转换类，然后反射生成转换类对象
     * @param sourceBeanType    源bean类型
     * @param targetType        目标类型
     * @param generateClassname 生成类名
     * @param fieldNamePrefix   字段名称前缀
     * @return {@link Map}
     * @throws Exception 异常
     */
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

    /**
     * 选择复杂类型处理模式，需要各个实现类覆写
     *
     * @param sourceBeanType 源bean类型
     * @param targetType     目标类型
     * @return {@link StrategyMode}
     * @throws Exception 异常
     */
    public StrategyMode chooseStrategyMode(Type sourceBeanType, Type targetType) throws Exception {
        return null;
    }

    /**
     * 每次构建完成转换类后清理TheahdLoacl 变量
     */
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

    /**
     * 策略匹配判断
     *
     * @param sourceBeanType 源bean类型
     * @param targetType     目标类型
     * @return boolean
     * @throws Exception 异常
     */
    @Override
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws Exception {
        boolean matchStrategy = false;

        if (Objects.nonNull(chooseStrategyMode(sourceBeanType, targetType))) {
            matchStrategy = true;
        }
        return matchStrategy;
    }

    /**
     * 根据用户环境获取类字节码主版本号,默认 52，即jdk 1.8
     *
     * @return int
     */
    public static int getClassVersion() {
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
