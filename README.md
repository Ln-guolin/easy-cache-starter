<p align="center">
	<a href="https://github.com/Ln-guolin/spring-boot-starter-cache"><img src="https://soilove.oss-cn-hangzhou.aliyuncs.com/32e/pro-mall/easy-cache-starter.png" width="350px"></a>
</p>
<p align="center">
	<strong>一个整合了Redis缓存和Caffeine本地缓存的SpringBoot Starter</strong>
</p>
<p align="center">
	<a target="_blank" href="https://github.com/Ln-guolin/spring-boot-starter-cache/blob/master/LICENSE">
		<img src="https://img.shields.io/:license-Apache2.0-blue.svg" />
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-8+-green.svg" />
	</a>
	<a target="_blank" href="https://gitter.im/pro-32e/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge">
		<img src="https://badges.gitter.im/pro-32e/community.svg" />
	</a>
	<a href="https://github.com/Ln-guolin/spring-boot-starter-cache">
        <img src="https://img.shields.io/github/repo-size/Ln-guolin/spring-boot-starter-cache"/>
    </a>
	<a href="https://github.com/Ln-guolin/spring-boot-starter-cache">
        <img src="https://img.shields.io/github/issues-raw/Ln-guolin/spring-boot-starter-cache"/>
    </a>
    <a href="https://github.com/Ln-guolin/spring-boot-starter-cache">
        <img src="https://img.shields.io/github/v/release/Ln-guolin/spring-boot-starter-cache?include_prereleases"/>
    </a>
	<a href="https://github.com/Ln-guolin/spring-boot-starter-cache">
        <img src="https://img.shields.io/github/stars/Ln-guolin/spring-boot-starter-cache?style=social"/>
    </a>
</p>


<p align="center">
    在此鸣谢Jetbrains
	<a target="_blank" href="https://www.jetbrains.com/?from=spring-boot-starter-cache">
		<img src="https://soilove.oss-cn-hangzhou.aliyuncs.com/32e/jetbrains/jetbrains.svg" height="30"/>
	</a>
	为本项目提供免费正版工具支持!
</p>


## 项目介绍

本项目整合了Redis缓存和Caffeine本地缓存，并实现了以及基于两者的常用接口和功能，将其封装成为SpringBoot Starter，以便项目快速使用。


##### Redis实现内容：
- 支持单例，主从，集群三种模式部署的Redis快速接入
- 提供redis基本操作命令
- 提供mq操作接口，实现了实时和延迟2种消息队列
- 提供easy快捷操作功能，实现了"快速缓存"、"锁"、"自旋锁"、"幂等控制"接口
- 提供geo位置计算

##### Caffeine实现内容：
- 提供动态失效缓存接口
- 提供固定时间缓存接口
- 提供注解实现方式，并支持spel表达式

## 项目结构
```lua
├── LICENSE
├── README.md
├── pom.xml
├── spring-boot-starter-cache.iml
└── src
    └── main
        ├── java
        │   └── cn
        │       └── soilove
        │           └── cache
        │               ├── annotations
        │               │   ├── EasyLocalCache.java
        │               │   └── EasyLocalCacheClean.java
        │               ├── aspect
        │               │   └── LocalCacheAspect.java
        │               ├── config
        │               │   ├── CacheAutoConfiguration.java
        │               │   └── CacheStarterException.java
        │               ├── properties
        │               │   └── RedisProperties.java
        │               ├── service
        │               │   ├── RedisService.java
        │               │   ├── handler
        │               │   │   └── RedisMQHandler.java
        │               │   └── impl
        │               │       ├── JedisClusterServiceImpl.java
        │               │       ├── JedisSentinelServiceImpl.java
        │               │       └── JedisSingleServiceImpl.java
        │               └── utils
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
    <artifactId>spring-boot-starter-cache</artifactId>
    <version>${last.version}</version>
</dependency>
```


### Caffeine本地缓存使用

##### 使用静态工具类

```java
// 本地缓存
String str = CaffeineCacheUtils.getFixed("key",() -> {return "query";});

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

##### 1，首先进行配置添加
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

##### 2，直接在代码中注入调用

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





