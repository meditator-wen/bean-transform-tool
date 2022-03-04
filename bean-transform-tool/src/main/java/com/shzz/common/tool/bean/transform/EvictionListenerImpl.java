package com.shzz.common.tool.bean.transform;

import com.googlecode.concurrentlinkedhashmap.EvictionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  google map，注册监听类，监听map 元素清除事件，基于LRU 算法。
 *
 * @author wen wang
 * @date 2022/03/04
 */
public class EvictionListenerImpl<K, V> implements EvictionListener<K, V> {
    private final static Logger LOG = LoggerFactory.getLogger(com.shzz.common.tool.bean.transform.EvictionListenerImpl.class);
    // 缓存名称
    private String cacheVariableName;

    public EvictionListenerImpl(String cacheVariableName) {
        this.cacheVariableName = cacheVariableName;
    }


    @Override
    public void onEviction(K k, V v) {

        LOG.warn(cacheVariableName + "  Evicted key=" + k);

    }

}