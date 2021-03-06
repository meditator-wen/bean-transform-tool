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
import com.shzz.common.tool.bean.transform.SystemProperties;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.*;

import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.bean.transform.asm.context.Context;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.BeanTransFormsHandler;
import com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter;
import com.shzz.common.tool.bean.transform.asm.LocalVariableInfo;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.code.BeanTransformException;
import com.shzz.common.tool.code.CommonCode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter.*;
import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.*;
import static com.shzz.common.tool.bean.transform.asm.strategy.StrategyMode.*;


/**
 * ??????????????????
 *
 * @author wen wang
 * @date 2021/12/8 15:47
 */
public class CollectionTypeStrategy extends AbstractComplexTypeStrategy {

    /**
     * ??????
     */
    private static final Logger LOG = LoggerFactory.getLogger("ParameterizedTypeStrategy");

    /**
     * ??????????????????Collection?????????????????????Collection????????????????????????????????????sourceTypeList_Local ?????????
     */
    ThreadLocal<List<Type>> sourceTypeList_Local=new ThreadLocal<>();
    /**
     * ?????????????????????Collection?????????????????????Collection????????????????????????????????????targetTypeList_Local ?????????
     */
    ThreadLocal<List<Type>> targetTypeList_Local=new ThreadLocal<>();
    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????sourceClassList_Local ?????????
     */
    ThreadLocal<List<Class>> sourceClassList_Local=new ThreadLocal<>();


    /**
     *
     *
     * @param context ?????????
     */
    public CollectionTypeStrategy(AbstractContext context) {
        this.registerContext_local.set(context);
    }


    /**
     * ????????????
     */
    public static final String SUPER_CLASS_NAME = org.objectweb.asm.Type.getInternalName(BeanTransFormsHandler.class);

    /**
     * ??????Collection ??? ??????Collection ????????????????????????
     * 1  ????????????
     * 2  ????????????????????????????????? ???????????????Collection ??????
     * 3  ???????????????Class ??????
     *
     * @param typeListSource ???????????????
     * @param typeListTarget ??????????????????
     * @return boolean
     * @throws Exception ??????
     */
    public static boolean collectionMatchCollection(List<Type> typeListSource, List<Type> typeListTarget) throws Exception {
        boolean match = true;
        if (Objects.isNull(typeListSource) || Objects.isNull(typeListTarget)) {
            match = false;
            return match;
        }
        if ((typeListSource.size() == typeListTarget.size()) && (typeListSource.size() >= 2)) {
            int layers = typeListSource.size();
            for (int layer = 0; layer < layers - 1; ++layer) {
                if (!(typeListSource.get(layer) instanceof ParameterizedType)) {
                    match = false;
                } else if (!Collection.class.isAssignableFrom((Class) ((ParameterizedType) typeListSource.get(layer)).getRawType())) {
                    match = false;
                } else if (!(typeListTarget.get(layer) instanceof ParameterizedType)) {
                    match = false;
                } else if (!Collection.class.isAssignableFrom((Class) ((ParameterizedType) typeListTarget.get(layer)).getRawType())) {
                    match = false;
                }

            }
            if (!(typeListTarget.get(layers - 1) instanceof Class)) {
                match = false;
            } else if (!(typeListSource.get(layers - 1) instanceof Class)) {
                match = false;
            }


            Class targetCollectionEleClass = (Class) typeListTarget.get(layers - 1);
            Class sourceCollectionClass = (Class) typeListSource.get(layers - 1);

            boolean matchConditional1 = (TypeTransformAssist.isBaseType(targetCollectionEleClass) && (!TypeTransformAssist.isBaseType(sourceCollectionClass)));
            boolean matchConditional2 = (!TypeTransformAssist.isBaseType(targetCollectionEleClass) && (TypeTransformAssist.isBaseType(sourceCollectionClass)));

            if (matchConditional1 || matchConditional2) {
                if (SystemProperties.getStrictModeFlag()) {
                    // ???????????? strict.mode.flag ????????????????????????????????????????????????
                    throw new BeanTransformException(CommonCode.TYPE_MISMATCH.getErrorCode(),
                            CommonCode.TYPE_MISMATCH.getErrorOutline(),
                            "?????????????????????:" + sourceCollectionClass.getSimpleName()
                                    + "??? ???????????????????????????" + targetCollectionEleClass.getSimpleName()
                                    + " , ????????????, ??????????????????????????????????????????????????????strict.mode.flag=false");

                }

            }
        }
        return match;

    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param extensTransformMethodVisitor ??????????????????????????????????????????
     * @param sourceRawType                ???????????????
     * @param targetRawType                ??????????????????
     * @param sourceElemType               ??????elem????????????
     * @param newMethodPrefix              ???????????????
     * @param layer                        ???????????????????????????????????????
     * @param pattern                      ????????????
     * @return boolean
     * @throws Exception ??????
     */
    private boolean visitCollectionTransformCode(MethodVisitor extensTransformMethodVisitor, Class sourceRawType, Class targetRawType, Class sourceElemType, String newMethodPrefix, int layer, StrategyMode pattern) throws Exception {
        if (!((pattern == StrategyMode.COLLECTION_TO_COLLECTION_PATTERN) || (pattern == StrategyMode.ARRAY_TO_COLLECTION_PATTERN))) {
            return false;
        }

        Class targetClasImpl = findCollectionImplementClass(targetRawType);
        extensTransformMethodVisitor.visitCode();
        final Label startOfMethodBeanTransformsLable = new Label();

        final Label endOfMethodBeanTransformsLable = new Label();
        // ????????????
        // Map<String, LocalVariableInfo> defineLocalVar = defineLocalVar(startOfMethodBeanTransformsLable, endOfMethodBeanTransformsLable, targetRawType, sourceElemType,pattern,getOwnerClassInternalName(this.namePrefix.get(),this.collectionOrArraySourceRawType.get(),this.collectionTargetRawType.get()));
        Map<String, LocalVariableInfo> defineLocalVar = defineLocalVar(startOfMethodBeanTransformsLable, endOfMethodBeanTransformsLable, targetRawType, sourceElemType, pattern, getOwnerClassInternalName());

        // ???????????????????????????
        extensTransformMethodVisitor.visitLabel(startOfMethodBeanTransformsLable);
        LocalVariableInfo targetVar = defineLocalVar.get(TARGET_VARIABLE_NAME);
        LocalVariableInfo sourceObjectVar = defineLocalVar.get("sourceObject");
        extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
        Label transformStart = new Label();
        extensTransformMethodVisitor.visitJumpInsn(Opcodes.IFNONNULL, transformStart);
        extensTransformMethodVisitor.visitInsn(Opcodes.ACONST_NULL);
        extensTransformMethodVisitor.visitInsn(Opcodes.ARETURN);
        extensTransformMethodVisitor.visitLabel(transformStart);
        /**
         *  ?????????????????????
         */

        extensTransformMethodVisitor.visitTypeInsn(Opcodes.NEW, org.objectweb.asm.Type.getInternalName(targetClasImpl));
        extensTransformMethodVisitor.visitInsn(Opcodes.DUP);

        if (targetClasImpl == Stack.class) {
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, org.objectweb.asm.Type.getInternalName(targetClasImpl), BeanTransformsMethodAdapter.INIT_METHOD_NAME, "()V", false);
        } else {

            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            if (pattern == StrategyMode.COLLECTION_TO_COLLECTION_PATTERN) {
                // ????????????size
                extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(Collection.class));
                extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "size", "()I", true);
            } else if (pattern == StrategyMode.ARRAY_TO_COLLECTION_PATTERN) {
                // ????????????length
                extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
                extensTransformMethodVisitor.visitInsn(Opcodes.ARRAYLENGTH);
                // ????????????length??? ????????? int????????? iterator
                extensTransformMethodVisitor.visitVarInsn(Opcodes.ISTORE, defineLocalVar.get(ARRAY_LENGTH_VARIABLE_NAME).getIndex());
                extensTransformMethodVisitor.visitLabel(defineLocalVar.get(ARRAY_LENGTH_VARIABLE_NAME).getStart());
                extensTransformMethodVisitor.visitVarInsn(Opcodes.ILOAD, defineLocalVar.get(ARRAY_LENGTH_VARIABLE_NAME).getIndex());
            }
            // ?????????????????????????????????????????????????????????(Collection ??????array) size?????????
            extensTransformMethodVisitor.visitLdcInsn(Integer.valueOf(0));
            extensTransformMethodVisitor.visitInsn(Opcodes.ISHL);
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, org.objectweb.asm.Type.getInternalName(targetClasImpl), BeanTransformsMethodAdapter.INIT_METHOD_NAME, "(I)V", false);

        }

        extensTransformMethodVisitor.visitVarInsn(Opcodes.ASTORE, targetVar.getIndex());
        extensTransformMethodVisitor.visitLabel(targetVar.getStart());

        LocalVariableInfo tempElement = defineLocalVar.get(TEMP_ELEMENT_VARIABLE_NAME);
        extensTransformMethodVisitor.visitInsn(Opcodes.ACONST_NULL);
        extensTransformMethodVisitor.visitVarInsn(Opcodes.ASTORE, tempElement.getIndex());
        extensTransformMethodVisitor.visitLabel(tempElement.getStart());

        LocalVariableInfo transformBaseTypeVar = defineLocalVar.get(TRANSFORM_BASETYPE_VAR);
        if (pattern == StrategyMode.COLLECTION_TO_COLLECTION_PATTERN) {
            LocalVariableInfo iteratorVar = defineLocalVar.get(ITERATOR_VARIABLE_NAME);
            Label whileJump = new Label();
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "iterator", "()Ljava/util/Iterator;", true);
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ASTORE, iteratorVar.getIndex());
            extensTransformMethodVisitor.visitLabel(iteratorVar.getStart());
            Label iteratorGotoLabel = new Label();
            // hasNext ????????????
            extensTransformMethodVisitor.visitLabel(iteratorGotoLabel);
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "hasNext", "()Z", true);
            extensTransformMethodVisitor.visitJumpInsn(Opcodes.IFEQ, whileJump);
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
            // ??????Iterator next ????????????
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "next", "()Ljava/lang/Object;", true);
            extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceElemType));
            // ??????????????????????????????
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ASTORE, tempElement.getIndex());

            /**
             * ?????????????????????????????????????????????????????????????????????????????????????????????extensionObjectTransform ?????????????????????
             *
             */

            // ??????????????? ??????Collection add ????????????????????????????????????
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());

            // transformByteCode(defineLocalVar, layer, sourceElemType, extensTransformMethodVisitor, newMethodPrefix, pattern);
            transformByteCode(defineLocalVar, layer, sourceElemType, extensTransformMethodVisitor, newMethodPrefix);

            if (sourceElemType == this.sourceElementType_local.get()) {
                // note ???????????????????????????????????????transformBaseTypeVar ???????????????load ??????
                typeLoadByteCode(targetElementType_local.get(), extensTransformMethodVisitor, transformBaseTypeVar.getIndex());
            }
            // ??????add ??????,????????????????????????
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "add", "(Ljava/lang/Object;)Z", true);
            extensTransformMethodVisitor.visitInsn(Opcodes.POP);
            //?????? while ??????????????????
            extensTransformMethodVisitor.visitJumpInsn(Opcodes.GOTO, iteratorGotoLabel);
            extensTransformMethodVisitor.visitLabel(whileJump);
        } else if (pattern == StrategyMode.ARRAY_TO_COLLECTION_PATTERN) {
            extensTransformMethodVisitor.visitLdcInsn(Integer.valueOf(0));
            LocalVariableInfo arrayIndex = defineLocalVar.get(ARRAY_INDEX_VARIABLE_NAME);
            LocalVariableInfo arrayLength = defineLocalVar.get(ARRAY_LENGTH_VARIABLE_NAME);
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ISTORE, arrayIndex.getIndex());
            extensTransformMethodVisitor.visitLabel(arrayIndex.getStart());
            if (targetClasImpl == Stack.class) {

                // ????????????length
                extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
                extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
                extensTransformMethodVisitor.visitInsn(Opcodes.ARRAYLENGTH);
                // ????????????length??? ????????? int????????? iterator
                extensTransformMethodVisitor.visitVarInsn(Opcodes.ISTORE, arrayLength.getIndex());
                extensTransformMethodVisitor.visitLabel(arrayLength.getStart());

            }
            Label gotoLabel = new Label();
            extensTransformMethodVisitor.visitLabel(gotoLabel);
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ILOAD, arrayIndex.getIndex());
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ILOAD, arrayLength.getIndex());
            Label forLoopExitLable = new Label();
            extensTransformMethodVisitor.visitJumpInsn(Opcodes.IF_ICMPGE, forLoopExitLable);

            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ILOAD, arrayIndex.getIndex());

            arrayElementLoad(sourceElemType, extensTransformMethodVisitor);
            extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceElemType));
            // ??????????????????????????????
            typeStoreByteCode(sourceElemType, extensTransformMethodVisitor, tempElement.getIndex());

            // ??????????????? ??????Collection add ????????????????????????????????????
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());

            // ??????????????????

            transformByteCode(defineLocalVar, layer, sourceElemType, extensTransformMethodVisitor, newMethodPrefix);
            if (sourceElemType == this.sourceElementType_local.get()) {
                // note ???????????????????????????????????????transformBaseTypeVar ???????????????load ??????
                typeLoadByteCode(targetElementType_local.get(), extensTransformMethodVisitor, transformBaseTypeVar.getIndex());
            }

//            if (targetClasImpl == Stack.class) {
//                extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,org.objectweb.asm.Type.getInternalName(Stack.class),"push","(Ljava/lang/Object;)Ljava/lang/Object;", false);
//            }else{
//                // ??????????????????(??????)add ????????????????????????????????????
//                extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "add", "(Ljava/lang/Object;)Z", true);
//            }

            // ??????????????????(??????)add ????????????????????????????????????
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "add", "(Ljava/lang/Object;)Z", true);

            extensTransformMethodVisitor.visitInsn(Opcodes.POP);
            //++index
            extensTransformMethodVisitor.visitIincInsn(arrayIndex.getIndex(), 1);
            // GOTO
            extensTransformMethodVisitor.visitJumpInsn(Opcodes.GOTO, gotoLabel);
            extensTransformMethodVisitor.visitLabel(forLoopExitLable);

        }


        // ???????????????????????????
        extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());
        extensTransformMethodVisitor.visitInsn(Opcodes.ARETURN);
        extensTransformMethodVisitor.visitLabel(endOfMethodBeanTransformsLable);
        // ?????????????????????
        BeanTransformsMethodAdapter.visitLocalVarivale(defineLocalVar, extensTransformMethodVisitor);
        // ???????????????????????????
        extensTransformMethodVisitor.visitMaxs(1, 1);

        extensTransformMethodVisitor.visitEnd();
        return true;
    }


    /**
     * ?????????????????????
     * ??????{@link AbstractComplexTypeStrategy#geneInstruction(ClassWriter, Type, Type, String)}
     *
     * @param extensTransformImplClassWriter
     * @param targetType
     * @param sourceBeanType
     * @param newMethodPrefix
     * @throws Exception ??????
     */
    @Override
    public void geneInstruction(ClassWriter extensTransformImplClassWriter, Type targetType, Type sourceBeanType, String newMethodPrefix) throws Exception {

        boolean methodGeneSuccess = false;
        MethodVisitor methodVisitorGen = extensTransformImplClassWriter.visitMethod(Opcodes.ACC_PUBLIC, TransformUtilGenerate.EXTEND_TRANSFORM_METHOD_NAME, TransformUtilGenerate.EXTEND_TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});
        this.extensTransformMethodVisitor_Local.set(methodVisitorGen);
        this.namePrefix_Local.set(newMethodPrefix);
        StrategyMode findMode = chooseStrategyMode(sourceBeanType, targetType);

        if (findMode == StrategyMode.COLLECTION_TO_COLLECTION_PATTERN) {

            ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
            ParameterizedType sourceParameterizedType = (ParameterizedType) sourceBeanType;
            List<Type> targetTypeList = resolveCollectionElenentType(targetParameterizedType);
            List<Type> sourceTypeList = resolveCollectionElenentType(sourceParameterizedType);
            this.sourceTypeList_Local.set(sourceTypeList);
            this.targetTypeList_Local.set(targetTypeList);

            if (collectionMatchCollection(sourceTypeList, targetTypeList)) {

                Class targetElementType = (Class) targetTypeList.get(targetTypeList.size() - 1);
                Class sourceElementType = (Class) sourceTypeList.get(sourceTypeList.size() - 1);
                //?????????????????????
                this.sourceElementType_local.set(sourceElementType);
                this.targetElementType_local.set(targetElementType);

                methodGeneSuccess= iterationGeneByteCode(targetTypeList,newMethodPrefix,extensTransformImplClassWriter,methodVisitorGen,findMode);


            }


        } else if (findMode== StrategyMode.ARRAY_TO_COLLECTION_PATTERN) {

            ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
            Class sourceClass = (Class) sourceBeanType;
            List<Type> targetTypeList = resolveCollectionElenentType(targetParameterizedType);
            // ????????????ComponentType ??????
            List<Class> sourceClassList = ArrayTypeStrategy.resolveArrayElementType(sourceClass);
            this.sourceClassList_Local.set(sourceClassList);
            this.targetTypeList_Local.set(targetTypeList);

            if (collectionMatchArrayType(sourceClassList, targetTypeList)) {

                Class targetElementType = (Class) targetTypeList.get(targetTypeList.size() - 1);
                Class sourceElementType = sourceClassList.get(sourceClassList.size() - 1);
                this.targetElementType_local.set(targetElementType);
                this.sourceElementType_local.set(sourceElementType);
                methodGeneSuccess= iterationGeneByteCode(targetTypeList,newMethodPrefix,extensTransformImplClassWriter,methodVisitorGen,findMode);

            }


        }

        if (!methodGeneSuccess) {
            // ?????????????????????????????????????????????????????????????????????
            new DefaultComplexTypeStrategy(methodVisitorGen).geneInstruction(extensTransformImplClassWriter, targetType, sourceBeanType, newMethodPrefix);

        }

    }

    /**
     * ?????????????????????Collection ?????????????????????????????????????????????
     *
     * @param targetTypeList  ??????????????????
     * @param newMethodPrefix ???????????????
     * @param classWriter     ????????????????????????
     * @param mv              mv
     * @param mode            ????????????
     * @return boolean
     * @throws Exception ??????
     */
    protected boolean iterationGeneByteCode(List<Type> targetTypeList, String newMethodPrefix, ClassWriter classWriter, MethodVisitor mv, StrategyMode mode) throws Exception {
        boolean methodGeneSuccess=false;
        for (int layer = 0; layer < targetTypeList.size() - 1; ++layer) {

            Class targetRawType = null;
            Class sourceRawType = null;
            Class sourceElemType = null;

            // Class sourceArrayElemClass=null;
            if(StrategyMode.COLLECTION_TO_COLLECTION_PATTERN==mode){
                targetRawType = (Class) ((ParameterizedType) this.targetTypeList_Local.get().get(layer)).getRawType();
                sourceRawType = (Class) ((ParameterizedType)  this.sourceTypeList_Local.get().get(layer)).getRawType();
                if ((sourceTypeList_Local.get().get(layer + 1)) instanceof Class) {
                    sourceElemType = (Class) sourceTypeList_Local.get().get(layer + 1);
                } else if ((sourceTypeList_Local.get().get(layer + 1)) instanceof ParameterizedType) {
                    sourceElemType = (Class) ((ParameterizedType) sourceTypeList_Local.get().get(layer + 1)).getRawType();
                }
            }else if(StrategyMode.ARRAY_TO_COLLECTION_PATTERN==mode){
                targetRawType = (Class) ((ParameterizedType) this.targetTypeList_Local.get().get(layer)).getRawType();
                sourceRawType = sourceClassList_Local.get().get(layer);
                sourceElemType =  sourceClassList_Local.get().get(layer + 1);

            }


            if (layer == 0) {
                // ????????????????????????
                methodGeneSuccess = visitCollectionTransformCode(mv, sourceRawType, targetRawType,  sourceElemType, newMethodPrefix, layer, mode);

            } else {
                MethodVisitor newArrayTransformMethod = classWriter.visitMethod(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, methodName(newMethodPrefix, layer), TransformUtilGenerate.EXTEND_TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});
                // ?????????????????????????????????????????????private???????????????????????? ???????????? ????????????+????????????????????????????????????????????????????????????
                // ?????????????????????????????????????????????????????? methodGeneSuccess ??????
                visitCollectionTransformCode(newArrayTransformMethod, sourceRawType, targetRawType, sourceElemType, newMethodPrefix, layer, mode);
            }
        }

        return methodGeneSuccess;
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
        return super.geneTransform(sourceBeanType, targetType, generateClassname, fieldNamePrefix);
    }


    /**
     * ???????????????????????????????????????????????????
     *
     * @param sourceBeanType ???bean??????
     * @param targetType     ????????????
     * @return {@link StrategyMode}
     * @throws Exception ??????
     */
    @Override
    public StrategyMode chooseStrategyMode(Type sourceBeanType, Type targetType) throws Exception {

        StrategyMode findStrategy = null;
        if ((targetType instanceof ParameterizedType) && (sourceBeanType instanceof ParameterizedType)) {
            ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
            ParameterizedType sourceParameterizedType = (ParameterizedType) sourceBeanType;

            this.targetRawType_local.set((Class) targetParameterizedType.getRawType());
            this.sourceRawType_local.set((Class) sourceParameterizedType.getRawType());
            if (Collection.class.isAssignableFrom(this.targetRawType_local.get())
                    && Collection.class.isAssignableFrom(this.sourceRawType_local.get())) {

                try {
                    resolveCollectionElenentType(targetParameterizedType);
                    findStrategy = StrategyMode.COLLECTION_TO_COLLECTION_PATTERN;
                } catch (BeanTransformException e) {
                    e.printStackTrace();
                }

                // findStrategy = StrategyMode.COLLECTION_TO_COLLECTION_PATTERN;
            }
        } else if ((targetType instanceof ParameterizedType) && (sourceBeanType instanceof Class)) {
            ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
            Class sourceClass = (Class) sourceBeanType;
            this.targetRawType_local.set((Class) targetParameterizedType.getRawType());
            this.sourceRawType_local.set(sourceClass);
            if (Collection.class.isAssignableFrom(this.targetRawType_local.get())
                    && sourceClass.isArray()) {

                try {
                    resolveCollectionElenentType(targetParameterizedType);
                    findStrategy = StrategyMode.ARRAY_TO_COLLECTION_PATTERN;
                } catch (BeanTransformException e) {
                    e.printStackTrace();
                }
                // findStrategy = StrategyMode.ARRAY_TO_COLLECTION_PATTERN;
            }
        }
        return findStrategy;
    }

    /**
     * ??????????????????
     * ??????{@link AbstractComplexTypeStrategy#strategyMatch(Type, Type)}
     *
     * @param sourceBeanType ???bean??????
     * @param targetType     ????????????
     * @return boolean
     * @throws Exception ??????
     */
    @Override
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws Exception {

        return super.strategyMatch(sourceBeanType, targetType);
    }

    /**
     * ??????threadlocal
     */
    @Override
    public void clearThreadLocal() {
        super.clearThreadLocal();
        sourceTypeList_Local.remove();
        targetTypeList_Local.remove();
        sourceClassList_Local.remove();

    }
}
