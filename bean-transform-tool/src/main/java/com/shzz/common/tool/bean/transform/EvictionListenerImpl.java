package com.shzz.common.tool.bean.transform;

import com.googlecode.concurrentlinkedhashmap.EvictionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author by wen wang
 * @description TODO
 * @created 2022/3/4 20:15
 */
public class EvictionListenerImpl<K, V> implements EvictionListener<K, V> {
    private final static Logger LOG = LoggerFactory.getLogger(EvictionListenerImpl.class);
    private String cacheVariableName;// 缓存名称

    public EvictionListenerImpl(String cacheVariableName) {
        this.cacheVariableName = cacheVariableName;
    }


    @Override
    public void onEviction(K k, V v) {

        LOG.warn(cacheVariableName + "  Evicted key=" + k);

    }
}