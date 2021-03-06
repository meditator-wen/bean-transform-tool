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
package com.shzz.common.tool.code;


/**
 * 错误码接口
 *
 * @author wen wang
 * @date 2021/9/1 22:58
 */
public interface ErrorEncoder {


    /**
     * @return {@link String}
     */
    String getErrorCode();

    /**
     * @return {@link String}
     */
    String getErrorOutline();

    /**
     * @return {@link String}
     */
    String getErrorChainDetail();
}
