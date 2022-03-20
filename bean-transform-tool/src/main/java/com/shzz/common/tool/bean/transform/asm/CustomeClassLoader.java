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

import com.shzz.common.tool.bean.transform.SystemProperties;
import com.shzz.common.tool.code.BeanTransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


/**
 * custome类装入器
 *
 * @author wen wang
 * @date 2021/11/20 20:30
 */
public class CustomeClassLoader extends ClassLoader {


    /**
     * custome类装入器
     */
    protected CustomeClassLoader() {

    }

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger("CustomeClassLoader");


    /**
     * udf加载类
     *
     * @param name  名字
     * @param bytes 字节
     * @return {@link Class}
     * @throws ClassNotFoundException 类没有发现异常
     */
    public Class<?> udfLoadClass(String name, byte[] bytes) throws ClassNotFoundException {
        Class<?> cla = null;
        try {
            cla = super.loadClass(name);
        } catch (ClassNotFoundException e) {
            if (Objects.isNull(cla)) {
                cla = super.defineClass(name, bytes, 0, bytes.length);

            }
        }
        return cla;
    }


}
