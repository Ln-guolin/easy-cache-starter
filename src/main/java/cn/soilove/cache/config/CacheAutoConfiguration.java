package cn.soilove.cache.config;

import cn.soilove.cache.properties.RedisProperties;
import cn.soilove.cache.service.RedisService;
import cn.soilove.cache.service.impl.JedisClusterServiceImpl;
import cn.soilove.cache.service.impl.JedisSentinelServiceImpl;
import cn.soilove.cache.service.impl.JedisSingleServiceImpl;
import cn.soilove.cache.utils.CacheStarterCode;
import cn.soilove.cache.utils.ExceptionStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.*;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * 自动配置类
 *
 * @author: Chen GuoLin
 * @create: 2020-04-07 18:00
 **/
@Slf4j(topic = "[starter][cache]")
@Configuration
@ConditionalOnClass(value = {RedisService.class})
@EnableConfigurationProperties(RedisProperties.class)
public class CacheAutoConfiguration {

    private static final String MODE_SINGLE = "single";
    private static final String MODE_SENTINEL = "sentinel";
    private static final String MODE_CLUSTER = "cluster";

    @Resource
    private RedisProperties redisProperties;

    @Bean
    @ConditionalOnMissingBean
    public JedisPool jedisSingle(){

        // 单点
        if(redisProperties.getMode().equals(MODE_SINGLE)){
            // 获得连接池配置对象，设置配置项
            JedisPoolConfig config = buildJedisPoolConfig();

            // 获得连接池，指定host、端口、超时时间、密码和数据库
            JedisPool jedisPool = new JedisPool(
                    config,
                    redisProperties.getHost(),
                    redisProperties.getPort(),
                    redisProperties.getTimeOut(),
                    redisProperties.getPassword(),
                    redisProperties.getDatabase());
            log.info("redis建立连接成功，模式=" + redisProperties.getMode());
            return jedisPool;
        }

        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public JedisSentinelPool jedisSentinel(){

        // 主从
        if(redisProperties.getMode().equals(MODE_SENTINEL)){
            // 获得连接池配置对象，设置配置项
            JedisPoolConfig config = buildJedisPoolConfig();

            // sentinel模式的host是由多个英文逗号分隔的地址+端口组成
            Set<String> sentinels = new HashSet<String>();
            String[] hosts = redisProperties.getHost().split(",");
            for(String host : hosts){
                sentinels.add(host);
            }

            // 获得连接池，指定host、端口、超时时间、密码和数据库
            JedisSentinelPool sentinelPool = new JedisSentinelPool(
                    redisProperties.getMasterName(),
                    sentinels,
                    config,
                    redisProperties.getTimeOut(),
                    redisProperties.getPassword(),
                    redisProperties.getDatabase());
            log.info("redis建立连接成功，模式=" + redisProperties.getMode());
            return sentinelPool;
        }

        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public JedisCluster jedisCluster(){
        // 集群
        if(redisProperties.getMode().equals(MODE_CLUSTER)){
            // 获得连接池配置对象，设置配置项
            JedisPoolConfig config = buildJedisPoolConfig();

            // cluster模式的host是由多个英文逗号分隔的地址+端口组成
            Set<HostAndPort> nodes = new HashSet<HostAndPort>();
            // 添加节点
            String[] hosts = redisProperties.getHost().split(",");
            for(String host : hosts){
                String[] splits = host.split(":");
                nodes.add(new HostAndPort(splits[0], Integer.valueOf(splits[1])));
            }

            try {
                // 获得连接池，指定host、端口、超时时间、密码和数据库
                JedisCluster jedis = new JedisCluster(
                        nodes,
                        redisProperties.getTimeOut(),
                        redisProperties.getTimeOut(),
                        5,
                        redisProperties.getPassword(),
                        config);
                log.info("redis建立连接成功，模式=" + redisProperties.getMode());
                return jedis;
            } catch (Exception e) {
                throw new CacheStarterException(CacheStarterCode.CONFIG_ERROR.getCode(),"[starter][cache]redis获取连接发生异常，msg="+ ExceptionStringUtils.getStackTraceAsString(e));
            }
        }

        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisService redisBean() {

        // 单点
        if(redisProperties.getMode().equals(MODE_SINGLE)){
            return new JedisSingleServiceImpl();
        }
        // 主从
        if(redisProperties.getMode().equals(MODE_SENTINEL)){
            return new JedisSentinelServiceImpl();
        }
        // 集群
        if(redisProperties.getMode().equals(MODE_CLUSTER)){
            return new JedisClusterServiceImpl();
        }
        log.error("[错误]redis配置错误，请指定模式，参数:[redis.mode]！");
        throw new CacheStarterException(CacheStarterCode.CONFIG_ERROR.getCode(),"[starter][cache][错误]redis配置错误，请指定模式，参数:[redis.mode]！");
    }

    /**
     * 设置连接池信息
     * @return
     */
    private JedisPoolConfig buildJedisPoolConfig(){
        // 获得连接池配置对象，设置配置项
        JedisPoolConfig config = new JedisPoolConfig();
        // 最大连接数
        config.setMaxTotal(redisProperties.getMaxTotal());
        // 最大空闲连接数
        config.setMaxIdle(redisProperties.getMaxIdle());
        // 最小空闲连接数
        config.setMinIdle(redisProperties.getMinIdle());
        // 最大等待毫秒数
        config.setMaxWaitMillis(redisProperties.getMaxWaitMillis());
        return config;
    }
}
