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
package com.shzz.common.tool.bean.transform.asm.context;

import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.strategy.ComplexTypeStrategy;

import java.lang.reflect.Type;
import java.util.Map;


/**
 * AbstractContext super interface
 *
 * @author wen wang
 * @date 2022/1/10 14:02
 */
public interface Context {

    /**
     * generate transform class
     *
     * @param sourceBeanType
     * @param targetType
     * @param fieldNamePrefix A field of a complex type in the owner class will generate the conversion class object of the field
     *                        and store it in a field of the conversion class corresponding to the owner class.
     *                        The name prefix of the field should be consistent with the field name of the complex type.
     * @return {@link Map}     key is field name of the conversion class,
     * value is Object of conversion class,
     * and The parent interface is {@link Transform}
     * @throws Exception
     */
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String fieldNamePrefix) throws Exception;

    /**
     * @return {@link String}
     */
    public String geneClassName();
}
