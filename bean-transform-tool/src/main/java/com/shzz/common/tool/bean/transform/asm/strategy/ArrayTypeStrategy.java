package com.shzz.common.tool.bean.transform.asm.strategy;

import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.SystemProperties;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.LocalVariableInfo;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.TypeTransformAssist;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.bean.transform.asm.context.Context;
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

import static com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter.visitLocalVarivale;
import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.EXTEND_TRANSFORM_METHOD_DESC;
import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.EXTEND_TRANSFORM_METHOD_NAME;
import static com.shzz.common.tool.bean.transform.asm.strategy.StrategyMode.*;

/**
 * @Classname ArrayTypeStrategy
 * @Description TODO
 * @Date 2021/12/24 16:27
 * @Created by wen wang
 */
public class ArrayTypeStrategy extends AbstractComplexTypeStrategy {
    private static final Logger LOG = LoggerFactory.getLogger("ArrayTypeStrategy");

    // 数组维数targetArrayDems，一维和多维对应的字节码指令不同,每层新建数组时需要更新维度值，
    ThreadLocal<Integer> targetArrayDems = new ThreadLocal<>();

    ThreadLocal<List<Type>> sourceTypeList_Local = new ThreadLocal<>();
    ThreadLocal<List<Class>> sourceClassList_Local = new ThreadLocal<>();

    public ArrayTypeStrategy(AbstractContext context) {
        this.registerContext_local.set(context);
    }

    private void createArray(Class arrayRawClass, Class arrayElemClass, int dems, MethodVisitor mv, int lengthVarIndex) {
        //数组长度变量
        mv.visitVarInsn(Opcodes.ILOAD, lengthVarIndex);
        if (dems == 1) {

            if (arrayElemClass == byte.class) {
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
            } else if (arrayElemClass == char.class) {
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_CHAR);
            } else if (arrayElemClass == boolean.class) {
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
            } else if (arrayElemClass == short.class) {
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_SHORT);
            } else if (arrayElemClass == int.class) {
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
            } else if (arrayElemClass == long.class) {
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
            } else if (arrayElemClass == float.class) {
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
            } else if (arrayElemClass == double.class) {
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_DOUBLE);
            } else if (TypeTransformAssist.referenceType(arrayElemClass)) {
                mv.visitTypeInsn(Opcodes.ANEWARRAY, org.objectweb.asm.Type.getInternalName(arrayElemClass));
            }

        } else if (dems > 1) {
            //数组长度变量,加载第一维length
            mv.visitVarInsn(Opcodes.ILOAD, lengthVarIndex);
            for (int dem = 2; dem <= dems; ++dem) {
                // 其他维度设置常量1即可
                mv.visitLdcInsn(Integer.valueOf(1));
            }

            // mv.visitTypeInsn(Opcodes.ANEWARRAY,org.objectweb.asm.Type.getInternalName(arrayRawClass));
            mv.visitMultiANewArrayInsn(org.objectweb.asm.Type.getDescriptor(arrayRawClass), dems);
        }

    }

    private boolean visitArrayTransformCode(MethodVisitor transformMethodVisitor, Class sourceRawType, Class targetRawType, Class sourceElemType, Class targetElemType, String newMethodPrefix, int layer, StrategyMode pattern) throws Exception {
        if (!((pattern == StrategyMode.COLLECTION_TO_ARRAY_PATTERN) || (pattern == StrategyMode.ARRAY_TO_ARRAY_PATTERN))) {
            return false;
        }

        transformMethodVisitor.visitCode();
        final Label startOfMethodLable = new Label();

        final Label endOfMethodLable = new Label();
        // 定义变量
        Map<String, LocalVariableInfo> localVar = new HashMap<>();
        localVar = defineLocalVar(startOfMethodLable, endOfMethodLable, targetRawType, sourceElemType, pattern, getOwnerClassInternalName());

        // 方法起始位置打标签
        transformMethodVisitor.visitLabel(startOfMethodLable);

        LocalVariableInfo targetVar = localVar.get(TARGET_VARIABLE_NAME);
        LocalVariableInfo sourceObjectVar = localVar.get("sourceObject");
        transformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
        Label transformStart = new Label();
        transformMethodVisitor.visitJumpInsn(Opcodes.IFNONNULL, transformStart);
        transformMethodVisitor.visitInsn(Opcodes.ACONST_NULL);
        transformMethodVisitor.visitInsn(Opcodes.ARETURN);

        transformMethodVisitor.visitLabel(transformStart);
        transformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
        if (pattern == StrategyMode.COLLECTION_TO_ARRAY_PATTERN) {
            // 源集合类size
            transformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(Collection.class));
            transformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "size", "()I", true);

        } else if (pattern == StrategyMode.ARRAY_TO_ARRAY_PATTERN) {
            transformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
            transformMethodVisitor.visitInsn(Opcodes.ARRAYLENGTH);
        }
        LocalVariableInfo arrayLength = localVar.get(ARRAY_LENGTH_VARIABLE_NAME);
        transformMethodVisitor.visitVarInsn(Opcodes.ISTORE, arrayLength.getIndex());

        createArray(targetRawType, targetElementType_local.get(), this.targetArrayDems.get(), transformMethodVisitor, arrayLength.getIndex());

        transformMethodVisitor.visitVarInsn(Opcodes.ASTORE, targetVar.getIndex());

        // index 变量赋初始值0
        transformMethodVisitor.visitLdcInsn(Integer.valueOf(0));
        LocalVariableInfo arrayIndex = localVar.get(ARRAY_INDEX_VARIABLE_NAME);
        transformMethodVisitor.visitVarInsn(Opcodes.ISTORE, arrayIndex.getIndex());


        LocalVariableInfo tempElement = localVar.get(TEMP_ELEMENT_VARIABLE_NAME);
        if (pattern == StrategyMode.COLLECTION_TO_ARRAY_PATTERN) {
            LocalVariableInfo iteratorVar = localVar.get(ITERATOR_VARIABLE_NAME);
            // Collection 子类，通过Iterator 迭代赋值目标数组
            Label whileJump = new Label();
            transformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            transformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
            transformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Collection.class), "iterator", "()Ljava/util/Iterator;", true);
            transformMethodVisitor.visitVarInsn(Opcodes.ASTORE, iteratorVar.getIndex());
            transformMethodVisitor.visitLabel(iteratorVar.getStart());
            Label iteratorGotoLabel = new Label();
            transformMethodVisitor.visitLabel(iteratorGotoLabel); // hasNext 回跳标签
            transformMethodVisitor.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
            transformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "hasNext", "()Z", true);
            transformMethodVisitor.visitJumpInsn(Opcodes.IFEQ, whileJump);
            transformMethodVisitor.visitVarInsn(Opcodes.ALOAD, iteratorVar.getIndex());
            // 调用Iterator next 接口方法
            transformMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(Iterator.class), "next", "()Ljava/lang/Object;", true);
            transformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceElemType));
            // 迭代元素存入局部变量
            transformMethodVisitor.visitVarInsn(Opcodes.ASTORE, tempElement.getIndex());

            // 存入数组元素值，1 加载数组对象-> 2 加载索引值 -> 3 加载 需要存入的元素值 -> store
            transformMethodVisitor.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());
            transformMethodVisitor.visitVarInsn(Opcodes.ILOAD, arrayIndex.getIndex());

            /**
             * 内层转换方法调用，嵌套泛型最内层转换方法与前面不同，不递归调用extensionObjectTransform 方法，新建方法
             *
             */
            //transformByteCode(localVar, layer, sourceElemType, transformMethodVisitor, newMethodPrefix, pattern);
            transformByteCode(localVar, layer, sourceElemType, transformMethodVisitor, newMethodPrefix);

//            if (TypeTransformAssist.isPrimitiveType(targetElemType)) {
//                Class primitiveMapType = TypeTransformAssist.typeMap(targetElemType);
//
//                transformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(primitiveMapType));
//
//                TypeTransformAssist.baseTypeProcessByteCode(targetElemType, primitiveMapType, transformMethodVisitor, true);
//
//            }
            arrayElementStore(targetElemType, transformMethodVisitor);
            //++index
            transformMethodVisitor.visitIincInsn(arrayIndex.getIndex(), 1);
            //回跳 while 循环判断标签
            transformMethodVisitor.visitJumpInsn(Opcodes.GOTO, iteratorGotoLabel);
            transformMethodVisitor.visitLabel(whileJump);
        } else if (pattern == StrategyMode.ARRAY_TO_ARRAY_PATTERN) {
            // for 循环 代码体
            Label gotoLabel = new Label();
            transformMethodVisitor.visitLabel(gotoLabel);
            transformMethodVisitor.visitVarInsn(Opcodes.ILOAD, arrayIndex.getIndex());
            transformMethodVisitor.visitVarInsn(Opcodes.ILOAD, arrayLength.getIndex());
            Label forLoopExitLable = new Label();
            transformMethodVisitor.visitJumpInsn(Opcodes.IF_ICMPGE, forLoopExitLable);
            transformMethodVisitor.visitVarInsn(Opcodes.ALOAD, sourceObjectVar.getIndex());
            transformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceRawType));
            transformMethodVisitor.visitVarInsn(Opcodes.ILOAD, arrayIndex.getIndex());
            arrayElementLoad(sourceElemType, transformMethodVisitor);
            transformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(sourceElemType));
            // 迭代元素存入局部变量
            typeStoreByteCode(sourceElemType, transformMethodVisitor, tempElement.getIndex());
            // 存入数组元素值，1 加载数组对象-> 2 加载索引值 -> 3 加载 需要存入的元素值 -> store
            transformMethodVisitor.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());
            transformMethodVisitor.visitVarInsn(Opcodes.ILOAD, arrayIndex.getIndex());
            // 临时对象转换
            //transformByteCode(localVar, layer, sourceElemType, transformMethodVisitor, newMethodPrefix, pattern);
            transformByteCode(localVar, layer, sourceElemType, transformMethodVisitor, newMethodPrefix);
//            if (TypeTransformAssist.isPrimitiveType(targetElemType)) {
//                Class primitiveMapType = TypeTransformAssist.typeMap(targetElemType);
//
//                transformMethodVisitor.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(primitiveMapType));
//
//                try {
//                    TypeTransformAssist.baseTypeProcessByteCode(targetElemType, primitiveMapType, transformMethodVisitor, true);
//                } catch (Exception e) {
//                    LOG.error(e.toString());
//                }
//            }
            arrayElementStore(targetElemType, transformMethodVisitor);
            //++index
            transformMethodVisitor.visitIincInsn(arrayIndex.getIndex(), 1);
            //GOTO
            transformMethodVisitor.visitJumpInsn(Opcodes.GOTO, gotoLabel);
            transformMethodVisitor.visitLabel(forLoopExitLable);
        }
        // 退出循环，返回结果
        transformMethodVisitor.visitVarInsn(Opcodes.ALOAD, targetVar.getIndex());
        transformMethodVisitor.visitInsn(Opcodes.ARETURN);
        transformMethodVisitor.visitLabel(endOfMethodLable);
        // 定义局部变量表
        visitLocalVarivale(localVar, transformMethodVisitor);
        // 方法末位位置打标签
        transformMethodVisitor.visitMaxs(1, 1);

        transformMethodVisitor.visitEnd();


        return true;
    }

    protected boolean iterationGeneByteCode(List<Class> targetClassList, String newMethodPrefix, ClassWriter extensTransformImplClassWriter, MethodVisitor extensTransformMethodVisitor, StrategyMode mode) throws Exception {
        boolean methodGeneSuccess = false;
        for (int layer = 0; layer < targetClassList.size() - 1; ++layer) {
            Class targetRawType = targetClassList.get(layer);
            Class targetElemType = targetClassList.get(layer + 1);
            Class sourceRawType = null;
            Class sourceElemType = null;
            if (StrategyMode.COLLECTION_TO_ARRAY_PATTERN == mode) {
                sourceRawType = (Class) ((ParameterizedType) sourceTypeList_Local.get().get(layer)).getRawType();
                if ((sourceTypeList_Local.get().get(layer + 1)) instanceof Class) {
                    sourceElemType = (Class) sourceTypeList_Local.get().get(layer + 1);
                } else if ((sourceTypeList_Local.get().get(layer + 1)) instanceof ParameterizedType) {
                    sourceElemType = (Class) ((ParameterizedType) sourceTypeList_Local.get().get(layer + 1)).getRawType();
                }
            } else if (StrategyMode.ARRAY_TO_ARRAY_PATTERN == mode) {
                sourceRawType = sourceClassList_Local.get().get(layer);
                sourceElemType = sourceClassList_Local.get().get(layer + 1);
            }

            this.targetArrayDems.set(targetClassList.size() - layer - 1);
            if (layer == 0) {

                // 编写方法体字节码
                methodGeneSuccess = visitArrayTransformCode(extensTransformMethodVisitor, sourceRawType, targetRawType, sourceElemType, targetElemType, newMethodPrefix, layer, mode);

            } else {
                MethodVisitor newCollectionTransformMethod = extensTransformImplClassWriter.visitMethod(Opcodes.ACC_PRIVATE, methodName(newMethodPrefix, layer), EXTEND_TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});
                // 内部元素转换方法，非接口方法，private修饰，内部调用。 方法名是 公共前缀+层级信息，内层集合类依次循环创建转换方法
                visitArrayTransformCode(newCollectionTransformMethod, sourceRawType, targetRawType, sourceElemType, targetElemType, newMethodPrefix, layer, mode);
            }
        }

        return methodGeneSuccess;
    }

    @Override
    public void geneInstruction(ClassWriter extensTransformImplClassWriter, Type targetType, Type sourceBeanType, String newMethodPrefix) throws Exception {
        boolean methodGeneSuccess = false;
        MethodVisitor extensTransformMethodVisitor = extensTransformImplClassWriter.visitMethod(Opcodes.ACC_PUBLIC, EXTEND_TRANSFORM_METHOD_NAME, EXTEND_TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});
        this.namePrefix_Local.set(newMethodPrefix);
        StrategyMode findMode = chooseStrategyMode(sourceBeanType, targetType);
        if (Objects.nonNull(findMode)) {
            if (StrategyMode.ARRAY_TO_ARRAY_PATTERN == findMode) {
                // 数组类，ComponentType 解析
                List<Class> targetClassList = resolveArrayElenentType(this.targetRawType_local.get());
                List<Class> sourceClassList = resolveArrayElenentType(this.sourceRawType_local.get());
                sourceClassList_Local.set(sourceClassList);
                if (arrayMatchArray(targetClassList, sourceClassList)) {
                    this.targetElementType_local.set(targetClassList.get(targetClassList.size() - 1));
                    this.sourceElementType_local.set(sourceClassList.get(sourceClassList.size() - 1));
                    methodGeneSuccess = iterationGeneByteCode(targetClassList, newMethodPrefix, extensTransformImplClassWriter, extensTransformMethodVisitor, findMode);

                }

            } else if (StrategyMode.COLLECTION_TO_ARRAY_PATTERN == findMode) {
                ParameterizedType sourceParameterizedType = (ParameterizedType) sourceBeanType;
                List<Type> sourceTypeList = resolveCollectionElenentType(sourceParameterizedType);
                // 数组类，ComponentType 解析
                List<Class> targetClassList = resolveArrayElenentType(this.targetRawType_local.get());
                sourceTypeList_Local.set(sourceTypeList);
                if (collectionMatchArrayType(targetClassList, sourceTypeList)) {

                    this.targetElementType_local.set(targetClassList.get(targetClassList.size() - 1));
                    this.sourceElementType_local.set((Class) sourceTypeList.get(sourceTypeList.size() - 1));

                    methodGeneSuccess = iterationGeneByteCode(targetClassList, newMethodPrefix, extensTransformImplClassWriter, extensTransformMethodVisitor, findMode);

                }
            }
        }
        if (!methodGeneSuccess) {
            // 不满足以上条件，也需要实现接口方法，默认返回空
            new DefaultComplexTypeStrategy(extensTransformMethodVisitor).geneInstruction(extensTransformImplClassWriter, targetType, sourceBeanType, newMethodPrefix);

        }

    }

    public static boolean arrayMatchArray(List<Class> targetArrayTypeList, List<Class> sourceArrayTypeList) throws Exception {

        boolean match = true;

        if (Objects.isNull(targetArrayTypeList) || Objects.isNull(sourceArrayTypeList)) {
            match = false;
        }


        if (targetArrayTypeList.size() != sourceArrayTypeList.size()) {
            // 维度不匹配
            match = false;
        }

        Class targetArrayEleClass = targetArrayTypeList.get(targetArrayTypeList.size() - 1);
        Class sourceArrayEleClass = sourceArrayTypeList.get(sourceArrayTypeList.size() - 1);

        boolean matchConditional1 = (TypeTransformAssist.isBaseType(targetArrayEleClass) && (!TypeTransformAssist.isBaseType(sourceArrayEleClass)));
        boolean matchConditional2 = (!TypeTransformAssist.isBaseType(targetArrayEleClass) && (TypeTransformAssist.isBaseType(sourceArrayEleClass)));

        if (matchConditional1 || matchConditional2) {
            if (SystemProperties.getStrictModeFlag()) {
                // 系统配置 strict.mode.flag 如果是严格模式，不转换，抛出异常
                throw new BeanTransformException(CommonCode.TYPE_MISMATCH.getErrorCode(),
                        CommonCode.TYPE_MISMATCH.getErrorOutline(),
                        "源数组元素类型:" + sourceArrayEleClass.getSimpleName()
                                + "， 目标数组元素类型：" + targetArrayEleClass.getSimpleName() +
                                " 无法转换, 如需默认转换，请在代码中设置系统变量strict.mode.flag=false ");

            }


        }


        return match;


    }

    @Override
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception {
        return super.geneTransform(sourceBeanType, targetType, generateClassname, fieldNamePrefix);
    }

    @Override
    public StrategyMode chooseStrategyMode(Type sourceBeanType, Type targetType) throws Exception {
        StrategyMode strategyMode = null;
        Class targetClass = null;
        Class sourceClass = null;
        if ((targetType instanceof Class) && (sourceBeanType instanceof ParameterizedType)) {
            ParameterizedType sourceParameterizedType = (ParameterizedType) sourceBeanType;
            targetClass = (Class) targetType;
            sourceClass = (Class) sourceParameterizedType.getRawType();
            this.targetRawType_local.set(targetClass);
            this.sourceRawType_local.set(sourceClass);
            if (targetClass.isArray() && (Collection.class.isAssignableFrom(sourceClass))) {
                strategyMode = StrategyMode.COLLECTION_TO_ARRAY_PATTERN;
            }
        } else if ((targetType instanceof Class) && (sourceBeanType instanceof Class)) {
            targetClass = (Class) targetType;
            sourceClass = (Class) sourceBeanType;
            this.targetRawType_local.set(targetClass);
            this.sourceRawType_local.set(sourceClass);
            if (targetClass.isArray() && sourceClass.isArray()) {
                strategyMode = StrategyMode.ARRAY_TO_ARRAY_PATTERN;
            }
        }

        return strategyMode;
    }

    @Override
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws Exception {

        return super.strategyMatch(sourceBeanType, targetType);

    }

    @Override
    public void clearThreadLocal() {
        super.clearThreadLocal();
        targetArrayDems.remove();
        sourceTypeList_Local.remove();
        sourceClassList_Local.remove();


    }
}
