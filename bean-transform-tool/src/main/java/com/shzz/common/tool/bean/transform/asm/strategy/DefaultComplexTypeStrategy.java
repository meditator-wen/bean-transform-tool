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
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import com.shzz.common.tool.bean.transform.asm.context.Context;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.context.AbstractContext;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.shzz.common.tool.bean.transform.asm.BeanTransformsMethodAdapter.*;
import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.*;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static com.shzz.common.tool.bean.transform.asm.strategy.StrategyMode.*;

/**
 * 默认复杂类型策略
 *
 * @author wen wang
 * @date 2021/12/26 20:36
 */
public class DefaultComplexTypeStrategy extends AbstractComplexTypeStrategy {

    /**
     * 默认复杂类型策略
     *
     * @param context 上下文
     */
    public DefaultComplexTypeStrategy(AbstractContext context){
        this.registerContext_local.set(context);
    }

    /**
     * 默认复杂类型策略
     *
     * @param mv mv
     */
    public DefaultComplexTypeStrategy(MethodVisitor mv){
        extensTransformMethodVisitor_Local.set(mv);
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

        //默认实现，空值转换
        MethodVisitor methodVisitorLoacl = extensTransformMethodVisitor_Local.get();
        if (Objects.isNull(methodVisitorLoacl)) {

            methodVisitorLoacl = extensTransformImplClassWriter.visitMethod(Opcodes.ACC_PUBLIC, TransformUtilGenerate.EXTEND_TRANSFORM_METHOD_NAME, TransformUtilGenerate.EXTEND_TRANSFORM_METHOD_DESC, null, new String[]{"java/lang/Exception"});
            extensTransformMethodVisitor_Local.set(methodVisitorLoacl);
        }

        methodVisitorLoacl.visitCode();
        final Label startOfMethodBeanTransformsLable = new Label();
        final Label endOfMethodBeanTransformsLable = new Label();
        methodVisitorLoacl.visitInsn(Opcodes.ACONST_NULL);
        methodVisitorLoacl.visitInsn(Opcodes.ARETURN);
        // 定义局部变量表
        BeanTransformsMethodAdapter.visitLocalVarivale(defineMethodParameterVar(startOfMethodBeanTransformsLable, endOfMethodBeanTransformsLable, getOwnerClassInternalName()), extensTransformMethodVisitor_Local.get());
        // 方法末位位置打标签
        methodVisitorLoacl.visitMaxs(1, 1);
        methodVisitorLoacl.visitEnd();
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

        ClassWriter extensTransformImplClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        TransformUtilGenerate.checkGenerateClassname(generateClassname);
        String internalName = generateClassname.replace('.', '/');

        // 创建类名
        extensTransformImplClassWriter.visit(getClassVersion(),
                ACC_PUBLIC+ACC_FINAL,
                internalName,
                null,
                TransformUtilGenerate.OBJECT_CLASS_INTERNAL_NAME, new String[]{TransformUtilGenerate.EXTENSION_TRANSFORM_CLASS_INTERNAL_NAME});
        MethodVisitor defaultMethodVisitor = extensTransformImplClassWriter.visitMethod(ACC_PUBLIC,
                BeanTransformsMethodAdapter.INIT_METHOD_NAME,
                BeanTransformsMethodAdapter.INIT_METHOD_DESCRIPTOR,
                null,
                null);

        defaultMethodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        defaultMethodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, TransformUtilGenerate.OBJECT_CLASS_INTERNAL_NAME, BeanTransformsMethodAdapter.INIT_METHOD_NAME, BeanTransformsMethodAdapter.INIT_METHOD_DESCRIPTOR, false);
        defaultMethodVisitor.visitInsn(Opcodes.RETURN);
        defaultMethodVisitor.visitMaxs(1, 1);
        defaultMethodVisitor.visitEnd();
        // 实现复杂类型转换方法，基于源类和目标类生成对应转换字节码
        geneInstruction(extensTransformImplClassWriter, targetType, sourceBeanType, fieldNamePrefix);
        // 注意
        extensTransformImplClassWriter.visitEnd();

        byte[] classBytes = extensTransformImplClassWriter.toByteArray();
        Class extendClassImpl = TransformUtilGenerate.loadASMGenerateClass(classBytes, generateClassname);
        Constructor<?> constructor = extendClassImpl.getDeclaredConstructor();//默认构造方法；
        // 通过ClassWriter 生成的类已指定实现接口 com.shzz.common.tool.bean.transform.ExtensionObjectTransform，可强转
        ExtensionObjectTransform defaultExtensionObjectTransform = (ExtensionObjectTransform) constructor.newInstance();

        Map<String, ExtensionObjectTransform> innerExtensionObjectTransformMap = new HashMap<>(4);

        String geneConvertField = fieldNamePrefix + TransformUtilGenerate.EXTEND_IMPL_FIELD_NAME_SUFFIX;
        innerExtensionObjectTransformMap.put(geneConvertField,defaultExtensionObjectTransform);
        clearThreadLocal();
        return innerExtensionObjectTransformMap;

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
        return true;
    }
}
