package com.shzz.common.tool.bean.transform.asm.context;

import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.TypeTransformAssist;
import com.shzz.common.tool.bean.transform.asm.strategy.*;
import com.shzz.common.tool.bean.transform.asm.strategy.ArrayTypeStrategy;
import com.shzz.common.tool.bean.transform.asm.strategy.MapTypeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate.EXTEND_TRANSFORM_IMPL_CLASS_NAME_PREFIX;

/**
 * @Classname AbstractTypeState
 * @Description TODO
 * @Date 2022/1/3 23:03
 * @Created by wen wang
 */
public class TransformTypeContext extends AbstractContext {

    private static final Logger LOG = LoggerFactory.getLogger("TransformUtilGenerate");


    public static Map<String, Class<? extends ComplexTypeStrategy>> typeStrategyHashMap = new ConcurrentHashMap<>();

    public TransformTypeContext(Field sourceField, Field targetField, Class ownerClass) {
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.ownerClass = ownerClass;
        this.identify = sourceField.getName();
    }

    public TransformTypeContext(TransformTypeContext otherContext) {
        this.sourceField = otherContext.getSourceField();
        this.targetField = otherContext.getTargetField();
        this.ownerClass = otherContext.ownerClass;
        this.identify = sourceField.getName();
    }

    static {
        //默认策略缓存。用户拓展新增的策略通过增加register 函数添加
        typeStrategyHashMap.put("com.shzz.common.tool.bean.transform.asm.strategy.UniversalClassTypeStrategy", UniversalClassTypeStrategy.class);
        typeStrategyHashMap.put("com.shzz.common.tool.bean.transform.asm.strategy.ArrayTypeStrategy", ArrayTypeStrategy.class);
        typeStrategyHashMap.put("com.shzz.common.tool.bean.transform.asm.strategy.CollectionTypeStrategy", CollectionTypeStrategy.class);
        typeStrategyHashMap.put("com.shzz.common.tool.bean.transform.asm.strategy.MapTypeStrategy", MapTypeStrategy.class);

    }


    @Override
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String fieldNamePrefix) throws Exception {
        //, TransformUtilGenerate transformUtilGenerate

        Collection<Class<? extends ComplexTypeStrategy>> strategyImplClasses = typeStrategyHashMap.values();
        Type fieldGenericType = targetField.getGenericType();
        boolean flagTarget = (fieldGenericType instanceof GenericArrayType) ||
                (fieldGenericType instanceof TypeVariable) ||
                (fieldGenericType instanceof WildcardType) ||
                (fieldGenericType instanceof ParameterizedType) ||
                ((fieldGenericType instanceof Class) && (((Class) fieldGenericType).isArray()));


        if (!flagTarget) {
            return null;
        }

        for (Class<? extends ComplexTypeStrategy> temp : strategyImplClasses) {
            Constructor<? extends ComplexTypeStrategy> constructor = temp.getDeclaredConstructor(AbstractContext.class);
            constructor.setAccessible(true);

            ComplexTypeStrategy complexTypeStrategy = constructor.newInstance(this);
            if (complexTypeStrategy.strategyMatch(sourceBeanType, targetType)) {
                LOG.info("sourceBeanType：{} is {}，targetType：{} is {}，匹配策略 {}", sourceBeanType.getTypeName(), sourceBeanType.getClass().getSimpleName(), targetType.getTypeName(), targetType.getClass().getSimpleName(), complexTypeStrategy.getClass().getSimpleName());
                return complexTypeStrategy.geneTransform(sourceBeanType, targetType, geneClassName(), fieldNamePrefix);
            }
        }
        // 如果没查找到对应策略，则使用默认策略

        LOG.warn("sourceBeanType：{}   {}，targetType：{}   {}，未查找到对应策略，则使用默认策略,空值转换", sourceBeanType.getTypeName(), sourceBeanType.getClass().getSimpleName(), targetType.getTypeName(), targetType.getClass().getSimpleName());

        return new DefaultComplexTypeStrategy(this).geneTransform(sourceBeanType, targetType, geneClassName(), fieldNamePrefix);

    }



    @Override
    public String geneClassName() {
        String fieldNameRefactor = targetField.getName().substring(0, 1).toUpperCase() + targetField.getName().substring(1);
        return EXTEND_TRANSFORM_IMPL_CLASS_NAME_PREFIX + "$" + ownerClass.getSimpleName() + "$" + fieldNameRefactor;
    }

    @Override
    public String getIdentify() {
        return super.getIdentify();
    }

    @Override
    public void setIdentify(String identify) {
        super.setIdentify(identify);
    }

    @Override
    public Field getSourceField() {
        return super.getSourceField();
    }

    @Override
    public void setSourceField(Field sourceField) {
        super.setSourceField(sourceField);
    }

    @Override
    public Field getTargetField() {
        return super.getTargetField();
    }

    @Override
    public void setTargetField(Field targetField) {
        super.setTargetField(targetField);
    }

    @Override
    public Class getOwnerClass() {
        return super.getOwnerClass();
    }

    @Override
    public void setOwnerClass(Class ownerClass) {
        super.setOwnerClass(ownerClass);
    }
}
