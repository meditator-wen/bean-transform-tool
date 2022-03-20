package com.shzz.common.tool.bean.transform.asm.strategy;


import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.BeanTransFormsHandler;
import com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter;
import com.shzz.common.tool.bean.transform.asm.LocalVariableInfo;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.bean.transform.asm.context.TransformTypeContext;
import com.shzz.common.tool.code.BeanTransformException;
import com.shzz.common.tool.code.CommonCode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter.*;
import static com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter.EXTENSION_TRANSFORM_METHOD_DESC;
import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.*;
import static com.shzz.common.tool.bean.transform.asm.strategy.MapTypeStrategy.writeField;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACONST_NULL;


/**
 * {@link CollectionTypeStrategy}增补策略，处理N 层 Collection，内部可嵌套Map  Collection 数组等任意情况
 *
 * @author wen wang
 * @date 2022/2/15 21:29
 */
public class CollectionSupplementStrategy extends AbstractComplexTypeStrategy {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger("SupplementStrategy");

    /**
     * 上下文
     */
    private AbstractContext context;


    /**
     * 当地序列
     */
    public static volatile ThreadLocal<Integer> sequence_Local = new ThreadLocal<>();


    /**
     * 当地目标集合类型
     */
    private ThreadLocal<Type> targetCollectionType_Local = new ThreadLocal<>();
    /**
     * 目标实际类型地方
     */
    private ThreadLocal<Type> targetActualType_Local = new ThreadLocal<>();

    /**
     * 源集合类型地方
     */
    private ThreadLocal<Type> sourceCollectionType_Local = new ThreadLocal<>();
    /**
     * 源实际类型地方
     */
    private ThreadLocal<Type> sourceActualType_Local = new ThreadLocal<>();
    /**
     * 内部名称
     *///基于源类Collection字段和目标类Collection 字段 生成的转换的类原始类名和internal 类名（包路径中"." 替换成 "/" 转成）
    private String internalName;
    /**
     * 生成类名
     */
    private String generateClassname;

    /**
     * 元素变换类描述
     *///基于源类Collection字段和目标类Collection  字段 生成的转换的类描述信息
    private String elementTransformClassDescription;


    /**
     * 收集补充策略
     *
     * @param context 上下文
     */
    public CollectionSupplementStrategy(AbstractContext context) {
        this.context = context;
        // 如果同一线程再次创建CollectionTypeSupplenemtStrategy 序号累加

        try {
            setSequence(sequence_Local, context.getSourceField().getGenericType(), context.getTargetField().getGenericType());

        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }


    /**
     * 得到业主类内部名称
     *
     * @return {@link String}
     */
    @Override
    protected String getOwnerClassInternalName() {
        String generateClassname = context.geneClassName() + sequence_Local.get().toString();
        String generateClassInternalName = generateClassname.replace('.', '/');
        return generateClassInternalName;
    }


    /**
     * 基因指令
     *
     * @param extensTransformImplClassWriter extens变换impl类作家
     * @param targetType                     目标类型
     * @param sourceBeanType                 源bean类型
     * @param newMethodPrefix                新方法前缀
     * @throws Exception 异常
     */
    @Override
    public void geneInstruction(ClassWriter extensTransformImplClassWriter, Type targetType, Type sourceBeanType, String newMethodPrefix) throws Exception {
        MethodVisitor mv = extensTransformImplClassWriter.visitMethod(Opcodes.ACC_PUBLIC, EXTEND_TRANSFORM_METHOD_NAME, EXTEND_TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});
        mv.visitCode();
        final Label methodStartLable = new Label();
        final Label methodEndLable = new Label();

        Class sourceRawClass = (Class) ((ParameterizedType) sourceCollectionType_Local.get()).getRawType();
        Class targetRawClass = (Class) ((ParameterizedType) targetCollectionType_Local.get()).getRawType();
        Class sourceActualElemClass = null;
        Class targetActualElemClass = null;
        if (sourceActualType_Local.get() instanceof ParameterizedType) {
            sourceActualElemClass = (Class) ((ParameterizedType) sourceActualType_Local.get()).getRawType();
        } else if (sourceActualType_Local.get() instanceof Class) {
            sourceActualElemClass = (Class) sourceActualType_Local.get();
        } else {
            LOG.warn("sourceBeanType：{}   {}，targetType：{}   {}，不满足CollectionTypeSupplenemtStrategy 类型转换条件，内部元素非ParameterizedType或者Class，默认空值转换，请自定义com.shzz.common.tool.bean.transform.ExtensionObjectTransform 接口实现转换", sourceBeanType.getTypeName(), sourceBeanType.getClass().getSimpleName(), targetType.getTypeName(), targetType.getClass().getSimpleName());
            throw new BeanTransformException(CommonCode.TYPE_UNSUPPORT.getErrorCode(), CommonCode.TYPE_UNSUPPORT.getErrorOutline(), "内部元素非ParameterizedType或者Class");
        }

        if (targetActualType_Local.get() instanceof ParameterizedType) {
            targetActualElemClass = (Class) ((ParameterizedType) targetActualType_Local.get()).getRawType();
        } else if (targetActualType_Local.get() instanceof Class) {
            targetActualElemClass = (Class) targetActualType_Local.get();
        } else {
            LOG.warn("sourceBeanType：{}   {}，targetType：{}   {}，不满足CollectionTypeSupplenemtStrategy 类型转换条件，内部元素非ParameterizedType或者Class，默认空值转换，请自定义com.shzz.common.tool.bean.transform.ExtensionObjectTransform 接口实现转换", sourceBeanType.getTypeName(), sourceBeanType.getClass().getSimpleName(), targetType.getTypeName(), targetType.getClass().getSimpleName());
            throw new BeanTransformException(CommonCode.TYPE_UNSUPPORT.getErrorCode(), CommonCode.TYPE_UNSUPPORT.getErrorOutline(), "内部元素非ParameterizedType或者Class");
        }

        // 定义变量
        Map<String, LocalVariableInfo> localVar = new HashMap<>();
        localVar.putAll(defineLocalVar(methodStartLable, methodEndLable, targetRawClass, sourceActualElemClass, StrategyMode.COLLECTION_TO_COLLECTION_PATTERN, getOwnerClassInternalName()));

        // 方法起始位置打标签
        mv.visitLabel(methodStartLable);
        LocalVariableInfo sourceObjectVar = localVar.get("sourceObject");

        mv.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
        Label transformStart = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, transformStart);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitLabel(transformStart);
        /**
         *   非空分支字节码
         */
        Class targetClasImpl = findCollectionImplementClass(targetRawClass);
        mv.visitTypeInsn(Opcodes.NEW, org.objectweb.asm.Type.getInternalName(targetClasImpl));
        mv.visitInsn(Opcodes.DUP);


        if (targetClasImpl == Stack.class) {

            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, org.objectweb.asm.Type.getInternalName(targetClasImpl), BeanTransformsMethodAdapter.INIT_METHOD_NAME, "()V", false);
        } else {

            mv.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            // 源集合类size
            mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(Collection.class));
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "size", "()I", true);
            // 避免扩容影响效率，初始大小设置为源对象(Collection 或者array) size的两倍
            mv.visitLdcInsn(Integer.valueOf(0));
            mv.visitInsn(Opcodes.ISHL);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, org.objectweb.asm.Type.getInternalName(targetClasImpl), BeanTransformsMethodAdapter.INIT_METHOD_NAME, "(I)V", false);

        }

        LocalVariableInfo targetVar = localVar.get(TARGET_VARIABLE_NAME);

        mv.visitVarInsn(Opcodes.ASTORE, targetVar.getIndex());
        mv.visitLabel(targetVar.getStart());


        LocalVariableInfo tempElement = localVar.get(TEMP_ELEMENT_VARIABLE_NAME);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitVarInsn(Opcodes.ASTORE, tempElement.getIndex());
        mv.visitLabel(tempElement.getStart());
        LocalVariableInfo iteratorVar = localVar.get(ITERATOR_VARIABLE_NAME);
        Label whileJump = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawClass));
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "iterator", "()Ljava/util/Iterator;", true);
        mv.visitVarInsn(Opcodes.ASTORE, iteratorVar.getIndex());
        mv.visitLabel(iteratorVar.getStart());
        Label iteratorGotoLabel = new Label();
        // hasNext 回跳标签
        mv.visitLabel(iteratorGotoLabel);
        mv.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "hasNext", "()Z", true);
        mv.visitJumpInsn(Opcodes.IFEQ, whileJump);
        mv.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
        // 调用Iterator next 接口方法
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "next", "()Ljava/lang/Object;", true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceActualElemClass));
        // 迭代元素存入局部变量
        mv.visitVarInsn(Opcodes.ASTORE, tempElement.getIndex());

        // 目标类对象 调用Collection add 方法，目标对象变量入栈，
        mv.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());
        // temp 元素转换

        // transform value object
        mv.visitVarInsn(Opcodes.ALOAD, SELF_OBJECT_VAR_OFFSET);
        mv.visitFieldInsn(Opcodes.GETFIELD, internalName, transformFieldName(), elementTransformClassDescription);
        if (elementTransformClassDescription == BEAN_TRANSFORM_DESC) {
            /**
             * invoke {@link BeanTransFormsHandler}  beanTransforms method
             * public abstract Object beanTransforms(Class sourceBeanClass,Object sourceBeanObject, Class targetClass) throws Exception;
             */
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(Opcodes.ALOAD, tempElement.getIndex());
            mv.visitInsn(ACONST_NULL);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, BEAN_TRANSFORM_NAME, BEAN_TRANSFORM_METHOD_NAME, BEAN_TRANSFORM_METHOD_DESC, false);

        } else if (elementTransformClassDescription == EXTENSION_TRANSFORM_INTERFACE_DESC) {
            /**
             * invoke {@link ExtensionObjectTransform} extensionObjectTransform method
             *
             * public Object extensionObjectTransform(Object sourceObject, boolean deepCopy) throws Exception;
             */


            mv.visitVarInsn(Opcodes.ALOAD, tempElement.getIndex());

            // boolean true 常量入栈
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EXTENSION_TRANSFORM_INTERFACE_NAME, EXTENSION_TRANSFORM_METHOD_NAME, EXTENSION_TRANSFORM_METHOD_DESC, true);

        }

        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(targetActualElemClass));

        // 调用add 方法,相关参数已经入栈
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "add", "(Ljava/lang/Object;)Z", true);
        mv.visitInsn(Opcodes.POP);
        //回跳 while 循环判断标签
        mv.visitJumpInsn(Opcodes.GOTO, iteratorGotoLabel);
        mv.visitLabel(whileJump);
        // 退出循环，返回结果
        mv.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(methodEndLable);
        // 定义局部变量表
        BeanTransformsMethodAdapter.visitLocalVarivale(localVar, mv);
        // 方法末位位置打标签
        mv.visitMaxs(1, 1);

        mv.visitEnd();

    }

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
    @Override
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception {

        if (!strategyMatch(sourceBeanType, targetType)) {
            LOG.warn("sourceBeanType：{}   {}，targetType：{}   {}，不满足CollectionTypeSupplenemtStrategy 类型转换条件，默认空值转换，请自定义com.shzz.common.tool.bean.transform.ExtensionObjectTransform 接口实现转换", sourceBeanType.getTypeName(), sourceBeanType.getClass().getSimpleName(), targetType.getTypeName(), targetType.getClass().getSimpleName());
            return new DefaultComplexTypeStrategy(context).geneTransform(sourceBeanType, targetType, generateClassname, fieldNamePrefix);
        }


        // 传入类名基础上增加编号
        generateClassname = generateClassname + "$" + sequence_Local.get().toString();

        this.generateClassname = generateClassname;

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        checkGenerateClassname(generateClassname);
        internalName = generateClassname.replace('.', '/');

        // 创建类
        classWriter.visit(getClassVersion(),
                ACC_PUBLIC + ACC_FINAL,
                internalName,
                null,
                OBJECT_CLASS_INTERNAL_NAME, new String[]{EXTENSION_TRANSFORM_CLASS_INTERNAL_NAME});

        TransformTypeContext elementTransformContext = new TransformTypeContext(((TransformTypeContext) context));
        Transform elementTransform = (Transform) elementTransformContext.geneTransform(sourceActualType_Local.get(), targetActualType_Local.get(), fieldNamePrefix).values().toArray()[0];

        // 创建内层袁术转换类字段
        elementTransformClassDescription = writeField(classWriter, elementTransform, transformFieldName());

        // 转换类构造函数
        MethodVisitor initVisitor = classWriter.visitMethod(ACC_PUBLIC,
                INIT_METHOD_NAME,
                INIT_METHOD_DESCRIPTOR,
                null,
                null);

        initVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        initVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, OBJECT_CLASS_INTERNAL_NAME, INIT_METHOD_NAME, INIT_METHOD_DESCRIPTOR, false);
        initVisitor.visitInsn(Opcodes.RETURN);
        initVisitor.visitMaxs(1, 1);
        initVisitor.visitEnd();

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
        ExtensionObjectTransform extensionTransform = (ExtensionObjectTransform) constructor.newInstance();

        Field elementTransformField = extendClassImpl.getDeclaredField(transformFieldName());
        elementTransformField.setAccessible(true);
        elementTransformField.set(extensionTransform, elementTransform);

        Map<String, ExtensionObjectTransform> innerExtensionObjectTransformMap = new HashMap<>(4);

        innerExtensionObjectTransformMap.put(collectionTransformFieldName(), extensionTransform);
        clearThreadLocal();
        return innerExtensionObjectTransformMap;

    }

    /**
     * 将字段名
     *
     * @return {@link String}
     */
    private String transformFieldName() {
        return context.getIdentify() + EXTEND_IMPL_FIELD_NAME_SUFFIX + "_element";
    }

    /**
     * 集合变换字段名
     *
     * @return {@link String}
     */
    private String collectionTransformFieldName() {
        return context.getIdentify() + EXTEND_IMPL_FIELD_NAME_SUFFIX;
    }


    /**
     * 明确线程本地
     */
    @Override
    public void clearThreadLocal() {
        super.clearThreadLocal();
        targetCollectionType_Local.remove();
        targetActualType_Local.remove();
        sourceCollectionType_Local.remove();
        sourceActualType_Local.remove();
    }

    /**
     * 战略匹配
     *
     * @param sourceBeanType 源bean类型
     * @param targetType     目标类型
     * @return boolean
     * @throws Exception 异常
     */
    @Override
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws Exception {
        boolean match = false;

        if ((targetType instanceof ParameterizedType) && (sourceBeanType instanceof ParameterizedType)) {
            ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
            ParameterizedType sourceParameterizedType = (ParameterizedType) sourceBeanType;

            targetCollectionType_Local.set(targetParameterizedType);
            sourceCollectionType_Local.set(sourceParameterizedType);

            Class targetClass = (Class) targetParameterizedType.getRawType();
            Class sourceClass = (Class) sourceParameterizedType.getRawType();

            if (Collection.class.isAssignableFrom(targetClass)
                    && Collection.class.isAssignableFrom(sourceClass)) {

                // 集合类内部泛型实参数组 size==1
                Type targetActualType = targetParameterizedType.getActualTypeArguments()[0];
                Type sourceActualType = sourceParameterizedType.getActualTypeArguments()[0];
                if (Objects.nonNull(targetActualType) && Objects.nonNull(sourceActualType)) {
                    targetActualType_Local.set(targetActualType);
                    sourceActualType_Local.set(sourceActualType);
                    match = true;
                }

            }
        }

        if (!match) {

            if (Objects.nonNull(sequence_Local.get())) {

                sequence_Local.set(sequence_Local.get() - 1);
            }

        }

        return match;
    }


}
