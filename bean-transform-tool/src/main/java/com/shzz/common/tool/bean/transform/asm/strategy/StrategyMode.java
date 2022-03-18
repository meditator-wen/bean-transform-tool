package com.shzz.common.tool.bean.transform.asm.strategy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * 策略模式
 *
 * @author Administrator
 * @date 2022/03/09
 */
public enum StrategyMode {

    /**
     * 映射到地图模式
     */
    MAP_TO_MAP_PATTERN(5,"map_to_map_pattern"),

    /**
     * 集合数组模式
     */
    COLLECTION_TO_ARRAY_PATTERN(4,"collection_to_array_pattern"),

    /**
     * 数组,数组模式
     */
    ARRAY_TO_ARRAY_PATTERN(3,"array_to_array_pattern"),
    /**
     * 数组集合模式
     */
    ARRAY_TO_COLLECTION_PATTERN(2,"array_to_collection_pattern"),
    /**
     * 收集收集模式
     */
    COLLECTION_TO_COLLECTION_PATTERN(1,"collection_to_collection_pattern"),
    /**
     * 默认策略
     */
    DEFAULT_STRATEGY(0,"default");

    /**
     * 模式
     */
    private int mode;
    /**
     * 描述
     */
    private String description;


    /**
     * 策略模式
     *
     * @param mode        模式
     * @param description 描述
     */
    private StrategyMode(int mode, String description){
        this.description=description;
        this.mode=mode;
    }

    /**
     * 字符串
     *
     * @return {@link String}
     */
    @Override
    public String toString() {
        return "StrategyMode{" +
                "mode=" + mode +
                ", description='" + description + '\'' +
                '}';
    }
}
