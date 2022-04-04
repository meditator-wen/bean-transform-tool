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
package com.shzz.common.tool.bean.transform.asm.strategy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * 策略模式
 * @author wen wang
 *
 * @date 2022/03/09
 */
public enum StrategyMode {

    /**
     * Map转换Map模式
     */
    MAP_TO_MAP_PATTERN(5,"map_to_map_pattern"),

    /**
     * 集合转换数组模式
     */
    COLLECTION_TO_ARRAY_PATTERN(4,"collection_to_array_pattern"),

    /**
     * 数组转换数组模式
     */
    ARRAY_TO_ARRAY_PATTERN(3,"array_to_array_pattern"),
    /**
     * 数组转换集合模式
     */
    ARRAY_TO_COLLECTION_PATTERN(2,"array_to_collection_pattern"),
    /**
     * 集合转换集合模式
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
     * @param mode        模式
     * @param description 描述
     */
    private StrategyMode(int mode, String description){
        this.description=description;
        this.mode=mode;
    }

    /**
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
