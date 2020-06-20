package cn.soilove.cache.service.impl;

import cn.soilove.cache.service.CaffeineService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 本地缓存
 *
 * @author: Chen GuoLin
 * @create: 2020-04-14 17:17
 **/
public class CaffeineServiceImpl implements CaffeineService {
    /**
     * 空值
     */
    private static final String NULL_VALUE = "NULL_VALUE";

    /**
     * 本地缓存最大数量
     */
    private static final long LOCAL_CAFFEINE_MAXIMUM_SIZE = 100_0000;

    /**
     * 固定时间缓存 - 1分钟
     */
    private static final Cache<Object, Object> fixedCache = Caffeine.newBuilder()
            .maximumSize(LOCAL_CAFFEINE_MAXIMUM_SIZE)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
    /**
     * 固定时间缓存 - 1小时
     */
    private static final Cache<Object, Object> fixed4HourCache = Caffeine.newBuilder()
            .maximumSize(LOCAL_CAFFEINE_MAXIMUM_SIZE)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    /**
     * 固定时间缓存 - 1天
     */
    private static final Cache<Object, Object> fixed4DayCache = Caffeine.newBuilder()
            .maximumSize(LOCAL_CAFFEINE_MAXIMUM_SIZE)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    /**
     * 动态时间缓存map
     * key=缓存key
     * value=Caffeine.newBuilder()
     */
    private static final Map<String, Cache<Object, Object>> cacheMap = new ConcurrentHashMap<>();


    @Override
    public <R> R getFixed(String key,Supplier<R> supplier){
        return getCaffeine(fixedCache,key,supplier);
    }

    @Override
    public void setFixed(String key, Object obj) {
        fixedCache.put(key,obj);
    }

    @Override
    public void delFixed(String key){
        fixedCache.invalidate(key);
    }

    @Override
    public <R> R getFixed4Hours(String key, Supplier<R> supplier) {
        return getCaffeine(fixed4HourCache,key,supplier);
    }

    @Override
    public void setFixed4Hours(String key, Object obj) {
        fixed4HourCache.put(key,obj);
    }

    @Override
    public void delFixed4Hours(String key) {
        fixed4HourCache.invalidate(key);
    }

    @Override
    public <R> R getFixed4Days(String key, Supplier<R> supplier) {
        return getCaffeine(fixed4DayCache,key,supplier);
    }

    @Override
    public void setFixed4Days(String key, Object obj) {
        fixed4DayCache.put(key,obj);
    }

    @Override
    public void delFixed4Days(String key) {
        fixed4DayCache.invalidate(key);
    }

    @Override
    public <R> R get(String key, long expireSecond, Supplier<R> supplier){
        // 获取缓存
        Cache<Object, Object> caffeineCache = loadCaffeine(key,expireSecond);

        return getCaffeine(caffeineCache,key,supplier);
    }

    @Override
    public void set(String key, long expireSecond, Object obj) {
        // 获取缓存
        Cache<Object, Object> caffeineCache = loadCaffeine(key,expireSecond);
        caffeineCache.put(key,obj);
    }

    @Override
    public <R> R get(String key){
        Cache<Object, Object> caffeineCache = cacheMap.get(key);
        if(caffeineCache != null){
            Object obj = caffeineCache.getIfPresent(key);
            if(obj == null){
                return null;
            }
            // 空缓存判断
            if(Objects.equals(obj,NULL_VALUE)){
                return null;
            }
            return (R) obj;
        }
        return null;
    }

    @Override
    public void del(String key){
        Cache<Object, Object> caffeineCache = cacheMap.get(key);
        if(caffeineCache != null){
            caffeineCache.invalidate(key);
        }
    }

    /**
     * 获取缓存
     * @param key
     * @param expireSecond
     * @return
     */
    private static Cache<Object, Object> loadCaffeine(String key, long expireSecond){
        Cache<Object, Object> caffeineCache = cacheMap.get(key);
        if(caffeineCache == null){
            caffeineCache = Caffeine.newBuilder()
                    .maximumSize(LOCAL_CAFFEINE_MAXIMUM_SIZE)
                    .expireAfterWrite(expireSecond, TimeUnit.SECONDS)
                    .build();
            cacheMap.put(key,caffeineCache);
        }
        return caffeineCache;
    }

    /**
     * 获取缓存
     * @param caffeineCache
     * @param key
     * @param supplier
     * @param <R>
     * @return
     */
    private  <R> R getCaffeine(Cache<Object, Object> caffeineCache, String key, Supplier<R> supplier){
        // 读取缓存
        Object obj = caffeineCache.getIfPresent(key);
        if (obj != null) {

            // 空缓存判断
            if(Objects.equals(obj,NULL_VALUE)){
                return null;
            }

            return (R) obj;
        }

        // DB操作
        R result = supplier.get();
        if (result != null) {
            // 设置本地缓存
            caffeineCache.put(key, result);
        } else {
            // 数据为空的时候，设置空值缓存
            caffeineCache.put(key, NULL_VALUE);
        }
        return result;
    }

}
