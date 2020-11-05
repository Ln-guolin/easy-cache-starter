package cn.soilove.cache.service.handler;

import cn.soilove.cache.config.CacheStarterException;
import cn.soilove.cache.model.RedisBloomFilterConfig;
import cn.soilove.cache.service.RedisService;
import cn.soilove.cache.utils.CacheStarterCode;
import cn.soilove.cache.utils.RedisKeysEnum;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Pipeline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * redis布隆过滤器
 *
 * @author: Chen GuoLin
 * @create: 2020-11-05 12:05
 **/
@Slf4j
@Component
public class RedisBloomFilter {

    @Autowired
    private RedisService redisService;

    private static final Map<String, RedisBloomFilterConfig> bloomFilterConfigMap = new ConcurrentHashMap<>();

    /**
     * 构造布隆过滤器
     *
     * @param expectedInsertions 过滤器容量
     * @param fpp               期望的假阳性概率 0-1之间取值，值越小越精确，消耗的内存也更大，如：0.00001
     */
    public void create(String key,int expectedInsertions, double fpp) {
        checkArgument(expectedInsertions >= 0, "Expected insertions (%s) must be >= 0", expectedInsertions);
        checkArgument(fpp > 0.0, "False positive probability (%s) must be > 0.0", fpp);
        checkArgument(fpp < 1.0, "False positive probability (%s) must be < 1.0", fpp);
        if (expectedInsertions == 0) {
            expectedInsertions = 1;
        }

        // 配置类
        RedisBloomFilterConfig config = new RedisBloomFilterConfig();

        // Funnel 设置
        config.setFunnel(Funnels.stringFunnel(Charsets.UTF_8));
        // bit size 计算
        config.setBitSize(optimalNumOfBits(expectedInsertions, fpp));
        checkArgument(config.getBitSize() > 0, "data length is zero!");

        // numHashFunctions 计算
        config.setNumHashFunctions(optimalNumOfHashFunctions(expectedInsertions, config.getBitSize()));
        checkArgument(config.getNumHashFunctions() > 0, "numHashFunctions (%s) must be > 0", config.getNumHashFunctions());
        checkArgument(config.getNumHashFunctions() <= 255, "numHashFunctions (%s) must be <= 255", config.getNumHashFunctions());

        // 缓存配置
        String configKey = RedisKeysEnum.REDIS_BF_CONFIG.parseKey(key);
        redisService.set(configKey, JSON.toJSONString(config));

        // 缓存配置到本地
        key = RedisKeysEnum.REDIS_BF_NAMESPACE.parseKey(key);
        bloomFilterConfigMap.put(key,config);
    }


    /**
     * 添加元素
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        long[] offset = this.murmurHashOffset(key,value);

        // 命名空间key
        key = RedisKeysEnum.REDIS_BF_NAMESPACE.parseKey(key);

        Pipeline pipeline = redisService.getPipeline();
        for (long i : offset) {
            pipeline.setbit(key, i, true);
        }
        pipeline.syncAndReturnAll();
    }

    /**
     * 是否可能包含元素
     * @param key
     * @param value
     * @return
     */
    public boolean mightContain(String key, String value) {
        long[] offset = this.murmurHashOffset(key,value);

        // 命名空间key
        key = RedisKeysEnum.REDIS_BF_NAMESPACE.parseKey(key);

        Pipeline pipeline = redisService.getPipeline();
        for (long index : offset) {
            pipeline.getbit(key, index);
        }
        return !pipeline.syncAndReturnAll().contains(false);
    }

    /**
     * 获取配置
     * @param key
     * @return
     */
    private RedisBloomFilterConfig getConfig(String key){
        // 缓存配置
        String configKey = RedisKeysEnum.REDIS_BF_CONFIG.parseKey(key);
        String obj = redisService.get(configKey);
        if(StringUtils.isEmpty(obj)){
            log.error("[starter][cache][bloomFilter]" + CacheStarterCode.BLOOM_FILTER_NOT_EXISTS_ERROR.getMsg() + "，namespace:" + key);
            throw new CacheStarterException(CacheStarterCode.BLOOM_FILTER_NOT_EXISTS_ERROR);
        }
        RedisBloomFilterConfig config = JSON.parseObject(obj,RedisBloomFilterConfig.class);

        // 缓存配置到本地
        key = RedisKeysEnum.REDIS_BF_NAMESPACE.parseKey(key);
        bloomFilterConfigMap.put(key,config);
        return config;
    }

    /**
     * 计算元素值哈希后映射到Bitmap的位置
     * @param value 元素值
     * @return bit下标的数组
     */
    private long[] murmurHashOffset(String key, String value) {

        RedisBloomFilterConfig config = bloomFilterConfigMap.get(key);
        if(config == null){
            config = getConfig(key);
        }

        long hash64 = Hashing.murmur3_128().hashObject(value, config.getFunnel()).asLong();
        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);

        long[] offset = new long[config.getNumHashFunctions()];
        for (int i = 1; i <= config.getNumHashFunctions(); i++) {
            int combinedHash = hash1 + i * hash2;
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }
            offset[i - 1] = combinedHash % config.getBitSize();
        }
        return offset;
    }


    /**
     * Computes m (total bits of Bloom filter) which is expected to achieve, for the specified
     * expected insertions, the required false positive probability.
     *
     * <p>See http://en.wikipedia.org/wiki/Bloom_filter#Probability_of_false_positives for the
     * formula.
     *
     * @param n expected insertions (must be positive)
     * @param p false positive rate (must be 0 < p < 1)
     */
    private long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    /**
     * Computes the optimal k (number of hashes per element inserted in Bloom filter), given the
     * expected insertions and total number of bits in the Bloom filter.
     *
     * <p>See http://en.wikipedia.org/wiki/File:Bloom_filter_fp_probability.svg for the formula.
     *
     * @param n expected insertions (must be positive)
     * @param m total number of bits in Bloom filter (must be positive)
     */
    private int optimalNumOfHashFunctions(long n, long m) {
        // (m / n) * log(2), but avoid truncation due to division!
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }
}