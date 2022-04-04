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
package com.shzz.common.tool.bean.transform;

import com.shzz.common.tool.bean.transform.asm.BeanTransFormsHandler;


/**
 * 扩展对象变换
 *
 * @author wen wang
 * @date 2021/10/15 17:41
 */
public interface ExtensionObjectTransform extends Transform {

    /**
     * 扩展变换, 复杂类型类转换继承该接口。用户可以自定义该接口实现对象转换
     *
     * @param sourceObject 源对象
     * @param deepCopy     深拷贝
     * @return {@link Object}
     * @throws Exception 异常
     */
    public Object extensionObjectTransform(Object sourceObject, boolean deepCopy) throws Exception;

}
