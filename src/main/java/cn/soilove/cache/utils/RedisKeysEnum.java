package cn.soilove.cache.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缓存key
 *
 * @author: Chen GuoLin
 * @create: 2020-02-01 17:30
 **/
@Getter
@AllArgsConstructor
public enum RedisKeysEnum {

    /**
     * redis mq key
     * [0] = biz
     */
    REDIS_MQ_TOPIC("mq:topic:im:%s"),

    /**
     * redis mq delay key
     * [0] = biz
     */
    REDIS_MQ_DELAY_TOPIC("mq:topic:delay:%s");

    ;

    private String key;

    /**
     * 缓存key处理
     * 用法，CacheKeysEnum.*.parse
     * @param args
     * @return
     */
    public String parseKey(Object ... args){
        return String.format(this.key,args);
    }

}
