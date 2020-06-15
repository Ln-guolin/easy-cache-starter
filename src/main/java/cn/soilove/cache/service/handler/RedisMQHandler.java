package cn.soilove.cache.service.handler;

import cn.soilove.cache.service.RedisService;
import cn.soilove.cache.utils.RedisKeysEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;

/**
 * redis mq
 *
 * @author: Chen GuoLin
 * @create: 2020-05-08 11:47
 **/
@Slf4j(topic = "[redis][mq]")
@Component
public class RedisMQHandler {
    /**
     * 设置单次处理的最大消息条数
     */
    private static final int WORK_NUM = 10;

    @Autowired
    private RedisService redisService;

    /**
     * 订阅
     * @param topic
     * @param content
     */
    public void lpush(String topic,String content){
        String topicKey = RedisKeysEnum.REDIS_MQ_TOPIC.parseKey(topic);
        redisService.lpush(topicKey,content);
        log.info("[消息订阅]订阅完成！topic="+topicKey+"，content=" + content);
    }

    /**
     * 订阅 - 延时队列
     * @param topic
     * @param content
     * @param delaySecond
     */
    public void lpush4delay(String topic,String content,int delaySecond){
        String topicKey = RedisKeysEnum.REDIS_MQ_DELAY_TOPIC.parseKey(topic);

        // 时间转换
        Date date = addSecond(new Date(),delaySecond);

        long doTime = date.getTime();

        redisService.zadd(topicKey,doTime,content);
        log.info("[消息订阅][延时队列]订阅完成！topic="+topicKey+"，content=" + content + "，delaySecond=" + delaySecond + "，执行时间=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
    }

    /**
     * 消费 - 心跳监听
     * @param topic
     * @return
     */
    public void rpop(String topic, Function function){
        String topicKey = RedisKeysEnum.REDIS_MQ_TOPIC.parseKey(topic);
        // 设置单次处理的最大消息条数
        int n = 0;
        while (n < WORK_NUM){
            n ++;
            String content = redisService.rpop(topicKey);
            if(StringUtils.isEmpty(content)){
                // 无消息时直接跳出，等待下次心跳
                break;
            }

            try {
                log.info("[消息消费]开始消费！topic="+topicKey+"，content=" + content);
                function.apply(content);
                log.info("[消息消费]消费完成！topic="+topicKey+"，content=" + content);
            }catch (Exception e){
                // 补偿消息
                redisService.lpush(topicKey,content);
                log.error("[消息消费]消费失败，加入队列进行补偿：topic="+topicKey+"，content=" + content);
            }
        }
    }

    /**
     * 消费 - 心跳监听
     * @param topic
     * @return
     */
    public void rpop4delay(String topic, Function function){
        String topicKey = RedisKeysEnum.REDIS_MQ_DELAY_TOPIC.parseKey(topic);

        // 时间转换
        long doTime = System.currentTimeMillis();

        // 获取已到执行时间的消息列表
        Set<String> contents = redisService.zrangeByScore(topicKey,0,doTime);
        if(CollectionUtils.isEmpty(contents)){
            // 无消息时直接跳出，等待下次心跳
            return;
        }

        // 执行
        for(String content : contents){
            try {
                log.info("[消息消费][延时队列]开始消费！topic="+topicKey+"，content=" + content);
                function.apply(content);
                redisService.zrem(topicKey,content);
                log.info("[消息消费][延时队列]消费完成！topic="+topicKey+"，content=" + content);
            }catch (Exception e){
                // 补偿消息
                redisService.zadd(topicKey,doTime,content);
                log.error("[消息消费][延时队列]消费失败，加入队列进行补偿：topic="+topicKey+"，content=" + content);
            }
        }
    }

    /**
     * 增加秒
     * @param date
     * @param seconds
     * @return
     */
    public static Date addSecond(Date date, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 时区设置
        cal.setTime(date);
        cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + seconds);
        return cal.getTime();
    }

}
