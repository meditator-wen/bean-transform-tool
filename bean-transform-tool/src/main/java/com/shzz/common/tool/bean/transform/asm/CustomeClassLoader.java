package com.shzz.common.tool.bean.transform.asm;

import com.shzz.common.tool.bean.transform.SystemProperties;
import com.shzz.common.tool.code.BeanTransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @Classname CustomeClassLoader
 * @Description TODO
 * @Date 2021/11/20 20:30
 * @Created by wen wang
 */
public class CustomeClassLoader extends ClassLoader {


    protected CustomeClassLoader() {

    }

    private static final Logger LOG = LoggerFactory.getLogger("CustomeClassLoader");


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
