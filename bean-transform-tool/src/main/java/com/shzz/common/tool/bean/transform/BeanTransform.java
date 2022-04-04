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


/**
 * bean转换接口类
 *
 * @author wen wang
 * @date 2022/1/14 9:51
 */
public interface BeanTransform extends Transform{

    /**
     * 转换类接口
     * 接口内泛型方法是为了在代码编译阶段完成类型检查
     * @param sourceBeanClass  源bean类
     * @param sourceBeanObject 源bean对象
     * @param targetClass      目标类
     * @return T      该接口子类对象
     * @throws Exception 异常
     */
    public  <S,T> T beanTransform(Class<S> sourceBeanClass, S sourceBeanObject, Class<T> targetClass) throws Exception;

}
