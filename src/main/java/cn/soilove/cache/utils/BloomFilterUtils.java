package cn.soilove.cache.utils;

import cn.soilove.cache.config.CacheStarterException;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器
 *
 * @author: Chen GuoLin
 * @create: 2020-10-30 14:15
 **/
public class BloomFilterUtils {

    private static final Map<String, BloomFilter<CharSequence>> bloomFilterMap = new ConcurrentHashMap<>();

    private static final double fpp = 0.00001;

    /**
     * 创建过滤器
     * @param namespace
     * @param expectedInsertions
     * @return
     */
    public static BloomFilter<CharSequence> create(String namespace, int expectedInsertions){

        BloomFilter<CharSequence> bloomFilter = bloomFilterMap.get(namespace);
        if(bloomFilter == null){
            bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), expectedInsertions, fpp);
            bloomFilterMap.put(namespace,bloomFilter);
        }
        return bloomFilter;
    }

    /**
     * 添加元素
     * @param namespace
     * @param keys
     */
    public static void put(String namespace,String ... keys){
        put(namespace, Lists.newArrayList(keys));
    }

    /**
     * 添加元素
     * @param namespace
     * @param keys
     */
    public static void put(String namespace, List<String> keys){
        BloomFilter<CharSequence> bloomFilter = bloomFilterMap.get(namespace);
        if(bloomFilter == null){
            throw new CacheStarterException(CacheStarterCode.BLOOM_FILTER_NOT_EXISTS_ERROR);
        }
        for (String key : keys){
            bloomFilter.put(key);
        }
    }

    /**
     * 是否可能包含元素
     * @param namespace
     * @param key
     */
    public static boolean mightContain(String namespace,String key){
        BloomFilter<CharSequence> bloomFilter = bloomFilterMap.get(namespace);
        if(bloomFilter == null){
            throw new CacheStarterException(CacheStarterCode.BLOOM_FILTER_NOT_EXISTS_ERROR);
        }
        return bloomFilter.mightContain(key);
    }
}



