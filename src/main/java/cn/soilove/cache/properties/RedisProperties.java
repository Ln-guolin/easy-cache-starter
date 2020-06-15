package cn.soilove.cache.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * redis配置
 *
 * @author: Chen GuoLin
 * @create: 2020-04-14 16:58
 **/
@Data
@ConfigurationProperties(prefix = RedisProperties.PREFIX )
public class RedisProperties {

    public static final String PREFIX = "redis";

    /**
     * 模式： single-单点，cluster-集群，sentinel-主从
     */
    private String mode = "single";

    /**
     * 地址配置，根据模式进行不同的配置：
     * single - 单个ip
     * sentinel / cluster - 英文逗号分隔的多个ip:端口，如：127.0.0.1:63791,127.0.0.1:63792
     */
    private String host = "localhost";

    /**
     * sentinel模式参数 - masterName
     */
    private String masterName;

    /**
     * single模式参数 - 端口
     */
    private Integer port = 6379;

    /**
     * 数据库
     */
    private Integer database = 0;

    /**
     * 密码
     */
    private String password;

    /**
     * 读取超时时间(毫秒)
     */
    private Integer timeOut = 5000;

    /**
     * 最大连接数
     */
    private Integer maxTotal = 200;

    /**
     * 最大空闲连接数
     */
    private Integer maxIdle = 8;

    /**
     * 最小空闲连接数
     */
    private Integer minIdle = 0;

    /**
     * 获取连接时的最大等待毫秒数
     */
    private Long maxWaitMillis = -1L;



}
