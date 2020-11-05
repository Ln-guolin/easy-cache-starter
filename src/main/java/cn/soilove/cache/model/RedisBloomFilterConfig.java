package cn.soilove.cache.model;

import lombok.Data;

/**
 * Redis布隆过滤器配置
 *
 * @author: Chen GuoLin
 * @create: 2020-11-05 16:58
 **/
@Data
public class RedisBloomFilterConfig {
    private long bitSize;
    private int numHashFunctions;
}



