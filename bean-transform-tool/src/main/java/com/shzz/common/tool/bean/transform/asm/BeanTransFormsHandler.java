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

import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.BeanTransform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 转换类接口
 *
 * @author wen wang
 * @date 2021/11/21 12:15
 */
public abstract class BeanTransFormsHandler implements BeanTransform {


    /**
     * bean转换
     *
     * @param sourceBeanClass  源bean类
     * @param sourceBeanObject 源bean对象
     * @param targetClass      目标类
     * @return {@link Object}
     * @throws Exception 异常
     */
    public abstract Object beanTransforms(Class sourceBeanClass,Object sourceBeanObject, Class targetClass) throws Exception;


    /**
     * bean转换
     *
     * @param sourceBeanClass  源bean类
     * @param sourceBeanObject 源bean对象
     * @param targetClass      目标类
     * @return {@link T}
     * @throws Exception 异常
     */
    @Override
    public  <S,T> T beanTransform(Class<S> sourceBeanClass, S sourceBeanObject, Class<T> targetClass) throws Exception{

        return  (T) beanTransforms(sourceBeanClass,sourceBeanObject,targetClass);
    }

}

