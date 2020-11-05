package cn.soilove.cache.utils;

import cn.soilove.cache.config.CacheStarterException;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器
 *
 * @author: Chen GuoLin
 * @create: 2020-10-30 14:15
 **/
@Slf4j
public class BloomFilterUtils {

    private static final Map<String, BloomFilter<CharSequence>> bloomFilterMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> expectedInsertionsMap = new ConcurrentHashMap<>();
    private static final double DEF_FPP = 0.00001;

    /**
     * 创建过滤器
     * @param namespace 过滤器命名空间
     * @param expectedInsertions 过滤器容量
     * @return
     */
    public static BloomFilter<CharSequence> create(String namespace, int expectedInsertions){
        return create(namespace,expectedInsertions,null);
    }

    /**
     * 创建过滤器
     * @param namespace 过滤器命名空间
     * @param expectedInsertions 过滤器容量
     * @param fpp 期望的假阳性概率 0-1之间取值，值越小越精确，消耗的内存也更大，如：0.00001
     * @return
     */
    public static BloomFilter<CharSequence> create(String namespace, int expectedInsertions,Double fpp){
        BloomFilter<CharSequence> bloomFilter = bloomFilterMap.get(namespace);
        if(bloomFilter == null){
            bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), expectedInsertions, fpp != null ? fpp : DEF_FPP);
            bloomFilterMap.put(namespace,bloomFilter);
            // 记录容量
            expectedInsertionsMap.put(namespace,expectedInsertions);
        }
        return bloomFilter;
    }

    /**
     * 添加元素
     * @param namespace 过滤器命名空间
     * @param item 元素
     */
    public static void put(String namespace,String item){
        BloomFilter<CharSequence> bloomFilter = getCharSequenceBloomFilter(namespace);
        check(bloomFilter,namespace);
        bloomFilter.put(item);
    }

    /**
     * 添加元素
     * @param namespace 过滤器命名空间
     * @param items 元素
     */
    public static void put(String namespace,String ... items){
        BloomFilter<CharSequence> bloomFilter = getCharSequenceBloomFilter(namespace);
        for (String item : items){
            check(bloomFilter,namespace);
            bloomFilter.put(item);
        }
    }

    /**
     * 添加元素
     * @param namespace 过滤器命名空间
     * @param items 元素
     */
    public static void put(String namespace, List<String> items){
        BloomFilter<CharSequence> bloomFilter = getCharSequenceBloomFilter(namespace);
        for (String item : items){
            check(bloomFilter,namespace);
            bloomFilter.put(item);
        }
    }

    /**
     * 是否可能包含元素
     * @param namespace 过滤器命名空间
     * @param item 元素
     */
    public static boolean mightContain(String namespace,String item){
        BloomFilter<CharSequence> bloomFilter = getCharSequenceBloomFilter(namespace);
        return bloomFilter.mightContain(item);
    }

    /**
     * 获取布隆过滤器对象
     * @param namespace 过滤器命名空间
     * @return
     */
    private static BloomFilter<CharSequence> getCharSequenceBloomFilter(String namespace) {
        BloomFilter<CharSequence> bloomFilter = bloomFilterMap.get(namespace);
        if (bloomFilter == null) {
            log.error("[starter][cache][bloomFilter]" + CacheStarterCode.BLOOM_FILTER_NOT_EXISTS_ERROR.getMsg() + "，namespace:" + namespace);
            throw new CacheStarterException(CacheStarterCode.BLOOM_FILTER_NOT_EXISTS_ERROR);
        }
        return bloomFilter;
    }

    /**
     * 检测容量
     * @param bloomFilter 过滤器
     * @param namespace 过滤器命名空间
     */
    private static void check(BloomFilter<CharSequence> bloomFilter,String namespace){
        int count = Optional.ofNullable(expectedInsertionsMap.get(namespace)).orElse(0);
        if(count <= bloomFilter.approximateElementCount()){
            log.error("[starter][cache][bloomFilter]" + CacheStarterCode.BLOOM_FILTER_ITEM_OUT_ERROR.getMsg() + "，已达上限值：" + bloomFilter.approximateElementCount() + "，namespace：" + namespace);
            throw new CacheStarterException(CacheStarterCode.BLOOM_FILTER_ITEM_OUT_ERROR.getCode(),CacheStarterCode.BLOOM_FILTER_ITEM_OUT_ERROR.getMsg() + "，已达上限值：" + bloomFilter.approximateElementCount() + "，namespace:" + namespace);
        }
    }
}



