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
 * 集合类型策略
 *
 * @author wen wang
 * @date 2021/12/8 15:47
 */
public class CollectionTypeStrategy extends AbstractComplexTypeStrategy {

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger("ParameterizedTypeStrategy");

    /**
     * 源类型如果是Collection类型，迭代解析Collection类型及其内部元素，存储于sourceTypeList_Local 字段中
     */
    ThreadLocal<List<Type>> sourceTypeList_Local=new ThreadLocal<>();
    /**
     * 目标类型如果是Collection类型，迭代解析Collection类型及其内部元素，存储于targetTypeList_Local 字段中
     */
    ThreadLocal<List<Type>> targetTypeList_Local=new ThreadLocal<>();
    /**
     * 源类型如果是数组类型，迭代解析数组类型及其内部组件元素，存储于sourceClassList_Local 字段中
     */
    ThreadLocal<List<Class>> sourceClassList_Local=new ThreadLocal<>();


    /**
     *
     *
     * @param context 上下文
     */
    public CollectionTypeStrategy(AbstractContext context) {
        this.registerContext_local.set(context);
    }


    /**
     * 超类名字
     */
    public static final String SUPER_CLASS_NAME = org.objectweb.asm.Type.getInternalName(BeanTransFormsHandler.class);

    /**
     * 嵌套Collection 和 嵌套Collection 转换匹配条件判断
     * 1  层数一致
     * 2  除了最内层外，其它层级 是参数化的Collection 类型
     * 3  最内层，是Class 类型
     *
     * @param typeListSource 源类型列表
     * @param typeListTarget 目标类型列表
     * @return boolean
     * @throws Exception 异常
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
                    // 系统配置 strict.mode.flag 如果是严格模式，不转换，抛出异常
                    throw new BeanTransformException(CommonCode.TYPE_MISMATCH.getErrorCode(),
                            CommonCode.TYPE_MISMATCH.getErrorOutline(),
                            "源集合元素类型:" + sourceCollectionClass.getSimpleName()
                                    + "， 目标集合元素类型：" + targetCollectionEleClass.getSimpleName()
                                    + " , 无法转换, 如需默认转换，请在代码中设置系统变量strict.mode.flag=false");

                }

            }
        }
        return match;

    }

    /**
     * 集合变换函数，生成集合类转换字节码
     *
     * @param extensTransformMethodVisitor 方法访问器，保持和调用者一致
     * @param sourceRawType                源原始类型
     * @param targetRawType                目标原始类型
     * @param sourceElemType               源类elem元素类型
     * @param newMethodPrefix              新方法前缀
     * @param layer                        迭代层数，多层集合依次累加
     * @param pattern                      转换模式
     * @return boolean
     * @throws Exception 异常
     */
    private boolean visitCollectionTransformCode(MethodVisitor extensTransformMethodVisitor, Class sourceRawType, Class targetRawType, Class sourceElemType, String newMethodPrefix, int layer, StrategyMode pattern) throws Exception {
        if (!((pattern == StrategyMode.COLLECTION_TO_COLLECTION_PATTERN) || (pattern == StrategyMode.ARRAY_TO_COLLECTION_PATTERN))) {
            return false;
        }

        Class targetClasImpl = findCollectionImplementClass(targetRawType);
        extensTransformMethodVisitor.visitCode();
        final Label startOfMethodBeanTransformsLable = new Label();

        final Label endOfMethodBeanTransformsLable = new Label();
        // 定义变量
        // Map<String, LocalVariableInfo> defineLocalVar = defineLocalVar(startOfMethodBeanTransformsLable, endOfMethodBeanTransformsLable, targetRawType, sourceElemType,pattern,getOwnerClassInternalName(this.namePrefix.get(),this.collectionOrArraySourceRawType.get(),this.collectionTargetRawType.get()));
        Map<String, LocalVariableInfo> defineLocalVar = defineLocalVar(startOfMethodBeanTransformsLable, endOfMethodBeanTransformsLable, targetRawType, sourceElemType, pattern, getOwnerClassInternalName());

        // 方法起始位置打标签
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
         *  非空分支字节码
         */

        extensTransformMethodVisitor.visitTypeInsn(Opcodes.NEW, org.objectweb.asm.Type.getInternalName(targetClasImpl));
        extensTransformMethodVisitor.visitInsn(Opcodes.DUP);

        if (targetClasImpl == Stack.class) {
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, org.objectweb.asm.Type.getInternalName(targetClasImpl), BeanTransformsMethodAdapter.INIT_METHOD_NAME, "()V", false);
        } else {

            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            if (pattern == StrategyMode.COLLECTION_TO_COLLECTION_PATTERN) {
                // 源集合类size
                extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(Collection.class));
                extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "size", "()I", true);
            } else if (pattern == StrategyMode.ARRAY_TO_COLLECTION_PATTERN) {
                // 源数组类length
                extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
                extensTransformMethodVisitor.visitInsn(Opcodes.ARRAYLENGTH);
                // 源数组类length值 存储到 int型变量 iterator
                extensTransformMethodVisitor.visitVarInsn(Opcodes.ISTORE, defineLocalVar.get(ARRAY_LENGTH_VARIABLE_NAME).getIndex());
                extensTransformMethodVisitor.visitLabel(defineLocalVar.get(ARRAY_LENGTH_VARIABLE_NAME).getStart());
                extensTransformMethodVisitor.visitVarInsn(Opcodes.ILOAD, defineLocalVar.get(ARRAY_LENGTH_VARIABLE_NAME).getIndex());
            }
            // 避免扩容影响效率，初始大小设置为源对象(Collection 或者array) size的两倍
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
            // hasNext 回跳标签
            extensTransformMethodVisitor.visitLabel(iteratorGotoLabel);
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "hasNext", "()Z", true);
            extensTransformMethodVisitor.visitJumpInsn(Opcodes.IFEQ, whileJump);
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
            // 调用Iterator next 接口方法
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "next", "()Ljava/lang/Object;", true);
            extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceElemType));
            // 迭代元素存入局部变量
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ASTORE, tempElement.getIndex());

            /**
             * 内层转换方法调用，嵌套泛型最内层转换方法与前面不同，不递归调用extensionObjectTransform 方法，新建方法
             *
             */

            // 目标类对象 调用Collection add 方法，目标对象变量入栈，
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());

            // transformByteCode(defineLocalVar, layer, sourceElemType, extensTransformMethodVisitor, newMethodPrefix, pattern);
            transformByteCode(defineLocalVar, layer, sourceElemType, extensTransformMethodVisitor, newMethodPrefix);

            if (sourceElemType == this.sourceElementType_local.get()) {
                // note 最内层元素的转换结果存储于transformBaseTypeVar 变量，需先load 加载
                typeLoadByteCode(targetElementType_local.get(), extensTransformMethodVisitor, transformBaseTypeVar.getIndex());
            }
            // 调用add 方法,相关参数已经入栈
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "add", "(Ljava/lang/Object;)Z", true);
            extensTransformMethodVisitor.visitInsn(Opcodes.POP);
            //回跳 while 循环判断标签
            extensTransformMethodVisitor.visitJumpInsn(Opcodes.GOTO, iteratorGotoLabel);
            extensTransformMethodVisitor.visitLabel(whileJump);
        } else if (pattern == StrategyMode.ARRAY_TO_COLLECTION_PATTERN) {
            extensTransformMethodVisitor.visitLdcInsn(Integer.valueOf(0));
            LocalVariableInfo arrayIndex = defineLocalVar.get(ARRAY_INDEX_VARIABLE_NAME);
            LocalVariableInfo arrayLength = defineLocalVar.get(ARRAY_LENGTH_VARIABLE_NAME);
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ISTORE, arrayIndex.getIndex());
            extensTransformMethodVisitor.visitLabel(arrayIndex.getStart());
            if (targetClasImpl == Stack.class) {

                // 源数组类length
                extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
                extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
                extensTransformMethodVisitor.visitInsn(Opcodes.ARRAYLENGTH);
                // 源数组类length值 存储到 int型变量 iterator
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
            // 迭代元素存入局部变量
            typeStoreByteCode(sourceElemType, extensTransformMethodVisitor, tempElement.getIndex());

            // 目标类对象 调用Collection add 方法，目标对象变量入栈，
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());

            // 临时对象转换

            transformByteCode(defineLocalVar, layer, sourceElemType, extensTransformMethodVisitor, newMethodPrefix);
            if (sourceElemType == this.sourceElementType_local.get()) {
                // note 最内层元素的转换结果存储于transformBaseTypeVar 变量，需先load 加载
                typeLoadByteCode(targetElementType_local.get(), extensTransformMethodVisitor, transformBaseTypeVar.getIndex());
            }

//            if (targetClasImpl == Stack.class) {
//                extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,org.objectweb.asm.Type.getInternalName(Stack.class),"push","(Ljava/lang/Object;)Ljava/lang/Object;", false);
//            }else{
//                // 调用目标对象(集合)add 方法，转换后对象加入集合
//                extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "add", "(Ljava/lang/Object;)Z", true);
//            }

            // 调用目标对象(集合)add 方法，转换后对象加入集合
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "add", "(Ljava/lang/Object;)Z", true);

            extensTransformMethodVisitor.visitInsn(Opcodes.POP);
            //++index
            extensTransformMethodVisitor.visitIincInsn(arrayIndex.getIndex(), 1);
            // GOTO
            extensTransformMethodVisitor.visitJumpInsn(Opcodes.GOTO, gotoLabel);
            extensTransformMethodVisitor.visitLabel(forLoopExitLable);

        }


        // 退出循环，返回结果
        extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());
        extensTransformMethodVisitor.visitInsn(Opcodes.ARETURN);
        extensTransformMethodVisitor.visitLabel(endOfMethodBeanTransformsLable);
        // 定义局部变量表
        BeanTransformsMethodAdapter.visitLocalVarivale(defineLocalVar, extensTransformMethodVisitor);
        // 方法末位位置打标签
        extensTransformMethodVisitor.visitMaxs(1, 1);

        extensTransformMethodVisitor.visitEnd();
        return true;
    }


    /**
     * 生成转换指令，
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
                //最内层元素类型
                this.sourceElementType_local.set(sourceElementType);
                this.targetElementType_local.set(targetElementType);

                methodGeneSuccess= iterationGeneByteCode(targetTypeList,newMethodPrefix,extensTransformImplClassWriter,methodVisitorGen,findMode);


            }


        } else if (findMode== StrategyMode.ARRAY_TO_COLLECTION_PATTERN) {

            ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
            Class sourceClass = (Class) sourceBeanType;
            List<Type> targetTypeList = resolveCollectionElenentType(targetParameterizedType);
            // 数组类，ComponentType 解析
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
            // 不满足以上条件，也需要实现接口方法，默认返回空
            new DefaultComplexTypeStrategy(methodVisitorGen).geneInstruction(extensTransformImplClassWriter, targetType, sourceBeanType, newMethodPrefix);

        }

    }

    /**
     * 迭代函数，多层Collection 依次迭代生成对应层级的转换指令
     *
     * @param targetTypeList  目标类型列表
     * @param newMethodPrefix 新方法前缀
     * @param classWriter     和调用者保持一致
     * @param mv              mv
     * @param mode            转换模式
     * @return boolean
     * @throws Exception 异常
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
                // 编写方法体字节码
                methodGeneSuccess = visitCollectionTransformCode(mv, sourceRawType, targetRawType,  sourceElemType, newMethodPrefix, layer, mode);

            } else {
                MethodVisitor newArrayTransformMethod = classWriter.visitMethod(Opcodes.ACC_PRIVATE, methodName(newMethodPrefix, layer), TransformUtilGenerate.EXTEND_TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});
                // 内部元素转换方法，非接口方法，private修饰，内部调用。 方法名是 公共前缀+层级信息，内层集合类依次循环创建转换方法
                // 注意，只需要第一层转换执行结果赋值给 methodGeneSuccess 即可
                visitCollectionTransformCode(newArrayTransformMethod, sourceRawType, targetRawType, sourceElemType, newMethodPrefix, layer, mode);
            }
        }

        return methodGeneSuccess;
    }

    /**
     * 生成转换类对象，封装与Map  中，主要针对Collection、Map、Array等复杂类型字段
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
        return super.geneTransform(sourceBeanType, targetType, generateClassname, fieldNamePrefix);
    }


    /**
     * 选择复杂类型处理模式，实现父类方法
     *
     * @param sourceBeanType 源bean类型
     * @param targetType     目标类型
     * @return {@link StrategyMode}
     * @throws Exception 异常
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
     * 转换策略匹配
     * 详见{@link AbstractComplexTypeStrategy#strategyMatch(Type, Type)}
     *
     * @param sourceBeanType 源bean类型
     * @param targetType     目标类型
     * @return boolean
     * @throws Exception 异常
     */
    @Override
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws Exception {

        return super.strategyMatch(sourceBeanType, targetType);
    }

    /**
     * 清理threadlocal
     */
    @Override
    public void clearThreadLocal() {
        super.clearThreadLocal();
        sourceTypeList_Local.remove();
        targetTypeList_Local.remove();
        sourceClassList_Local.remove();

    }
}
