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


    protected CustomeClassLoader(){

    }

    private static final Logger LOG = LoggerFactory.getLogger("CustomeClassLoader");

    private static final Map<String,byte[]> cacheClassByte=new ConcurrentHashMap<>();

    public static synchronized void clear(){
        cacheClassByte.clear();
    }

    public static synchronized void  putClassByte(String name,byte[] bytes){
        // 每次产生类字节码后需要调用该函数将类字节码加入缓存
        cacheClassByte.put(name,bytes);
    }


    public static synchronized  int staticSize(){
        Collection<byte[]> cacheBytes=cacheClassByte.values();
        int sum=0;
        for(byte[] bytes:cacheBytes){
            sum=sum+bytes.length;

        }
        return sum;

    }

    public static synchronized  boolean exceedThreshold(){
        int sizeThreshold=SystemProperties.getClassSizeThreshold();
        if(staticSize()>=sizeThreshold){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException{
        Class<?> cla= null;
        if(!exceedThreshold()){
            try {
                cla = super.loadClass(name);
            } catch (ClassNotFoundException e) {
                if(Objects.isNull(cla)){
                    if(cacheClassByte.containsKey(name)&&(cacheClassByte.get(name)!=null)&&(cacheClassByte.get(name).length>0)){
                        cla= super.defineClass(name, cacheClassByte.get(name), 0, cacheClassByte.get(name).length);
                    }else{
                        LOG.error("找不到类 {} 对应的字节码byte 数组，无法解析类",name);
                        throw new ClassNotFoundException("找不到类" +name+ "对应的字节码byte 数组，无法解析类");
                    }

                }
            }
        }else{

            LOG.error("通过 com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate 创建的类所占用内存空间 {} byte,超过阈值 {} byte", staticSize(), SystemProperties.THRESHOLD_SIZE);
            throw new ClassNotFoundException("通过TransformUtilGenerate 创建的类所占用总内存空间=" + staticSize() + "byte，超过阈值 " + SystemProperties.THRESHOLD_SIZE + "字节，新建类无法创建, 可修改系统配置 " + SystemProperties.CLASS_SIZE_THRESHOLD + " 值 (单位 byte)");
        }


        return cla;
    }

}
