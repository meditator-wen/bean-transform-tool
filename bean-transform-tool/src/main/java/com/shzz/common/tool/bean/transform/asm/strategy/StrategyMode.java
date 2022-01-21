package com.shzz.common.tool.bean.transform.asm.strategy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Classname StrategyMode
 * @Description TODO
 * @Date 2021/12/27 19:32
 * @Created by wen wang
 */
public enum StrategyMode {

    MAP_TO_MAP_PATTERN(5,"map_to_map_pattern"),

    COLLECTION_TO_ARRAY_PATTERN(4,"collection_to_array_pattern"),

    ARRAY_TO_ARRAY_PATTERN(3,"array_to_array_pattern"),
    ARRAY_TO_COLLECTION_PATTERN(2,"array_to_collection_pattern"),
    COLLECTION_TO_COLLECTION_PATTERN(1,"collection_to_collection_pattern"),
    DEFAULT_STRATEGY(0,"default");

    private int mode;
    private String description;


    private StrategyMode(int mode,String description){
       this.description=description;
       this.mode=mode;
    }

    @Override
    public String toString() {
        return "StrategyMode{" +
                "mode=" + mode +
                ", description='" + description + '\'' +
                '}';
    }
}
