package cn.soilove.cache.service;

import java.util.function.Supplier;

/**
 * 本地缓存
 *
 * @author: Chen GuoLin
 * @create: 2020-04-14 17:16
 **/
public interface CaffeineService {

    /**
     * 固定时间缓存 - 1分钟
     * @param key
     * @param supplier
     * @param <R>
     * @return
     */
    <R> R getFixed(String key,Supplier<R> supplier);

    /**
     * 删除固定时间缓存 - 1分钟
     * @param key
     */
    void delFixed(String key);

    /**
     * 固定时间缓存 - 1小时
     * @param key
     * @param supplier
     * @param <R>
     * @return
     */
    <R> R getFixed4Hours(String key, Supplier<R> supplier);

    /**
     * 删除固定时间缓存 - 1小时
     * @param key
     */
    void delFixed4Hours(String key);

    /**
     * 固定时间缓存 - 1小时
     * @param key
     * @param supplier
     * @param <R>
     * @return
     */
    <R> R getFixed4Days(String key,Supplier<R> supplier);

    /**
     * 删除固定时间缓存 - 1小时
     * @param key
     */
    void delFixed4Days(String key);

    /**
     * 动态时间缓存
     * @param key
     * @param expireSecond
     * @param supplier
     * @param <R>
     * @return
     */
    <R> R get(String key, long expireSecond, Supplier<R> supplier);

    /**
     * 获取缓存
     * @param key
     */
    <R> R get(String key);

    /**
     * 删除缓存
     * @param key
     */
    void del(String key);

}
