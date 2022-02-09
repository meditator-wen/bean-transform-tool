package com.shzz.common.tool.bean.transform.asm;

import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.BeanTransform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Classname BeanTransFormsHandler
 * @Description TODO
 * @Date 2021/11/21 12:15
 * @Created by wen wang
 */
public abstract class BeanTransFormsHandler implements BeanTransform {

    /**
     * @description: 转换抽象类
     * 该方法采用泛型申明方式，考虑到泛型信息是方便编译器记录java 代码中类型，
     * 而该抽象类的实现类字节码文件非编译模式生成，通过ASM 写入字节码，在生成字节码的过程中所有需要类元信息转换的地方都通过字节码编码方式自行处理
     * 增加泛型申明并无太大意义。
     * 定义的接口函数beanTransform 使用泛型是为了在代码编译阶段完成类型检查。
     * @param sourceBeanClass   源类元信息 com.shzz.common.tool.bean.transform.asm.BeanTransFormsHandler
     * @param sourceBeanObject  源类对象
     * @param targetClass  目标类信息，转换过程中会生成该类对象，须确保有无参构造方法，生成类方法体字节码 通过 new 指令创建
     * @return: 返回目标类
     * @auther: wen wang
     * @date: 2021/11/30 9:58
     */
    public abstract Object beanTransforms(Class sourceBeanClass,Object sourceBeanObject, Class targetClass) throws Exception;


    @Override
    public  <S,T> T beanTransform(Class<S> sourceBeanClass,S sourceBeanObject, Class<T> targetClass) throws Exception{
        /**
         * @Description: 参见接口方法说明
         * @Author: wangwen
         * @Date: 2022/2/9 21:58
         * @param sourceBeanClass: 
         * @param sourceBeanObject: 
         * @param targetClass: 
         * @return: T
         **/
       return  (T) beanTransforms(sourceBeanClass,sourceBeanObject,targetClass);
    }

}

