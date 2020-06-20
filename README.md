
## 缓存组件 - redis / caffeine


#### 使用方法
**1.** pom文件添加依赖
```xml
<!-- 私有缓存组件 -->
<dependency>
    <groupId>cn.soilove</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
    <version>1.1.3</version>
</dependency>
```

**2.** 在配置文件添加配置
```yaml
## 缓存数据库redis连接配置
# 模式：single-单点，cluster-集群，sentinel-主从
redis.mode=single
# 地址配置，根据模式进行不同的配置：
# single - 单个ip
# sentinel / cluster - 英文逗号分隔的多个ip:端口，如：127.0.0.1:63791,127.0.0.1:63792
redis.host=127.0.0.1
# sentinel模式参数 - masterName
redis.masterName=
# single模式参数 - 端口
redis.port=6379
# 数据库索引
redis.database=0
# 连接密码
redis.password=password123
# 连接超时时间（毫秒）
redis.timeout=5000
# 最大连接数
redis.maxTotal=200
# 最大空闲连接
redis.maxIdle=8
# 最小空闲连接
redis.minIdle=0
# 获取连接时的最大等待毫秒数
redis.maxWaitMillis=-1
```

**3.** 调用示例


redis

```java
// 注入
@Autowired
private RedisService redisService;

// redis缓存
redisService.set("key","1");

// 其他类似...

```

caffeine

```java
// 注入
@Autowired
private CaffeineService caffeineService;

// 本地缓存
String str = caffeineService.getFixed("key",() -> {return "query";});

// 其他类似...

```

redis mq

```java
// 注入
@Autowired
private RedisMQHandler redisMQHandler;

// 订阅
redisMQHandler.lpush("topic-name","处理内容字符串");


// 消费
@Scheduled(cron = "*/1 * * * * ?")
public void orderPay(){
    redisMQHandler.rpop("order-pay",(content) -> {
        log.info("【#######[mq]#######】正在处理消息：" + content);
        return null;
    });
}

// 订阅 - 延时队列
redisMQHandler.lpush4delay("topic-name","处理内容字符串",60);


// 消费 - 延时队列
@Scheduled(cron = "*/1 * * * * ?")
public void orderPay(){
    redisMQHandler.rpop4delay("order-pay",(content) -> {
        log.info("【#######[mq]#######】正在处理消息：" + content);
        return null;
    });
}



```
