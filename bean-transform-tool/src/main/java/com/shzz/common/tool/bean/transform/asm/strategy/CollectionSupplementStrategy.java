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
 * {@link CollectionTypeStrategy}?????????????????????N ??? Collection??????????????????Map  Collection ?????????????????????
 *
 * @author wen wang
 * @date 2022/2/15 21:29
 */
public class CollectionSupplementStrategy extends AbstractComplexTypeStrategy {
    /**
     * ??????
     */
    private static final Logger LOG = LoggerFactory.getLogger("SupplementStrategy");

    /**
     * ?????????
     */
    private AbstractContext context;


    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public static volatile ThreadLocal<Integer> sequence_Local = new ThreadLocal<>();


    /**
     * ????????????????????????
     */
    private ThreadLocal<Type> targetCollectionType_Local = new ThreadLocal<>();
    /**
     * ????????????????????????
     */
    private ThreadLocal<Type> targetActualType_Local = new ThreadLocal<>();

    /**
     * ????????????????????????
     */
    private ThreadLocal<Type> sourceCollectionType_Local = new ThreadLocal<>();
    /**
     * ????????????????????????
     */
    private ThreadLocal<Type> sourceActualType_Local = new ThreadLocal<>();
    /**
     * ????????????Collection??????????????????Collection ?????? ????????????????????????????????????internal ?????????????????????"." ????????? "/" ?????????
     */
    private String internalName;
    /**
     * ??????
     */
    private String generateClassname;

    /**
     * ????????????Collection??????????????????Collection  ?????? ?????????????????????????????????
     */
    private String elementTransformClassDescription;


    /**
     * @param context ?????????
     */
    public CollectionSupplementStrategy(AbstractContext context) {
        this.context = context;
        // ??????????????????????????????CollectionTypeSupplenemtStrategy ????????????

        try {
            setSequence(sequence_Local, context.getSourceField().getGenericType(), context.getTargetField().getGenericType());

        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }


    /**
     * ????????????????????????
     *
     * @return String
     */
    @Override
    protected String getOwnerClassInternalName() {
        String generateClassname = context.geneClassName() + sequence_Local.get().toString();
        String generateClassInternalName = generateClassname.replace('.', '/');
        return generateClassInternalName;
    }


    /**
     * ????????????????????????
     * ??????????????????
     * {@link AbstractComplexTypeStrategy#geneInstruction(ClassWriter, Type, Type, String)}
     *
     * @param extensTransformImplClassWriter
     * @param targetType
     * @param sourceBeanType
     * @param newMethodPrefix
     * @throws Exception ??????
     */
    @Override
    public void geneInstruction(ClassWriter extensTransformImplClassWriter, Type targetType, Type sourceBeanType, String newMethodPrefix) throws Exception {
        MethodVisitor mv = extensTransformImplClassWriter.visitMethod(Opcodes.ACC_PUBLIC + ACC_FINAL, EXTEND_TRANSFORM_METHOD_NAME, EXTEND_TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});
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
            LOG.warn("sourceBeanType???{}   {}???targetType???{}   {}????????????CollectionTypeSupplenemtStrategy ????????????????????????????????????ParameterizedType??????Class????????????????????????????????????com.shzz.common.tool.bean.transform.ExtensionObjectTransform ??????????????????", sourceBeanType.getTypeName(), sourceBeanType.getClass().getSimpleName(), targetType.getTypeName(), targetType.getClass().getSimpleName());
            throw new BeanTransformException(CommonCode.TYPE_UNSUPPORT.getErrorCode(), CommonCode.TYPE_UNSUPPORT.getErrorOutline(), "???????????????ParameterizedType??????Class");
        }

        if (targetActualType_Local.get() instanceof ParameterizedType) {
            targetActualElemClass = (Class) ((ParameterizedType) targetActualType_Local.get()).getRawType();
        } else if (targetActualType_Local.get() instanceof Class) {
            targetActualElemClass = (Class) targetActualType_Local.get();
        } else {
            LOG.warn("sourceBeanType???{}   {}???targetType???{}   {}????????????CollectionTypeSupplenemtStrategy ????????????????????????????????????ParameterizedType??????Class????????????????????????????????????com.shzz.common.tool.bean.transform.ExtensionObjectTransform ??????????????????", sourceBeanType.getTypeName(), sourceBeanType.getClass().getSimpleName(), targetType.getTypeName(), targetType.getClass().getSimpleName());
            throw new BeanTransformException(CommonCode.TYPE_UNSUPPORT.getErrorCode(), CommonCode.TYPE_UNSUPPORT.getErrorOutline(), "???????????????ParameterizedType??????Class");
        }

        // ????????????
        Map<String, LocalVariableInfo> localVar = new HashMap<>();
        localVar.putAll(defineLocalVar(methodStartLable, methodEndLable, targetRawClass, sourceActualElemClass, StrategyMode.COLLECTION_TO_COLLECTION_PATTERN, getOwnerClassInternalName()));

        // ???????????????????????????
        mv.visitLabel(methodStartLable);
        LocalVariableInfo sourceObjectVar = localVar.get("sourceObject");

        mv.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
        Label transformStart = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, transformStart);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);

        mv.visitLabel(transformStart);
        /**
         *   ?????????????????????
         */
        Class targetClasImpl = findCollectionImplementClass(targetRawClass);
        mv.visitTypeInsn(Opcodes.NEW, org.objectweb.asm.Type.getInternalName(targetClasImpl));
        mv.visitInsn(Opcodes.DUP);


        if (targetClasImpl == Stack.class) {

            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, org.objectweb.asm.Type.getInternalName(targetClasImpl), BeanTransformsMethodAdapter.INIT_METHOD_NAME, "()V", false);
        } else {

            mv.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            // ????????????size
            mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(Collection.class));
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "size", "()I", true);
            // ?????????????????????????????????????????????????????????(Collection ??????array) size?????????
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
        // hasNext ????????????
        mv.visitLabel(iteratorGotoLabel);
        mv.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "hasNext", "()Z", true);
        mv.visitJumpInsn(Opcodes.IFEQ, whileJump);
        mv.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
        // ??????Iterator next ????????????
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "next", "()Ljava/lang/Object;", true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceActualElemClass));
        // ??????????????????????????????
        mv.visitVarInsn(Opcodes.ASTORE, tempElement.getIndex());

        // ??????????????? ??????Collection add ????????????????????????????????????
        mv.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());
        // temp ????????????

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

            // boolean true ????????????
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, EXTENSION_TRANSFORM_INTERFACE_NAME, EXTENSION_TRANSFORM_METHOD_NAME, EXTENSION_TRANSFORM_METHOD_DESC, true);

        }

        mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(targetActualElemClass));

        // ??????add ??????,????????????????????????
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "add", "(Ljava/lang/Object;)Z", true);
        mv.visitInsn(Opcodes.POP);
        //?????? while ??????????????????
        mv.visitJumpInsn(Opcodes.GOTO, iteratorGotoLabel);
        mv.visitLabel(whileJump);
        // ???????????????????????????
        mv.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(methodEndLable);
        // ?????????????????????
        BeanTransformsMethodAdapter.visitLocalVarivale(localVar, mv);
        // ???????????????????????????
        mv.visitMaxs(1, 1);

        mv.visitEnd();

    }

    /**
     * ?????????????????????????????????Map  ??????????????????Collection???Map???Array?????????????????????
     * ??????{@link AbstractComplexTypeStrategy#geneTransform(Type, Type, String, String)}
     *
     * @param sourceBeanType    ???bean??????
     * @param targetType        ????????????
     * @param generateClassname ????????????
     * @param fieldNamePrefix   ??????????????????
     * @return {@link Map}
     * @throws Exception ??????
     */
    @Override
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception {

        if (!strategyMatch(sourceBeanType, targetType)) {
            LOG.warn("sourceBeanType???{}   {}???targetType???{}   {}????????????CollectionTypeSupplenemtStrategy ??????????????????????????????????????????????????????com.shzz.common.tool.bean.transform.ExtensionObjectTransform ??????????????????", sourceBeanType.getTypeName(), sourceBeanType.getClass().getSimpleName(), targetType.getTypeName(), targetType.getClass().getSimpleName());
            return new DefaultComplexTypeStrategy(context).geneTransform(sourceBeanType, targetType, generateClassname, fieldNamePrefix);
        }


        // ?????????????????????????????????
        generateClassname = generateClassname + "$" + sequence_Local.get().toString();

        this.generateClassname = generateClassname;

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        checkGenerateClassname(generateClassname);
        internalName = generateClassname.replace('.', '/');

        // ?????????
        classWriter.visit(getClassVersion(),
                ACC_PUBLIC + ACC_FINAL,
                internalName,
                null,
                OBJECT_CLASS_INTERNAL_NAME, new String[]{EXTENSION_TRANSFORM_CLASS_INTERNAL_NAME});

        TransformTypeContext elementTransformContext = new TransformTypeContext(((TransformTypeContext) context));
        Transform elementTransform = (Transform) elementTransformContext.geneTransform(sourceActualType_Local.get(), targetActualType_Local.get(), fieldNamePrefix).values().toArray()[0];

        // ?????????????????????????????????
        elementTransformClassDescription = writeField(classWriter, elementTransform, transformFieldName());

        // ?????????????????????
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
         * ????????????????????????{@link com.shzz.common.tool.bean.transform.ExtensionObjectTransform} ??????????????????????????????????????????????????????????????????
         * public Object extensionObjectTransform(Object sourceObject, boolean deepCopy) throws Exception;
         */
        geneInstruction(classWriter, targetType, sourceBeanType, fieldNamePrefix);

        classWriter.visitEnd();

        byte[] classBytes = classWriter.toByteArray();
        Class extendClassImpl = loadASMGenerateClass(classBytes, generateClassname);
        Constructor<?> constructor = extendClassImpl.getDeclaredConstructor();//?????????????????????
        // ??????ClassWriter ????????????????????????????????? com.shzz.common.tool.bean.transform.ExtensionObjectTransform????????????
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
     * ????????????????????????????????????
     *
     * @return {@link String}
     */
    private String transformFieldName() {
        return context.getIdentify() + EXTEND_IMPL_FIELD_NAME_SUFFIX + "_element";
    }

    /**
     * ?????????????????????
     *
     * @return {@link String}
     */
    private String collectionTransformFieldName() {
        return context.getIdentify() + EXTEND_IMPL_FIELD_NAME_SUFFIX;
    }


    /**
     * ??????threadlocal
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
     * ????????????
     * ??????{@link AbstractComplexTypeStrategy#strategyMatch(Type, Type)}
     *
     * @param sourceBeanType ???bean??????
     * @param targetType     ????????????
     * @return boolean
     * @throws Exception ??????
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

                // ????????????????????????????????? size==1
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
