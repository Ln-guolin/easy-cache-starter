<p align="center">
	<a href="https://github.com/Ln-guolin/easy-cache-starter"><img src="https://soilove.oss-cn-hangzhou.aliyuncs.com/32e/pro-mall/easy-cache-starter.png" width="350px"></a>
</p>
<p align="center">
	<strong>一个整合了Redis缓存和Caffeine本地缓存的SpringBoot Starter</strong>
</p>
<p align="center">
	<a target="_blank" href="https://github.com/Ln-guolin/easy-cache-starter/blob/master/LICENSE">
		<img src="https://img.shields.io/:license-Apache2.0-blue.svg" />
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-8+-green.svg" />
	</a>
	<a target="_blank" href="https://gitter.im/pro-32e/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge">
		<img src="https://badges.gitter.im/pro-32e/community.svg" />
	</a>
	<a href="https://github.com/Ln-guolin/easy-cache-starter">
        <img src="https://img.shields.io/github/repo-size/Ln-guolin/easy-cache-starter"/>
    </a>
	<a href="https://github.com/Ln-guolin/easy-cache-starter">
        <img src="https://img.shields.io/github/issues-raw/Ln-guolin/easy-cache-starter"/>
    </a>
    <a href="https://github.com/Ln-guolin/easy-cache-starter">
        <img src="https://img.shields.io/github/v/release/Ln-guolin/easy-cache-starter?include_prereleases"/>
    </a>
	<a href="https://github.com/Ln-guolin/easy-cache-starter">
        <img src="https://img.shields.io/github/stars/Ln-guolin/easy-cache-starter?style=social"/>
    </a>
</p>


<p align="center">
    在此鸣谢Jetbrains
	<a target="_blank" href="https://www.jetbrains.com/?from=easy-cache-starter">
		<img src="https://soilove.oss-cn-hangzhou.aliyuncs.com/32e/jetbrains/jetbrains.svg" height="30"/>
	</a>
	为本项目提供免费正版工具支持!
</p>


## 项目介绍

本项目整合了Redis缓存和Caffeine本地缓存，以及布隆过滤器，并实现了基于他们的常用接口、注解和功能，将其封装成为SpringBoot Starter，以便项目快速使用。


##### Redis缓存
- 支持单例，主从，集群三种模式部署的Redis快速接入
- 提供redis基本操作命令
- 提供mq操作接口，实现了实时和延迟2种消息队列
- 提供easy快捷操作功能，实现了"快速缓存"、"锁"、"自旋锁"、"幂等控制"接口
- 提供geo位置计算
- 提供注解实现方式，并支持spel表达式

##### Caffeine缓存
- 提供动态失效缓存接口
- 提供固定时间缓存接口
- 提供注解实现方式，并支持spel表达式

##### 布隆过滤器
- redis布隆过滤实现
- 本地布隆过滤实现(bloomFilterUtils)

##### 支持注解

| 注解                   | 类型    | 备注             |
| ---------------------- | ------- | ---------------- |
| @EasyIdempotent       | method   | 幂等控制     |
| @EasyLock             | method   | 分布式锁：自旋与非自旋     |
| @EasyLocalCache       | method   | caffeine本地缓存获取和设置，支持spel表达式     |
| @EasyLocalCacheClean  | method   | caffeine本地缓存清理     |
| @EasyRedisCache       | method   | redis缓存获取和设置，支持spel表达式     |
| @EasyRedisCacheClean  | method   | redis缓存清理     |

##  项目结构
```lua
easy-cache-starter
├── LICENSE
├── README.md
├── pom.xml
├── easy-cache-starter.iml
└── src
    └── main
        ├── java
        │   └── cn
        │       └── soilove
        │           └── cache
        │               ├── annotations
        │               │   ├── EasyIdempotent.java
        │               │   ├── EasyLocalCache.java
        │               │   ├── EasyLocalCacheClean.java
        │               │   ├── EasyLock.java
        │               │   ├── EasyRedisCache.java
        │               │   └── EasyRedisCacheClean.java
        │               ├── aspect
        │               │   ├── IdempotentAspect.java
        │               │   ├── LocalCacheAspect.java
        │               │   ├── LockAspect.java
        │               │   ├── RedisCacheAspect.java
        │               │   └── SpELAspectHandler.java
        │               ├── config
        │               │   ├── CacheAutoConfiguration.java
        │               │   └── CacheStarterException.java
        │               ├── properties
        │               │   └── RedisProperties.java
        │               ├── service
        │               │   ├── RedisService.java
        │               │   ├── handler
        │               │             ├── RedisBloomFilter.java
        │               │   │   └── RedisMQHandler.java
        │               │   └── impl
        │               │       ├── JedisClusterServiceImpl.java
        │               │       ├── JedisSentinelServiceImpl.java
        │               │       └── JedisSingleServiceImpl.java
        │               └── utils
        │                   ├── BloomFilterUtils.java
        │                   ├── CacheStarterCode.java
        │                   ├── CaffeineCacheUtils.java
        │                   ├── ExceptionStringUtils.java
        │                   └── RedisKeysEnum.java
        └── resources
            └── META-INF
                └── spring.factories

```

## 使用方法

### 引入组件
Maven方式引入：直接在工程pom.xml文件中添加如下依赖，即可使用
```xml
<!-- 缓存组件 -->
<dependency>
    <groupId>cn.soilove</groupId>
    <artifactId>easy-cache-starter</artifactId>
    <version>1.5.0</version>
</dependency>
```

### Caffeine本地缓存使用

##### 使用静态工具类
工具类：CaffeineCacheUtils
```java
// 本地缓存
String str = CaffeineCacheUtils.get("namespace","key",60,() -> {
                        return "任何内容或对象";
                    });

// 其他类似...

```

##### 使用注解
操作示例：
```java
// 获取数据并缓存
@EasyLocalCache(namespace = "area", key = "'info:' + #code", timeout = 60 * 60 * 24)

// 清空缓存空间
@EasyLocalCacheClean(namespace = "user")

// 清空指定缓存
@EasyLocalCacheClean(namespace = "area", key = "'info:' + #code")
```

表达式示例：
```
* 缓存key - spel表达式
* 示例：
*  key="#id"
*  key="#user.id"
*  key="'name:' + #user.name"
```

### Redis缓存使用

##### 配置添加
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

##### 使用注入bean方式

- redis 基本缓存

```java
// 注入
@Autowired
private RedisService redisService;

// redis缓存
redisService.set("key","1");

// 其他类似...

```



- redis mq

```java
// 注入
@Autowired
private RedisMQHandler redisMQHandler;

// 订阅
redisMQHandler.lpush("topic-name","处理内容字符串");


// 消费
@Scheduled(cron = "*/1 * * * * ?")
public void orderPay(){
    redisMQHandler.rpop("topic-name",(content) -> {
        log.info("【#######[mq]#######】正在处理消息：" + content);
        return null;
    });
}

// 订阅 - 延时队列
redisMQHandler.lpush4delay("topic-name","处理内容字符串",60);


// 消费 - 延时队列
@Scheduled(cron = "*/1 * * * * ?")
public void orderPay(){
    redisMQHandler.rpop4delay("topic-name",(content) -> {
        log.info("【#######[mq]#######】正在处理消息：" + content);
        return null;
    });
}
```

- redis 发布订阅
```java
// 订阅和消费消息
@Slf4j
@Component
public class RedisOrderPayMessageListener {

    @Autowired
    private RedisService redisService;

    @PostConstruct
    private void init(){
        ThreadPoolUtils.execute(() -> receiveMessage());
    }

    public void receiveMessage() {
        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                log.info("[message][订单支付成功]onMessage... channel:"+channel+",message:" + message);
                doit(message);
            }

            @Override
            public void onPMessage(String pattern, String channel, String message) {
                log.info("[message][订单支付成功]onPMessage... channel:"+channel+",message:" + message);
                doit(message);
            }
        };

        log.info("[message][subscribe]订阅消息渠道：order_node");
        redisService.subscribe(jedisPubSub,"order_node");
    }

    private void doit(String message){
        // 处理实际业务逻辑...
    }
}

// 发送消息
redisService.publish("order_node",orderCode);
```


- redis 简易缓存

```$java

/* 接口 */

/**
 * 简易-缓存
 * @param key 缓存key
 * @param seconds 缓存时间-秒
 * @param nullSeconds 空值缓存时间-秒
 * @param supplier Supplier接口
 * @return
 */
String easyCache(String key, int seconds, int nullSeconds, Supplier<String> supplier);

/**
 * 简易-缓存对象
 * @param key 缓存key
 * @param seconds 缓存时间-秒
 * @param nullSeconds 空值缓存时间-秒
 * @param classz 对象Class
 * @param supplier Supplier接口
 * @param <R>
 * @return
 */
<R> R easyCache(String key, int seconds,int nullSeconds,Class<R> classz, Supplier<R> supplier);

/* 调用 */

// 简易-缓存字符
String str = redisService.easyCache("key",60,5,() -> {
            // do query db
            return "";
        });

// 简易-缓存对象
UserInfo info = redisService.easyCache("key",60,5,UserInfo.class,() -> {
            // do query db
            return null;
        });
```

- redis 简易锁

```$java

// 简易锁
redisService.easyLock("key",seconds,() -> {// todo});

// 简易自旋锁
redisService.easySpinLock("key",seconds,() -> {// todo});

// 简易幂等
redisService.easyIdempotent("key",seconds,() -> {// todo});

```

- redis geo 位置计算

```angular2
// 添加元素和位置
redisService.geoadd(args ...)

// 查询元素位置信息
redisService.geopos(args ...)

// 计算元素之间的距离
redisService.geodist(args ...)

// 查找附近的元素和距离
redisService.georadius(args ...)

// 查询元素geohash字符
redisService.geohash(args ...)

// 移除元素位置
redisService.georem(args ...)
```

##### 使用注解
操作示例：
```java
// 幂等控制
@EasyIdempotent(key = "'pay:' + #order.code",timeout = 60)

// 加锁，可使用spin控制是否自旋
@EasyLock(key = "'pay:' + #order.code",timeout = 60,spin = true)

// 对象类型数据缓存
@EasyRedisCache(key = "'user:' + #user.id",classz = SysUser.class,timeout = 60,timeout4none = 5)

// 集合类型数据缓存
@EasyRedisCache(key = "'books:' + #type",classz = Book.class,array = true,timeout = 60,timeout4none = 5)

// 清空缓存
@EasyRedisCacheClean(key = "'user:' + #user.id")
```

### 布隆过滤器的使用

##### 本地过滤器
```java
// 创建过滤器
BloomFilterUtils.create("test",1000,0.00001);

// 设置元素
BloomFilterUtils.put("test","zhangsan");

// 验证是否存在
boolean exists = BloomFilterUtils.mightContain("test","zhangsan");       
```

##### redis过滤器
```java
// 注入过滤器
@Autowired
private RedisBloomFilter redisBloomFilter;

// 创建过滤器
redisBloomFilter.create("test",1000,0.0001);

// 设置元素
redisBloomFilter.put("test","zhangsan");

// 验证是否存在
boolean exists = redisBloomFilter.mightContain("test","zhangsan");
```



