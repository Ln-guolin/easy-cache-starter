package cn.soilove.cache.utils;

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
public class CaffeineCacheUtils {
    /**
     * 空值
     */
    private static final String NULL_VALUE = "NULL_VALUE";

    /**
     * 本地缓存最大数量
     */
    private static final long LOCAL_CAFFEINE_MAXIMUM_SIZE = 1000_0000;

    /**
     * 固定时间缓存 - 1分钟
     */
    private static final Cache<Object, Object> fixed4MinutesCache = Caffeine.newBuilder()
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
     * key=缓存module
     * value=Caffeine.newBuilder()
     */
    private static final Map<String, Cache<Object, Object>> cacheMap = new ConcurrentHashMap<>();


    /**
     * 获取缓存、无则设值 - 1分钟
     * @param key
     * @param supplier
     * @param <R>
     * @return
     */
    public static <R> R getFixed4Minutes(String key,Supplier<R> supplier){
        return getR4Set(fixed4MinutesCache,key,supplier);
    }

    /**
     * 获取缓存 - 1分钟
     * @param key
     * @param <R>
     * @return
     */
    public static <R> R getFixed4Minutes(String key){
        return getR(key, fixed4MinutesCache);
    }

    /**
     * 缓存设值 - 1分钟
     * @param key
     * @param obj
     */
    public static void setFixed4Minutes(String key, Object obj) {
        fixed4MinutesCache.put(key,obj);
    }

    /**
     * 删除缓存 - 1分钟
     * @param key
     */
    public static void delFixed4Minutes(String key){
        fixed4MinutesCache.invalidate(key);
    }


    /**
     * 获取缓存、无则设值 - 1小时
     * @param key
     * @param supplier
     * @param <R>
     * @return
     */
    public static <R> R getFixed4Hours(String key, Supplier<R> supplier) {
        return getR4Set(fixed4HourCache,key,supplier);
    }

    /**
     * 获取缓存 - 1小时
     * @param key
     * @param <R>
     * @return
     */
    public static <R> R getFixed4Hours(String key) {
        return getR(key,fixed4HourCache);
    }

    /**
     * 缓存设值 - 1小时
     * @param key
     * @param obj
     */
    public static void setFixed4Hours(String key, Object obj) {
        fixed4HourCache.put(key,obj);
    }

    /**
     * 删除缓存 - 1小时
     * @param key
     */
    public static void delFixed4Hours(String key) {
        fixed4HourCache.invalidate(key);
    }

    /**
     * 获取缓存、无则设值 - 1天
     * @param key
     * @param supplier
     * @param <R>
     * @return
     */
    public static <R> R getFixed4Days(String key, Supplier<R> supplier) {
        return getR4Set(fixed4DayCache,key,supplier);
    }

    /**
     * 获取缓存 - 1天
     * @param key
     * @param <R>
     * @return
     */
    public static <R> R getFixed4Days(String key) {
        return getR(key,fixed4DayCache);
    }

    /**
     * 缓存设值 - 1天
     * @param key
     * @param obj
     */
    public static void setFixed4Days(String key, Object obj) {
        fixed4DayCache.put(key,obj);
    }

    /**
     * 删除缓存 - 1天
     * @param key
     */
    public static void delFixed4Days(String key) {
        fixed4DayCache.invalidate(key);
    }

    /**
     * 获取缓存、无则设值 - 动态缓存、模块
     * @param module
     * @param key
     * @param expireSecond
     * @param supplier
     * @param <R>
     * @return
     */
    public static <R> R get(String module,String key, long expireSecond, Supplier<R> supplier){
        // 获取缓存
        Cache<Object, Object> caffeineCache = loadCaffeine(module,expireSecond);

        return getR4Set(caffeineCache,key,supplier);
    }

    /**
     * 获取缓存 - 动态缓存、模块
     * @param module
     * @param key
     * @param <R>
     * @return
     */
    public static <R> R get(String module,String key){
        Cache<Object, Object> caffeineCache = cacheMap.get(module);
        return getR(key, caffeineCache);
    }

    /**
     * 缓存设值 - 动态缓存、模块
     * @param module
     * @param key
     * @param expireSecond
     * @param obj
     */
    public static void set(String module,String key, long expireSecond, Object obj) {
        // 获取缓存
        Cache<Object, Object> caffeineCache = loadCaffeine(module,expireSecond);
        caffeineCache.put(key,obj);
    }

    /**
     * 删除缓存 - 动态缓存、模块
     * @param module
     * @param key
     */
    public static void del(String module,String key){
        Cache<Object, Object> caffeineCache = cacheMap.get(module);
        if(caffeineCache != null){
            caffeineCache.invalidate(key);
        }
    }

    /**
     * 删除缓存模块 - 动态缓存、模块
     * @param module
     */
    public static void del(String module){
        cacheMap.remove(module);
    }

    /**
     * 加载Caffeine对象
     * @param module
     * @param expireSecond
     * @return
     */
    private static Cache<Object, Object> loadCaffeine(String module, long expireSecond){
        Cache<Object, Object> caffeineCache = cacheMap.get(module);
        if(caffeineCache == null){
            caffeineCache = Caffeine.newBuilder()
                    .maximumSize(LOCAL_CAFFEINE_MAXIMUM_SIZE)
                    .expireAfterWrite(expireSecond, TimeUnit.SECONDS)
                    .build();
            cacheMap.put(module,caffeineCache);
        }
        return caffeineCache;
    }

    private static <R> R getR4Set(Cache<Object, Object> caffeineCache, String key, Supplier<R> supplier){
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

    private static <R> R getR(String key, Cache<Object, Object> caffeineCache) {
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

}
