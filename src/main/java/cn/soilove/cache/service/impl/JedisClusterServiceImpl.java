package cn.soilove.cache.service.impl;

import cn.soilove.cache.config.CacheStarterException;
import cn.soilove.cache.service.RedisService;
import cn.soilove.cache.utils.CacheStarterCode;
import cn.soilove.cache.utils.ExceptionStringUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JedisCluster操作
 *
 * @author: Chen GuoLin
 * @create: 2020-04-14 17:29
 **/
@Slf4j(topic = "[starter][cache][Cluster]")
public class JedisClusterServiceImpl implements RedisService {

    @Autowired
    private JedisCluster jedis;

    private <R> R doCommand(Function<JedisCluster,R> function){
        try{
            return function.apply(jedis);
        } catch (Exception e) {
            throw new CacheStarterException("[错误]redis命令执行异常，msg="+ ExceptionStringUtils.getStackTraceAsString(e));
        }
    }

    @Override
    public boolean exists(String key) {
        return doCommand(jedis -> key == null ? false : jedis.exists(key));
    }

    @Override
    public Long strlen(String key) {
        return doCommand(jedis -> key == null ? null : jedis.strlen(key));
    }


    @Override
    public void set(String key, String value) {
        doCommand(jedis -> jedis.set(key, value));
    }

    @Override
    public void set(String key, String value, int seconds) {
        doCommand(jedis -> jedis.setex(key, seconds, value));
    }

    @Override
    public boolean setnx(String key, String value, int seconds) {
        return doCommand(jedis -> {
            SetParams setParams = new SetParams();
            // key存在不做处理
            setParams.nx();
            // 设置失效时间
            setParams.ex(seconds);
            String result = jedis.set(key, value,setParams);
            if ("OK".equals(result)) {
                // 加锁成功
                return true;
            }
            return false;
        });
    }


    @Override
    public String get(String key) {
        return doCommand(jedis -> key == null ? null : jedis.get(key));
    }

    @Override
    public List<String> mget(String... keys) {
        return doCommand(jedis -> jedis.mget(keys));
    }

    @Override
    public ScanResult<String> scan(int index, String regx) {
        return doCommand(jedis -> {
            ScanParams scanParams = new ScanParams();
            scanParams.match(regx);
            /*查询总数*/
            scanParams.count(Integer.MAX_VALUE);
            return jedis.scan(String.valueOf(index), scanParams);
        });
    }

    @Override
    public ScanResult<String> scan(String regx) {
        return doCommand(jedis -> {
            ScanParams scanParams = new ScanParams();
            scanParams.match(regx);
            /*查询总数*/
            scanParams.count(Integer.MAX_VALUE);
            return jedis.scan(ScanParams.SCAN_POINTER_START, scanParams);
        });
    }

    @Override
    public Long persist(String key) {
        return doCommand(jedis -> key == null ? null : jedis.persist(key));
    }

    @Override
    public Long ttl(String key) {
        return doCommand(jedis -> jedis.ttl(key));
    }

    @Override
    public Long del(String key) {
        return doCommand(jedis -> key == null ? null : jedis.del(key));
    }

    @Override
    public Long incr(String key) {
        return doCommand(jedis -> key == null ? null : jedis.incr(key));
    }

    @Override
    public Long incrBy(String key,long increment) {
        return doCommand(jedis -> key == null ? null : jedis.incrBy(key,increment));
    }

    @Override
    public Long incrEX(String key, long seconds){
        String script = "local res = redis.call('incr',KEYS[1]); " +
                        "redis.call('expire',KEYS[1],ARGV[1]); " +
                        "return res;";
        Object result = this.eval(script, Collections.singletonList(key), Collections.singletonList(String.valueOf(seconds)));
        return Long.valueOf(result.toString());
    }

    @Override
    public void hset(String key, String field, String value) {
        doCommand(jedis -> jedis.hset(key, field, value));
    }

    @Override
    public String hget(String key, String field) {
        return doCommand(jedis -> jedis.hget(key, field));
    }

    @Override
    public Long hdel(String key, String field) {
        return doCommand(jedis -> jedis.hdel(key, field));
    }

    @Override
    public Long hincrBy(String key, String field,long increment) {
        return doCommand(jedis -> jedis.hincrBy(key, field,increment));
    }

    @Override
    public Boolean hexists(String key, String field) {
        return doCommand(jedis -> jedis.hexists(key, field));
    }

    @Override
    public Set<String> hkeys(String key) {
        return doCommand(jedis -> jedis.hkeys(key));
    }

    @Override
    public void hmset(String key, Map<String, String> map) {
        doCommand(jedis -> jedis.hmset(key, map));
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return doCommand(jedis -> jedis.hmget(key, fields));
    }

    @Override
    public Long hlen(String key) {
        return doCommand(jedis -> key == null ? Long.valueOf(0) : jedis.hlen(key));
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return doCommand(jedis -> key == null ? null : jedis.hgetAll(key));
    }

    @Override
    public Long zadd(String key,double score,String member){
        return doCommand(jedis -> key == null ? null : jedis.zadd(key,score,member));
    }

    @Override
    public Double zincrby(String key,double incr,String member){
        return doCommand(jedis -> key == null ? null : jedis.zincrby(key,incr,member));
    }

    @Override
    public Set<String> zrange(String key, Long start, Long stop){
        return doCommand(jedis -> key == null ? null : jedis.zrange(key,start,stop));
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max){
        return doCommand(jedis -> key == null ? null : jedis.zrangeByScore(key,min,max));
    }

    @Override
    public Set<String> zrevrange(String key, Long start, Long stop){
        return doCommand(jedis -> key == null ? null : jedis.zrevrange(key,start,stop));
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double min, double max){
        return doCommand(jedis -> key == null ? null : jedis.zrevrangeByScore(key,min,max));
    }

    @Override
    public Long zrem(String key, String ... members){
        return doCommand(jedis -> key == null ? null : jedis.zrem(key,members));
    }

    @Override
    public Long zcount(String key,  double min, double max){
        return doCommand(jedis -> key == null ? null : jedis.zcount(key,min,max));
    }

    @Override
    public Long zrank(String key,  String member){
        return doCommand(jedis -> key == null ? null : jedis.zrank(key,member));
    }

    @Override
    public Boolean zexists(String key,  String member){
        return zrank(key,member) != null;
    }

    @Override
    public Long sadd(String key,String member){
        return doCommand(jedis -> key == null ? null : jedis.sadd(key,member));
    }

    @Override
    public Set<String> smembers(String key,String member){
        return doCommand(jedis -> key == null ? null : jedis.smembers(key));
    }


    @Override
    public Set<String> sinter(String ... keys){
        return doCommand(jedis -> keys == null ? null : jedis.sinter(keys));
    }

    @Override
    public Long srem(String key,String ... members){
        return doCommand(jedis -> (key == null || members == null) ? null : jedis.srem(key,members));
    }

    @Override
    public String spop(String key){
        return doCommand(jedis -> key == null ? null : jedis.spop(key));
    }

    @Override
    public Set<String> spop(String key,Long count){
        return doCommand(jedis -> key == null ? null : jedis.spop(key,count));
    }

    @Override
    public String srandmember(String key){
        return doCommand(jedis -> key == null ? null : jedis.srandmember(key));
    }

    @Override
    public List<String> srandmember(String key,Integer count){
        return doCommand(jedis -> key == null ? null : jedis.srandmember(key,count));
    }

    @Override
    public void lpush(String key, String... strings) {
        doCommand(jedis -> jedis.lpush(key, strings));
    }

    @Override
    public void rpush(String key, String... strings) {
        doCommand(jedis -> jedis.rpush(key, strings));
    }

    @Override
    public Long llen(String key) {
        return doCommand(jedis -> key == null ? Long.valueOf(0) : jedis.llen(key));
    }

    @Override
    public String lindex(String key, long index) {
        return doCommand(jedis -> jedis.lindex(key, index));
    }

    @Override
    public List<String> lrangeAll(String key) {
        return lrange(key, 0, -1);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return doCommand(jedis -> jedis.lrange(key, start, end));
    }

    @Override
    public String lpop(String key) {
        return doCommand(jedis -> key == null ? null : jedis.lpop(key));
    }

    @Override
    public String rpop(String key) {
        return doCommand(jedis -> key == null ? null : jedis.rpop(key));
    }


    @Override
    public List<String> blpop(String key, int seconds) {
        return doCommand(jedis -> jedis.blpop(seconds, key));
    }

    @Override
    public void expire(String key, int seconds) {
        doCommand(jedis -> jedis.expire(key, seconds));
    }

    @Override
    public boolean lock(String key, int seconds) {
        key = LOCK_KEY_PREFIX + key;
        String finalKey = key;
        return doCommand(jedis -> {
            // 判断锁是否能加锁，成功设置表示加锁成功，利用set nx效果，如果存在相同值则返回失败的特性
            SetParams setParams = new SetParams();
            // key存在不做处理
            setParams.nx();
            // 设置失效时间
            setParams.ex(seconds);
            String result = jedis.set(finalKey, "1",setParams);
            return "OK".equals(result);
        });
    }

    @Override
    public boolean lockSpin(String key, int seconds) {
        try {
            key = LOCK_KEY_PREFIX + key;
            long startTime = System.currentTimeMillis();

            long waitInMilliSeconds = ((long) seconds) * 1000;

            try {
                // 不断轮询锁，直到超时
                while (System.currentTimeMillis() - startTime < waitInMilliSeconds) {
                    // 判断锁是否能加锁，成功设置表示加锁成功，利用set nx效果，如果存在相同值则返回失败的特性
                    boolean res = setnx(key,"1",seconds);
                    if(res){
                        return true;
                    }
                    // 轮训间隔10毫秒
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                throw new RuntimeException("spinLock error", e);
            }

            return lockSpin(key, seconds);
        } catch (Exception e) {
            log.error("redis 分布式等待锁-加锁异常，异常信息：", e);
            throw new CacheStarterException("命令执行异常！");
        }
    }

    @Override
    public boolean unLock(String key) {
        key = LOCK_KEY_PREFIX + key;
        String finalKey = key;
        return doCommand(jedis -> del(finalKey) > 0);
    }

    @Override
    public Object eval(String script){
        return doCommand(jedis -> jedis.eval(script,0));
    }

    @Override
    public Object eval(String script,int keyCount,String ... params){
        return doCommand(jedis -> jedis.eval(script,keyCount,params));
    }

    @Override
    public Object eval(String script,List<String> keys, List<String> args){
        return doCommand(jedis -> jedis.eval(script,keys,args));
    }

    @Override
    public Long geoadd(String key, double longitude, double latitude, String member){
        return doCommand(jedis -> jedis.geoadd(key,longitude,latitude,member));
    }

    @Override
    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap){
        return doCommand(jedis -> jedis.geoadd(key,memberCoordinateMap));
    }

    @Override
    public List<GeoCoordinate> geopos(String key, String ... members){
        return doCommand(jedis -> jedis.geopos(key,members));
    }

    @Override
    public Double geodist(String key, String member1, String member2){
        return doCommand(jedis -> jedis.geodist(key,member1,member2));
    }

    @Override
    public Double geodist(String key, String member1, String member2, GeoUnit unit){
        return doCommand(jedis -> jedis.geodist(key,member1,member2,unit));
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit){
        return doCommand(jedis -> jedis.georadius(key,longitude,latitude,radius,unit));
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param){
        return doCommand(jedis -> jedis.georadius(key,longitude,latitude,radius,unit));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key,String member, double radius, GeoUnit unit){
        return doCommand(jedis -> jedis.georadiusByMember(key,member,radius,unit));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member,  double radius, GeoUnit unit, GeoRadiusParam param){
        return doCommand(jedis -> jedis.georadiusByMember(key,member,radius,unit));
    }

    @Override
    public List<String> geohash(String key, String ... members){
        return doCommand(jedis -> jedis.geohash(key,members));
    }

    @Override
    public Long georem(String key, String ... members){
        return doCommand(jedis -> key == null ? null : jedis.zrem(key,members));
    }

    @Override
    public String easyCache(String key, int seconds, int nullSeconds, Supplier<String> supplier){
        // 优先读取缓存
        String res = get(key);
        if(!StringUtils.isEmpty(res)){
            // 判断是否为空值
            if(NULL_VALUE.equals(res)){
                return null;
            }
            return res;
        }

        // 执行查询
        res = supplier.get();
        // 空值处理
        if(StringUtils.isEmpty(res)){
            set(key,NULL_VALUE,nullSeconds);
            return null;
        }
        // 有值设置
        set(key,res,seconds);
        return res;
    }

    @Override
    public <R> R easyCache(String key, int seconds,int nullSeconds,Class<R> classz, Supplier<R> supplier){

        // 优先读取缓存
        String res = easyCache(key,seconds,nullSeconds,() -> {
            R r = supplier.get();
            if(r != null){
                return JSON.toJSONString(r);
            }
            return null;
        });

        if(StringUtils.isEmpty(res)){
            return null;
        }
        return JSON.parseObject(res, classz);
    }

    @Override
    public <T> T easyLock(String key, int seconds, Supplier<T> supplier){
        // 此任务正在执行，跳过
        boolean lock = this.lock(key,seconds);
        if(!lock){
            throw new CacheStarterException(CacheStarterCode.LOCK_ERROR);
        }
        try{
            return supplier.get();
        }finally {
            // 执行完成，移除key
            this.unLock(key);
        }
    }

    @Override
    public <T> T easySpinLock(String key, int seconds, Supplier<T> supplier){
        // 此任务正在执行，跳过
        boolean lock = this.lockSpin(key,seconds);
        if(!lock){
            throw new CacheStarterException(CacheStarterCode.LOCK_ERROR);
        }
        try{
            return supplier.get();
        }finally {
            // 执行完成，移除key
            this.unLock(key);
        }
    }

    @Override
    public <T> T easyIdempotent(String key, int seconds, Supplier<T> supplier){
        // 标记执行状态
        key = String.format("idpt:%s",key);

        // 通过lua脚本实现，原子自增1，并设置过期时间
        Long res = this.incrEX(key,seconds);
        if(res > 1){
            throw new CacheStarterException(CacheStarterCode.IDEMPOTENT_ERROR);
        }
        try{
            // 执行关键逻辑
            return supplier.get();
        } catch (RuntimeException e){
            this.del(key);
            throw e;
        } catch (Exception e){
            this.del(key);
            throw e;
        } catch (Throwable e){
            this.del(key);
            throw e;
        }
    }
}
