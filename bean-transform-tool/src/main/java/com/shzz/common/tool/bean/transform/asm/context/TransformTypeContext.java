package com.shzz.common.tool.bean.transform.asm.context;

import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.Transform;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import com.shzz.common.tool.bean.transform.asm.TypeTransformAssist;
import com.shzz.common.tool.bean.transform.asm.strategy.*;
import com.shzz.common.tool.bean.transform.asm.strategy.ArrayTypeStrategy;
import com.shzz.common.tool.bean.transform.asm.strategy.MapTypeStrategy;
import com.shzz.common.tool.code.BeanTransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
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

    // 缓存 策略，key 为策略优先级，value 为策略类信息
    private static Map<Integer, Class<? extends ComplexTypeStrategy>> typeStrategyHashMap = new ConcurrentHashMap<>();

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
        /**
         * 默认策略缓存。用户拓展新增的策略通过增加register 函数添加
         * key 为优先级，数值越大，优先级越高，
         * 遍历查找缓存策略是否匹配转换场景，优先调用高优先级策略类对象的strategyMatch 方法，
         * 如果strategyMatch方法匹配则直接返回该策略对象，不匹配则继续遍历直到结束。
         * 如果同一转换场景有两种以上同质的策略，那么高优先级的会被选中，其他的忽略。
         * 比如，源类和目标类中两个需要转换的Map 类型字段，
         * typeStrategyHashMap 有两种策略类A 和 B 调用strategyMatch 方法都能匹配这种场景，
         * 只是二者内部转换方法的代码实现方式不同，那么只会选择高优先级的策略。
         *
         *  开发者拓展场景时，自行继承 {@link ComplexTypeStrategy},并将策略类手动添加到typeStrategyHashMap 中.
         *
         */
        typeStrategyHashMap.put(4, UniversalClassTypeStrategy.class);
        typeStrategyHashMap.put(3, ArrayTypeStrategy.class);
        typeStrategyHashMap.put(2, CollectionTypeStrategy.class);
        typeStrategyHashMap.put(1, MapTypeStrategy.class);

    }

    public static void registerStrategy(Integer priority, Class<? extends ComplexTypeStrategy> strategyClass) throws BeanTransformException {
        /**
         * @Description: 开发者拓展场景时，自行继承 {@link ComplexTypeStrategy},
         * 并调用该方法添加到typeStrategyHashMap 中.
         * 注册的策略类在整个java 进程中全局可用。
         * @Author: wen wang
         * @Date: 2022/1/22 14:51
         * @param priority:
         * @param strategyClass:
         * @return: void
         **/
        if (typeStrategyHashMap.containsKey(priority)) {
            Class originalStrategyClass = typeStrategyHashMap.get(priority);
            throw new BeanTransformException("0xffb0", "注册转换策略异常", "指定的优先级 " + priority + " 已经有对应策略类: " + originalStrategyClass.getName());
        } else {
            typeStrategyHashMap.put(priority, strategyClass);
        }


    }


    @Override
    public Map<String, ? extends Transform> geneTransform(Type sourceBeanType, Type targetType, String fieldNamePrefix) throws Exception {
        //, TransformUtilGenerate transformUtilGenerate

        Collection<Class<? extends ComplexTypeStrategy>> strategyImplClasses = typeStrategyHashMap.values();

        Set<Integer> prioritys = typeStrategyHashMap.keySet();
        List<Integer> sorts = new ArrayList<>();
        sorts.addAll(prioritys);
        Collections.sort(sorts, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        Type fieldGenericType = targetField.getGenericType();
        boolean flagTarget = (fieldGenericType instanceof GenericArrayType) ||
                (fieldGenericType instanceof TypeVariable) ||
                (fieldGenericType instanceof WildcardType) ||
                (fieldGenericType instanceof ParameterizedType) ||
                ((fieldGenericType instanceof Class) && (((Class) fieldGenericType).isArray()));


        if (!flagTarget) {
            return null;
        }

        for (Integer key : sorts) {

            Class<? extends ComplexTypeStrategy> temp = typeStrategyHashMap.get(key);
            // LOG.info("iteration strategy key=={},strategy class name={}",key,temp.getSimpleName());
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
