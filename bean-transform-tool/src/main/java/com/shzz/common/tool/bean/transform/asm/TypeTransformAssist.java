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
package com.shzz.common.tool.bean.transform.asm;

import com.shzz.common.tool.bean.BeanFieldInfo;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.SystemProperties;
import com.shzz.common.tool.code.BeanTransformException;
import com.shzz.common.tool.code.CommonCode;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;


/**
 * 类型转换辅助功能类
 *
 * @author wen wang
 * @date 2021/11/7 20:39
 */
public class TypeTransformAssist {

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger("ResloverMetaInfo");

    /**
     * 是原始类型
     *
     * @param type 类型
     * @return boolean
     */
    public static boolean isPrimitiveType(Class<?> type) {
        boolean flag = (type == int.class) || (type == long.class)
                || (type == double.class)
                || (type == float.class)
                || (type == boolean.class)
                || (type == short.class) || (type == char.class)
                || (type == String.class)
                || (type == byte.class);

        return flag;

    }

    /**
     * 引用类型
     *
     * @param type 类型
     * @return boolean
     */
    public static boolean referenceType(Class<?> type) {
        boolean flag = ((type == int.class)
                || (type == long.class)
                || (type == double.class)
                || (type == float.class)
                || (type == boolean.class)
                || (type == short.class)
                || (type == char.class)
                || (type == byte.class));
        return !flag;

    }

    /**
     * 是基本类型
     *
     * @param type 类型
     * @return boolean
     */
    public static boolean isBaseType(Class<?> type) {
        boolean flag = isWrapsType(type) || isPrimitiveType(type);
        return flag;

    }

    /**
     * istiny
     *
     * @param type 类型
     * @return boolean
     */
    public static boolean istiny(Class type) {
        return ((char.class == type) || (byte.class == type)
                || (short.class == type) || (int.class == type) || (boolean.class == type));
    }

    /**
     * 字符串经
     *
     * @param warpType 经类型
     * @param mv       mv
     * @return boolean
     * @throws Exception 异常
     */
    private static boolean stringToWarp(Class warpType, MethodVisitor mv) throws Exception {

        if (isWrapsOrStringType(warpType) && (warpType != String.class)) {
            String numberWrapTypeInternalName = org.objectweb.asm.Type.getInternalName(warpType);
            // 执行包装方法  valueOf 返回值入栈

            // valueOf(String s)   方法描述信息

            String methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(warpType), org.objectweb.asm.Type.getType(String.class));

            if (Character.class == warpType) {
                //Character 只有 vauleOf(char ch) 方法，先转成char,对应的方法描述符也要调整，
                // 不然会报错： Type integer (current frame, stack[0]) is not assignable to 'java/lang/String'
                methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(warpType), org.objectweb.asm.Type.getType(char.class));

                wrapsOrStringToPrimitive(char.class, String.class, mv);
            }
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    numberWrapTypeInternalName,
                    "valueOf",
                    methodDescriptor,
                    false);

            return true;
        } else {
            return false;
        }


    }

    /**
     * 字符串数
     *
     * @param numberType 数字类型
     * @param mv         mv
     * @return boolean
     * @throws Exception 异常
     */
    private static boolean stringToNumber(Class numberType, MethodVisitor mv) throws Exception {
        if (isPrimitiveType(numberType)) {
            //先转换为对应的包装类型,调用 包装类静态方法 valueOf(String s) 转换。
            Class numberWrapType = typeMap(numberType);

            stringToWarp(numberWrapType, mv);
            // 转换成
            wrapsOrStringToPrimitive(numberType, numberWrapType, mv);

            return true;

        } else {
            return false;
        }
    }


    /**
     * 原始,原始
     *
     * @param targetClass 目标类
     * @param sourceClass 源类
     * @param mv          mv
     * @throws Exception 异常
     */
    protected static void primitiveToPrimitive(Class targetClass, Class sourceClass, MethodVisitor mv) throws Exception {
        // 处理原始类型之间强转 ,注意，调用该方法前源类对象的字段值要求已经入栈
        if ((!isPrimitiveType(sourceClass)) || (!isPrimitiveType(targetClass))) {

            throw new BeanTransformException(CommonCode.TYPE_MISMATCH.getErrorCode(), CommonCode.TYPE_MISMATCH.getErrorOutline(), "primitiveToPrimitive 方法只处理原始类型之间的相互转换，实际类型 " + targetClass.getSimpleName() + "," + sourceClass.getSimpleName());
        }
        if (targetClass == sourceClass) {
            // 不执行任何转换指令，上层自行处理
            return;
        }

        if (istiny(targetClass)) {
            //整型以下先转int,long  double float 到char  byte short 需要经过两步指令
            if (sourceClass == long.class) {
                mv.visitInsn(Opcodes.L2I);
            } else if (sourceClass == float.class) {
                mv.visitInsn(Opcodes.F2I);
            } else if (sourceClass == double.class) {
                mv.visitInsn(Opcodes.D2I);
            } else if ((sourceClass == String.class) && (targetClass != char.class)) {

                if (!stringToNumber(int.class, mv)) {
                    // String到 数值型类型无法转换，设置默认值
                    // 把已经入栈的String 变量出栈，重新入栈0常量 int
                    mv.visitInsn(Opcodes.POP);
                    mv.visitInsn(Opcodes.ICONST_0);
                }


            } else if ((sourceClass == String.class) && (targetClass == char.class)) {
                //调用 方法 public char charAt(int index)

                mv.visitInsn(Opcodes.ICONST_0);
                // 执行转换方法调用指令

                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(String.class), "charAt",
                        Type.getMethodDescriptor(Type.getType(char.class), Type.getType(int.class)),
                        false);


            }

            /**
             *
             *   boolean 类型变量在 虚拟机中以int 型表示，无需转换
             *   char byte short 需要在 上一步基础上继续转换
             */
            if (sourceClass != String.class) {
                if ((char.class == targetClass)) {
                    mv.visitInsn(Opcodes.I2C);
                } else if (byte.class == targetClass) {
                    mv.visitInsn(Opcodes.I2B);
                } else if (short.class == targetClass) {
                    mv.visitInsn(Opcodes.I2S);
                }
            }


        } else if (targetClass == float.class) {
            if (sourceClass == long.class) {
                mv.visitInsn(Opcodes.L2F);
            } else if (sourceClass == double.class) {
                mv.visitInsn(Opcodes.D2F);
            } else if (istiny(sourceClass)) {
                /**
                 * byte short char boolean int 变量存储时，虚拟机内部会以istore 指令存入变量,
                 * 返回值如果是byte short char，以ireturn 指令返回
                 * 所以，源类型如果返回的字段是以上类型均无需先转为int ,直接以i2f 转换后目标类 float 变量
                 */
                mv.visitInsn(Opcodes.I2F);
            } else if (String.class == targetClass) {
                if (!stringToNumber(float.class, mv)) {
                    // String到 数值型类型无法转换，设置默认值
                    //把已经入栈的String 变量出栈，重新入栈0 常量 float

                    mv.visitInsn(Opcodes.POP); //把已经入栈的String 变量出栈，重新入栈0 float常量
                    mv.visitInsn(Opcodes.FCONST_0);
                }

            }


        } else if (targetClass == long.class) {
            if (sourceClass == float.class) {
                mv.visitInsn(Opcodes.F2L);
            } else if (sourceClass == double.class) {
                mv.visitInsn(Opcodes.D2L);
            } else if (istiny(sourceClass)) {
                /**
                 * byte short char boolean int 变量存储时，虚拟机内部会以istore 指令存入变量,
                 * 返回值如果是byte short char，以ireturn 指令返回
                 * 所以，源类型如果返回的字段是以上类型均无需先转为int ,直接以i2l 转换后目标类 long型字段
                 */
                mv.visitInsn(Opcodes.I2L);
            } else if (String.class == sourceClass) {

                if (!stringToNumber(long.class, mv)) {
                    // String到 数值型类型无法转换，设置默认值
                    //把已经入栈的String 变量出栈，重新入栈0 常量 long
                    mv.visitInsn(Opcodes.POP);
                    mv.visitInsn(Opcodes.LCONST_0);
                }

            }
        } else if (targetClass == double.class) {
            if (sourceClass == float.class) {
                mv.visitInsn(Opcodes.F2D);
            } else if (sourceClass == long.class) {
                mv.visitInsn(Opcodes.L2D);
            } else if (istiny(sourceClass)) {
                /**
                 * byte short char boolean int 变量存储时，虚拟机内部会以istore 指令存入变量,
                 * 返回值如果是byte short char，以ireturn 指令返回
                 * 所以，源类型如果返回的字段是以上类型均无需先转为int ,直接以i2d 转换后目标类 double型字段
                 */
                mv.visitInsn(Opcodes.I2D);
            } else if (String.class == sourceClass) {

                if (!stringToNumber(double.class, mv)) {
                    // String到 数值型类型无法转换，设置默认值
                    mv.visitInsn(Opcodes.POP); //把已经入栈的String 变量出栈，重新入栈0 常量 double
                    mv.visitInsn(Opcodes.DCONST_0);
                }

            }
        } else if (targetClass == String.class) {
            String targetClassInternalName = org.objectweb.asm.Type.getInternalName(targetClass);
            org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getType(targetClass);
            String methodDescriptor = "";
            if ((sourceClass == byte.class) || (sourceClass == short.class)) {
                //String.valueOf  参数无 byte 以及short  类型，这种统一调用 valueOf(int i)
                // 方法描述符如下
                methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(returnType, org.objectweb.asm.Type.getType(int.class));
            } else {
                // 其他数值类型按照各自类型本身作为方法参数类型，方法描述符如下
                methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(returnType, org.objectweb.asm.Type.getType(sourceClass));
            }
            // 执行转换方法调用指令

            mv.visitMethodInsn(Opcodes.INVOKESTATIC, targetClassInternalName, "valueOf", methodDescriptor, false);

        }

    }

    /**
     * 类型图
     *
     * @param key 关键
     * @return {@link Class}
     */
    public static Class typeMap(Class key) {
        if (key == byte.class) {
            return Byte.class;
        } else if (key == Byte.class) {
            return byte.class;
        } else if (key == short.class) {
            return Short.class;
        } else if (key == Short.class) {
            return short.class;
        } else if (key == int.class) {
            return Integer.class;
        } else if (key == Integer.class) {
            return int.class;
        } else if (key == long.class) {
            return Long.class;
        } else if (key == Long.class) {
            return long.class;
        } else if (key == float.class) {
            return Float.class;
        } else if (key == Float.class) {
            return float.class;
        } else if (key == Double.class) {
            return double.class;
        } else if (key == double.class) {
            return Double.class;
        } else if (key == String.class) {
            return String.class;
        } else if (key == char.class) {
            return Character.class;
        } else if (key == Character.class) {
            return char.class;
        }
        return null;

    }

    /**
     * 原始包装或字符串
     *
     * @param targetClass 目标类
     * @param sourceClass 源类
     * @param mv          mv
     * @throws Exception 异常
     */
    protected static void wrapsOrStringToPrimitive(Class targetClass, Class sourceClass, MethodVisitor mv) throws Exception {
        /**
         * @description: 本函数只处理 包装类或者String 类型到基础类型的转换
         * 注意，调用该方法前源类对象的字段值要求已经入栈,且是包装类或者String类型
         * @param targetClass
         * @param sourceClass
         * @param mv
         * @return: void
         * @auther: wen wang
         * @date: 2021/11/27 19:30
         */

        if ((!isPrimitiveType(targetClass)) || (!isWrapsOrStringType(sourceClass))) {
            // todo
            throw new BeanTransformException(CommonCode.TYPE_MISMATCH.getErrorCode(), CommonCode.TYPE_MISMATCH.getErrorOutline(), "wrapsOrStringToPrimitive 方法只处理包装类或者String类到原始类型的转换，实际类型 " + targetClass.getSimpleName() + "," + sourceClass.getSimpleName());
        }
        if (targetClass == sourceClass) {
            // 不执行任何转换指令,只有String  to String 一种情况，其他的两类型不会相同
            return;
        }

        if (String.class != sourceClass) {

            if (String.class == targetClass) {
                // 直接调用包装类的toString 方法
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        org.objectweb.asm.Type.getInternalName(sourceClass),
                        "toString",
                        org.objectweb.asm.Type.getMethodDescriptor(
                                org.objectweb.asm.Type.getType(String.class)
                        ),
                        false);
            } else {

                TypeValueMethod typeValueMethod = TypeValueMethod.getTypeValueMethod(targetClass);
                // 先执行源类包装类字段拆箱方法转换成基础类型的值

                if (targetClass != char.class) {
                    if ((sourceClass != Character.class)) {
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                org.objectweb.asm.Type.getInternalName(sourceClass),
                                typeValueMethod.getMethodName(),
                                typeValueMethod.getMethodDescriptor(),
                                false);
                    } else {
                        wrapsOrStringToPrimitive(char.class, Character.class, mv);
                        primitiveToPrimitive(targetClass, char.class, mv);
                    }
                } else {
                    if ((sourceClass == Character.class)) {
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                org.objectweb.asm.Type.getInternalName(sourceClass),
                                typeValueMethod.getMethodName(),
                                typeValueMethod.getMethodDescriptor(),
                                false);
                    } else {
                        wrapsOrStringToPrimitive(typeMap(sourceClass), sourceClass, mv);
                        primitiveToPrimitive(targetClass, typeMap(sourceClass), mv);
                    }
                }


            }

        } else {
            // 转换成目标类字段类型值，执行基础类型转换,注意，执行的是拆箱类型（typeMap(sourceClass)）到目标类型的转换

            primitiveToPrimitive(targetClass, typeMap(sourceClass), mv);
        }


    }

    /**
     * 原始包装或字符串
     *
     * @param targetClass 目标类
     * @param sourceClass 源类
     * @param mv          mv
     * @throws Exception 异常
     */
    protected static void primitiveToWrapsOrString(Class targetClass, Class sourceClass, MethodVisitor mv) throws Exception {
        /**
         * @description: 本函数处理原始类型到包装类转换 ,注意，调用该方法前源类对象的字段值要求已经入栈,且是原始类型
         * @param targetClass
         * @param sourceClass
         * @param mv
         * @return: void
         * @auther: wen wang
         * @date: 2021/11/27 11:05
         */
        if ((!isPrimitiveType(sourceClass)) || (!isWrapsOrStringType(targetClass))) {
            // todo
            throw new BeanTransformException(CommonCode.TYPE_MISMATCH.getErrorCode(), CommonCode.TYPE_MISMATCH.getErrorOutline(), "primitiveToWrapsOrString 方法只处理原始类型到包装类或者String类之间的相互转换，实际类型 " + targetClass.getSimpleName() + "," + sourceClass.getSimpleName());
        }
        if (targetClass == sourceClass) {
            // 不执行任何转换指令
            return;
        }


        String targetClassInternalName = org.objectweb.asm.Type.getInternalName(targetClass);
        // 先转成包装类或者String 类型对应的原始类型（note String  只对应String，其他包装类见以下方法参数和返回值对应关系）
        primitiveToPrimitive(typeMap(targetClass), sourceClass, mv);
        /**
         * 包装类转换的方法有以下几种
         *  public static Byte valueOf(byte b)
         *  public static Short valueOf(short b)
         *  public static Integer valueOf(int b)
         *  public static Long valueOf(long b)
         *  public static Float valueOf(float b)
         *  public static Double valueOf(double b)
         *  public static Boolean valueOf(boolean b)
         *  public static Double valueOf(double b)
         *  public static Character valueOf(char ch)
         *
         */
        // 如果是String 类型，在上一步 primitiveToPrimitive(typeMap(targetClass), sourceClass, mv); 已做转换
        if (String.class != targetClass) {
            String methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(targetClass), org.objectweb.asm.Type.getType(typeMap(targetClass)));

            // 执行包装方法  valueOf 返回值入栈
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    targetClassInternalName,
                    "valueOf",
                    methodDescriptor,
                    false);
        }


    }

    /**
     * 包装包装或字符串或字符串
     *
     * @param targetClass 目标类
     * @param sourceClass 源类
     * @param mv          mv
     * @throws Exception 异常
     */
    protected static void wrapsOrStringToWrapsOrString(Class targetClass, Class sourceClass, MethodVisitor mv) throws Exception {
        /**
         * @description: 该方法只处理包装类或者String 类之间的相互转换
         * @param targetClass
         * @param sourceClass
         * @param mv
         * @return: void
         * @auther: wen wang
         * @date: 2021/11/28 0:05
         */
        if ((!isWrapsOrStringType(sourceClass)) || (!isWrapsOrStringType(targetClass))) {
            // todo
            throw new BeanTransformException(CommonCode.TYPE_UNSUPPORT.getErrorCode(), CommonCode.TYPE_UNSUPPORT.getErrorOutline(), "wrapsOrStringToWrapsOrString 方法只处理包装类或者String类之间的相互转换，实际类型 " + targetClass.getSimpleName() + "," + sourceClass.getSimpleName());

        }
        if ((targetClass == sourceClass) && (!SystemProperties.getWrapsTypeDeepyCopyFlag())) {

            return;
        }

        if ((sourceClass == String.class) && isWrapsOrStringType(targetClass)) {

            stringToWarp(targetClass, mv);

        } else {
            // 先转为原始类型
            wrapsOrStringToPrimitive(typeMap(targetClass), sourceClass, mv);
            // 再转换为包装类或者String 类型

            primitiveToWrapsOrString(targetClass, typeMap(targetClass), mv);
        }


    }

    /**
     * 基类型处理字节码
     *
     * @param targetClass 目标类
     * @param sourceClass 源类
     * @param mv          mv
     * @param isDeepyCopy deepy复制
     * @throws Exception 异常
     */
    public static void baseTypeProcessByteCode(Class targetClass, Class sourceClass, MethodVisitor mv, boolean isDeepyCopy) throws Exception {

        /**
         * @description: 该方法服务于 {@link BeanTransformsMethodAdapter}
         * 只在源类和目标类都是原始类型或者包装类型情况下调用
         * @param targetClass
         * @param sourceClass
         * @param mv
         * @param isDeepyCopy
         * @return: void
         * @auther: wen wang
         * @date: 2021/11/25 21:31
         */

        if (isBaseType(targetClass) && isBaseType(sourceClass)) {

            if ((targetClass == sourceClass) && (!isDeepyCopy)) {
                // 调用者上层自行处理二者转换的字节码,直接赋值转换
                return;
            } else {
                if (isPrimitiveType(targetClass) && isPrimitiveType(sourceClass)) {
                    primitiveToPrimitive(targetClass, sourceClass, mv);
                } else if (isPrimitiveType(targetClass) && isWrapsOrStringType(sourceClass)) {
                    wrapsOrStringToPrimitive(targetClass, sourceClass, mv);
                } else if (isWrapsOrStringType(targetClass) && isWrapsOrStringType(sourceClass)) {
                    wrapsOrStringToWrapsOrString(targetClass, sourceClass, mv);
                } else if (isWrapsOrStringType(targetClass) && isPrimitiveType(sourceClass)) {
                    primitiveToWrapsOrString(targetClass, sourceClass, mv);
                }

            }

        } else {
            LOG.warn("源类或目标类非基础类型或者包装类型");
            return;
        }


    }

    public static enum TypeValueMethod {

        CHARVALUE_METHOD("charValue", char.class),
        BYTEVALUE_METHOD("byteValue", byte.class),
        SHORTVALUE_METHOD("shortValue", short.class),
        INTVALUE_METHOD("intValue", int.class),
        LONGVALUE_METHOD("longValue", long.class),
        FLOATVALUE_METHOD("floatValue", float.class),
        DOUBLEVALUE_METHOD("doubleValue", double.class);

        private String methodName;

        private String methodDescriptor;


        private TypeValueMethod(String methodName, Class<?> returnPrimitiveType) {


            this.methodName = methodName;
            this.methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(
                    org.objectweb.asm.Type.getType(returnPrimitiveType));
        }

        public String getMethodName() {
            return methodName;
        }


        public String getMethodDescriptor() {
            return methodDescriptor;
        }


        public static TypeValueMethod getTypeValueMethod(Class<?> returnPrimitiveType) {
            if (byte.class == returnPrimitiveType) {
                return BYTEVALUE_METHOD;
            } else if (short.class == returnPrimitiveType) {
                return SHORTVALUE_METHOD;
            } else if (int.class == returnPrimitiveType) {
                return INTVALUE_METHOD;
            } else if (long.class == returnPrimitiveType) {
                return LONGVALUE_METHOD;
            } else if (float.class == returnPrimitiveType) {
                return FLOATVALUE_METHOD;
            } else if (double.class == returnPrimitiveType) {
                return DOUBLEVALUE_METHOD;
            } else if (char.class == returnPrimitiveType) {
                return CHARVALUE_METHOD;
            } else {
                return null;
            }
        }
    }


    /**
     * 包装还是字符串类型
     *
     * @param type 类型
     * @return boolean
     */
    public static boolean isWrapsOrStringType(Class<?> type) {
        boolean flag = isWrapsType(type) || (type.isAssignableFrom(String.class));
        return flag;

    }

    /**
     * 包装类型
     *
     * @param type 类型
     * @return boolean
     */
    public static boolean isWrapsType(Class<?> type) {
        boolean flag = (Integer.class == type) || (Long.class == type)
                || (Double.class == type)
                || (Float.class == type)
                || (Boolean.class == type)
                || (Short.class == type)
                || (Character.class == type)
                || (Byte.class == type);

        return flag;

    }


    /**
     * 源领域过程
     *
     * @param targetFieldName 目标字段名称
     * @param sourceFieldName 源字段名
     * @param sourceClass     源类
     * @param targetClass     目标类
     * @param resloveInfo     解决信息
     */
    public static void sourceFieldProcess(String targetFieldName,
                                          String sourceFieldName,
                                          Class<?> sourceClass,
                                          Class<?> targetClass,
                                          ResloveInfo resloveInfo) {
        /**
         * @description: 解析目标类字段对应的源类字段信息
         * @param targetFieldName
         * @param sourceFieldName
         * @param sourceClass
         * @param targetClass
         * @param resloveInfo
         * @return: void
         * @auther: wen wang
         * @date: 2021/11/30 9:56
         */

        if (Objects.isNull(sourceFieldName) || sourceFieldName.isEmpty()) {

            //注解中标注的源类字段名为空，则直接采用目标类对应的字段名
            resloveInfo.setSourceFieldName(targetFieldName);
        } else {
            resloveInfo.setSourceFieldName(sourceFieldName);
        }
        String sourceFiledGetFunctionName = ""; //源类中该字段对应的get 方法名称


        try {

            Field sourceField = sourceClass.getDeclaredField(resloveInfo.getSourceFieldName());

            resloveInfo.setSourceFieldType(sourceField.getType());
            resloveInfo.setSourceField(sourceField);

            /**
             * 源类字段 BeanFieldInfo 注解
             */
            BeanFieldInfo sourceBeanFieldInfo = sourceField.getAnnotation(BeanFieldInfo.class);

            if (Objects.isNull(sourceBeanFieldInfo)) {
                /**
                 * 如果源类中该字段无 {@link BeanFieldInfo },
                 */
                sourceFiledGetFunctionName = constructGetAndSetFunctionName(sourceField, true);

            } else {

                // 自定义转换类解析
                String extensionObjectTransformImplClass = sourceBeanFieldInfo.extensionObjectTransformImplClass();
                if (!Objects.isNull(extensionObjectTransformImplClass)) {
                    resloveInfo.setExtensionObjectTransformImplClass(extensionObjectTransformImplClass);
                }

                if (Objects.isNull(sourceBeanFieldInfo.getFunctionName()) || sourceBeanFieldInfo.getFunctionName().isEmpty()) {

                    /**
                     * 说明 注解 BeanFieldInfo 属性值 String setFunctionName() ; 未定义，通过默认构建方法创建get 方法
                     */
                    sourceFiledGetFunctionName = constructGetAndSetFunctionName(sourceField, true);
                } else {
                    sourceFiledGetFunctionName = sourceBeanFieldInfo.getFunctionName(); // 从注解中获取字段的get 方法名
                }

            }

            resloveInfo.setSourceFieldGetFunctionName(sourceFiledGetFunctionName);

            try {
                // 方法有效性判断
                Method sourceFieldGetMethod = sourceClass.getDeclaredMethod(sourceFiledGetFunctionName);
                //获取方法描述符
                String sourceFieldGetMethodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(sourceFieldGetMethod);
                String sourceFieldTypeInternalName = org.objectweb.asm.Type.getInternalName(sourceFieldGetMethod.getReturnType());
                resloveInfo.setSourceFieldTypeInternalName(sourceFieldTypeInternalName);
                resloveInfo.setSourceFieldGetFunctionDescriptor(sourceFieldGetMethodDescriptor);

                if (Modifier.isPublic(sourceFieldGetMethod.getModifiers())) {
                    // 源类中对应字段的get 方法必须是public 关键字修饰的方法
                    resloveInfo.setSourceFieldGetFunctionNameAvailable(true);
                }
            } catch (NoSuchMethodException e) {
                LOG.warn("类{}中字段{} {} 对应的get 方法 {}不存在", sourceClass.getSimpleName(), sourceField.getType().getSimpleName(), resloveInfo.getSourceFieldName(), sourceFiledGetFunctionName);
                LOG.error(e.toString());
            }


        } catch (NoSuchFieldException e) {
            LOG.warn("类{}中字段{} {} 对应的源类字段 {} 不存在", targetClass.getSimpleName(),
                    targetClass.getTypeName(),
                    targetFieldName,
                    resloveInfo.getSourceFieldName());
        }


    }


    /**
     * 检查类
     *
     * @param targetClass     目标类
     * @param sourceBeanClass 源bean类
     * @throws BeanTransformException bean转换异常
     */
    public static void checkClass(Class targetClass, Class sourceBeanClass) throws BeanTransformException {

        /**
         * 只针对顶层目标类或者顶层源类检查，如果是字段类型是以下几种情况依然会做相应转换
         * 顶层类不是以下几种情况的可进入转换代码，但必须要有字段 get  set 方法，否则不做任何处理返回空对象
         *
         */
        if (Map.class.isAssignableFrom(targetClass)
                || Collection.class.isAssignableFrom(targetClass)
                || (targetClass == Object.class)) {


            throw new BeanTransformException(CommonCode.TYPE_UNSUPPORT.getErrorCode(), CommonCode.TYPE_UNSUPPORT.getErrorOutline(),
                    "顶层目标类（即，字段的owner 类）是数组类，Map、 Collection 子类、Object类,不予处理，如果是内层字段类型" +
                            "请继承ExtensionObjectTransform 自定义实现，具体操作请参见 beanTransforms 方法说明 ");


        }

        if (Map.class.isAssignableFrom(sourceBeanClass)
                || Collection.class.isAssignableFrom(sourceBeanClass)
                || (targetClass == Object.class)) {

            throw new BeanTransformException(CommonCode.TYPE_UNSUPPORT.getErrorCode(), CommonCode.TYPE_UNSUPPORT.getErrorOutline(),
                    "顶层源类（即，字段的owner 类）是数组类，Map、 Collection 子类、Object类,不予处理，如果是内层字段类型" +
                            "请继承ExtensionObjectTransform 自定义实现，具体操作请参见 beanTransforms 方法说明 ");


        }
    }

    /**
     * 解决信息检查
     *
     * @param resloveInfo 解决信息
     * @return boolean
     */
    public static boolean resloveInfoCheck(ResloveInfo resloveInfo) {
        boolean check = true;
        if (Objects.isNull(resloveInfo)) {

            LOG.error("resloveInfo is null ");
            return false;
        }
        check = check && resloveInfo.isSourceFieldGetFunctionNameAvailable() && resloveInfo.isTargetFieldSetFunctionAvailable();

        if (Objects.isNull(resloveInfo.getSourceFieldName()) || Objects.isNull(resloveInfo.getTargetFieldName())) {
            check = false;
            LOG.error("sourceFieldName is null or targetFieldName is null");
        }


        if (Objects.isNull(resloveInfo.getSourceField()) || Objects.isNull(resloveInfo.getSourceFieldType())) {
            check = false;
            LOG.error("{} Source Field is null or Source Field type is null", resloveInfo.getSourceFieldName());
        }

        if (Objects.isNull(resloveInfo.getTargetFieldSetFunctionDescriptor())) {
            check = false;
            LOG.error("{} TargetField  SetFunction Descriptor is null ", resloveInfo.getTargetFieldName());
        }

        if (Objects.isNull(resloveInfo.getSourceFieldGetFunctionDescriptor())) {
            check = false;
            LOG.error("{} SourceField  getFunction Descriptor is null ", resloveInfo.getSourceFieldName());
        }

        return check;
    }

    /**
     * 解决
     *
     * @param field       场
     * @param targetClass 目标类
     * @param sourceClass 源类
     * @return {@link ResloveInfo}
     */
    public static ResloveInfo reslove(Field field, Class<?> targetClass, Class<?> sourceClass) {

        ResloveInfo resloveInfo = new ResloveInfo();
        BeanFieldInfo beanFieldInfo = field.getAnnotation(BeanFieldInfo.class);
        String targetFieldName = field.getName();
        resloveInfo.setTargetFieldName(targetFieldName);

        if (!Objects.isNull(beanFieldInfo)) {

            // 自定义转换类解析
            String extensionObjectTransformImplClass = beanFieldInfo.extensionObjectTransformImplClass();
            boolean userExtend = beanFieldInfo.userExtend();
            resloveInfo.setUserExtend(userExtend);
            boolean autoTransform = beanFieldInfo.autoTransform();
            resloveInfo.setAutoTransform(autoTransform);
            if (userExtend && (!Objects.isNull(extensionObjectTransformImplClass))
                    && (!extensionObjectTransformImplClass.isEmpty())) {
                resloveInfo.setExtensionObjectTransformImplClass(extensionObjectTransformImplClass);
            }

            /**
             * 对应字段的set 方法名，如果注解{@link BeanFieldInfo}中有则使用，无则通过constructGetAndSetFunctionName方法 构建，构建法则参见方法说明
             */
            String setFunctionName = "";
            if (Objects.isNull(beanFieldInfo.setFunctionName()) || beanFieldInfo.setFunctionName().isEmpty()) {
                /**
                 * 说明 注解 BeanFieldInfo 属性值 String setFunctionName() ; 未定义，通过默认构建方法创建set 方法
                 */
                setFunctionName = constructGetAndSetFunctionName(field, false);
            } else {
                setFunctionName = beanFieldInfo.setFunctionName();
            }

            resloveInfo.setTargetFieldSetFunctionName(setFunctionName);
            //检查该字段是否有有效的get 方法

            try {
                Method targetFieldSetMethod = targetClass.getDeclaredMethod(setFunctionName, field.getType());

                //获取方法描述符
                String targetFieldSetMethodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(targetFieldSetMethod);
                resloveInfo.setTargetFieldSetFunctionDescriptor(targetFieldSetMethodDescriptor);
                if (Modifier.isPublic(targetFieldSetMethod.getModifiers())) {
                    // 目标类中对应字段的set 方法必须是public 关键字修饰的方法
                    resloveInfo.setTargetFieldSetFunctionAvailable(true);
                }
            } catch (NoSuchMethodException e) {

                LOG.warn("类{}中字段{} {} 对应的set 方法 {}不存在，忽略该字段", targetClass.getSimpleName(), field.getGenericType().getTypeName(), field.getName(), setFunctionName);
                LOG.error(e.toString());
                // 如果本类字段无set 方法，则源类对应字段信息无需解析。后续遍历转换该字段时忽略
                return null;
            }


            String sourceFiledName = beanFieldInfo.sourceFieldName();

            // 处理目标字段对应的源类字段信息
            sourceFieldProcess(targetFieldName,
                    sourceFiledName,
                    sourceClass,
                    targetClass,
                    resloveInfo);


        } else {
            // 注未标注字段名称映射注解，默认表示本字段名和源对象中字段名一致  todo
            String sourceFiledName = targetFieldName;

            String targetSetFunctionName = constructGetAndSetFunctionName(field, false);
            resloveInfo.setTargetFieldSetFunctionName(targetSetFunctionName);

            try {
                Method targetFieldSetMethod = targetClass.getDeclaredMethod(targetSetFunctionName, field.getType());
                //获取方法描述符
                String targetFieldSetMethodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(targetFieldSetMethod);
                resloveInfo.setTargetFieldSetFunctionDescriptor(targetFieldSetMethodDescriptor);
                if (Modifier.isPublic(targetFieldSetMethod.getModifiers())) {
                    // 目标类中对应字段的set 方法必须是public 关键字修饰的方法
                    resloveInfo.setTargetFieldSetFunctionAvailable(true);
                }
            } catch (NoSuchMethodException e) {
                LOG.warn("类{}中字段{} {} 对应的set 方法 {}不存在", targetClass.getSimpleName(), field.getType().getSimpleName(), targetFieldName, targetSetFunctionName);
                LOG.error(e.toString());
            }

            sourceFieldProcess(targetFieldName,
                    sourceFiledName,
                    sourceClass,
                    targetClass,
                    resloveInfo);

        }


        return resloveInfo;

    }

    /**
     * 构建获取和设置函数名
     *
     * @param field   场
     * @param getFlag 让国旗
     * @return {@link String}
     */
    public static String constructGetAndSetFunctionName(Field field, boolean getFlag) {
        String name = "";
        if (Objects.isNull(field)) {
            LOG.error("解析字段get set 方法异常，传入字段field 为null");
            return name;
        }


        if (getFlag) {
            if (boolean.class.isAssignableFrom(field.getType())) {
                // 基础类型bolean 布尔默认字段默认获取方法是is前缀,(保持和 idea 默认的生成规则一致)，包装类Boolean 前缀依然是get
                name = "is" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            } else {
                name = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            }

        } else {
            name = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        }
        return name;

    }

    /**
     * 检查扩展对象变换impl
     *
     * @param extensionObjectTransformImpl 扩展对象变换impl
     * @param targetClass                  目标类
     * @param extensionImplFieldName       扩展impl字段名
     * @return boolean
     */
    public static boolean checkExtensionObjectTransformImpl(String extensionObjectTransformImpl, Class<?> targetClass, String extensionImplFieldName) {
        boolean checkSuccess = false; //默认无效
        if (Objects.isNull(extensionObjectTransformImpl) || extensionObjectTransformImpl.isEmpty()) {
            // 需要用户自定义接口实现转换的字段，如果注解中指定的转换类名称为空，则无法在代码中new 实现类对象
            // 直接null 入栈进入下一轮递归
            LOG.warn("{} {} 字段转换设置的拓展类 extensionObjectTransformImpl 为空，无法转换该字段", targetClass.getSimpleName(), extensionImplFieldName);
        } else {
            // 如果类名不为空，先检查类是否有效
            try {
                Class extensionObjectTransformImplClass = Class.forName(extensionObjectTransformImpl);

                if (ExtensionObjectTransform.class.isAssignableFrom(extensionObjectTransformImplClass)) {

                    try {
                        Constructor extensionObjectTransformImplConstruct = extensionObjectTransformImplClass.getDeclaredConstructor();
                        if (Modifier.isPublic(extensionObjectTransformImplConstruct.getModifiers())) {
                            checkSuccess = true;
                        }

                    } catch (NoSuchMethodException e) {
                        LOG.warn("拓展类 extensionObjectTransformImpl={} 没有无参构造方法，不执行对应字段的转换", extensionObjectTransformImpl);
                    }
                }
            } catch (ClassNotFoundException e) {
                LOG.warn("拓展类 extensionObjectTransformImpl={} 无法加载Class 文件 无效，不执行对应字段的转换", extensionObjectTransformImpl);

            }
        }

        return checkSuccess;
    }
}

