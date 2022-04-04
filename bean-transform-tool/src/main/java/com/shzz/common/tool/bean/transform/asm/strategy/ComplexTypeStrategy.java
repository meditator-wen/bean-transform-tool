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
import com.shzz.common.tool.bean.transform.Transform;
import org.objectweb.asm.ClassWriter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;


/**
 * 复杂类型策略
 *
 * @author wen wang
 * @date 2021/12/8 13:47
 */
public interface ComplexTypeStrategy extends Serializable {


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
    public Map<String, ? extends Transform>  geneTransform(Type sourceBeanType, Type targetType, String generateClassname, String fieldNamePrefix) throws Exception;


    /**
     * 策略匹配判断，该方法由外部调用，判断对应的类型是否符合该策略类的要求
     * 这个接口函数是类型转换字节码生成模块与策略选择模块解耦的关键
     *
     * @param sourceBeanType 源bean类型
     * @param targetType     目标类型
     * @return boolean
     * @throws Exception 异常
     */
    public boolean strategyMatch(Type sourceBeanType, Type targetType) throws  Exception;

}
