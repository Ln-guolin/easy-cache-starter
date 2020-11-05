package cn.soilove.cache.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回code
 *
 * @author: Chen GuoLin
 * @create: 2020-01-29 15:30
 **/
@Getter
@AllArgsConstructor
public enum CacheStarterCode {

    ERROR(-1,"Cache执行错误"),
    CONFIG_ERROR(-2,"Cache Starter配置错误"),
    LOCK_ERROR(101,"加锁错误"),
    IDEMPOTENT_ERROR(102,"幂等错误"),
    BLOOM_FILTER_NOT_EXISTS_ERROR(103,"布隆过滤器不存在"),
    BLOOM_FILTER_ITEM_OUT_ERROR(104,"布隆过滤器容量不足"),

    ;

    private int code;
    private String msg;
}
