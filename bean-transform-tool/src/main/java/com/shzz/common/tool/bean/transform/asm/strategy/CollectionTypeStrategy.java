package com.shzz.common.tool.bean.transform.asm.strategy;

import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
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
 * @Classname ParameterizedTypeStrategy
 * @Description TODO
 * @Date 2021/12/8 15:47
 * @Created by wen wang
 */
public class CollectionTypeStrategy extends AbstractComplexTypeStrategy {

    private static final Logger LOG = LoggerFactory.getLogger("ParameterizedTypeStrategy");

    ThreadLocal<List<Type>> sourceTypeList_Local=new ThreadLocal<>();
    ThreadLocal<List<Type>> targetTypeList_Local=new ThreadLocal<>();
    ThreadLocal<List<Class>> sourceClassList_Local=new ThreadLocal<>();


    public CollectionTypeStrategy(AbstractContext context) {
        this.registerContext_local.set(context);
    }


    public static final String SUPER_CLASS_NAME = org.objectweb.asm.Type.getInternalName(BeanTransFormsHandler.class);

    public static boolean collectionMatchCollection(List<Type> typeListSource, List<Type> typeListTarget) {
        /**
         * @description: 嵌套Collection 和 嵌套Collection 转换匹配条件判断
         * 1  层数一致
         * 2  除了最内层外，其它层级 是参数化的Collection 类型
         * 3  最内层，是Class 类型
         * @param typeList1
         * @param typeList2
         * @return: boolean
         * @auther: wen wang
         * @date: 2021/12/10 16:39
         */
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


        }
        return match;

    }

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
         *   非空分支字节码
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
                extensTransformMethodVisitor.visitVarInsn(Opcodes.ILOAD, defineLocalVar.get(ARRAY_LENGTH_VARIABLE_NAME).getIndex());
            }
            // 避免扩容影响效率，初始大小设置为源对象(Collection 或者array) size的两倍
            extensTransformMethodVisitor.visitLdcInsn(Integer.valueOf(2));
            extensTransformMethodVisitor.visitInsn(Opcodes.IMUL);
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, org.objectweb.asm.Type.getInternalName(targetClasImpl), BeanTransformsMethodAdapter.INIT_METHOD_NAME, "(I)V", false);

        }

        extensTransformMethodVisitor.visitVarInsn(Opcodes.ASTORE, targetVar.getIndex());
        extensTransformMethodVisitor.visitLabel(targetVar.getStart());

        LocalVariableInfo tempElement = defineLocalVar.get(TEMP_ELEMENT_VARIABLE_NAME);
        extensTransformMethodVisitor.visitInsn(Opcodes.ACONST_NULL);
        extensTransformMethodVisitor.visitVarInsn(Opcodes.ASTORE, tempElement.getIndex());
        extensTransformMethodVisitor.visitLabel(tempElement.getStart());


        if (pattern == StrategyMode.COLLECTION_TO_COLLECTION_PATTERN) {
            LocalVariableInfo iteratorVar = defineLocalVar.get(ITERATOR_VARIABLE_NAME);
            Label whileJump = new Label();
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            extensTransformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
            extensTransformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "iterator", "()Ljava/util/Iterator;", true);
            extensTransformMethodVisitor.visitVarInsn(Opcodes.ASTORE, iteratorVar.getIndex());
            extensTransformMethodVisitor.visitLabel(iteratorVar.getStart());
            Label iteratorGotoLabel = new Label();
            extensTransformMethodVisitor.visitLabel(iteratorGotoLabel); // hasNext 回跳标签
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

            transformByteCode(defineLocalVar, layer, sourceElemType, extensTransformMethodVisitor, newMethodPrefix, pattern);

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
            transformByteCode(defineLocalVar, layer, sourceElemType, extensTransformMethodVisitor, newMethodPrefix, pattern);
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
                List<Class> sourceClassList = ArrayTypeStrategy.resolveArrayElenentType(sourceClass);
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

    @Override
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception {
        return super.geneTransform(sourceBeanType, targetType, generateClassname, fieldNamePrefix);
    }


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
                findStrategy = StrategyMode.COLLECTION_TO_COLLECTION_PATTERN;
            }
        } else if ((targetType instanceof ParameterizedType) && (sourceBeanType instanceof Class)) {
            ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
            Class sourceClass = (Class) sourceBeanType;
            this.targetRawType_local.set((Class) targetParameterizedType.getRawType());
            this.sourceRawType_local.set(sourceClass);
            if (Collection.class.isAssignableFrom(this.targetRawType_local.get())
                    && sourceClass.isArray()) {
                findStrategy = StrategyMode.ARRAY_TO_COLLECTION_PATTERN;
            }
        }
        return findStrategy;
    }

    @Override
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws Exception {

        return super.strategyMatch(sourceBeanType, targetType);
    }

    @Override
    public void clearThreadLocal() {
        super.clearThreadLocal();
    }
}
