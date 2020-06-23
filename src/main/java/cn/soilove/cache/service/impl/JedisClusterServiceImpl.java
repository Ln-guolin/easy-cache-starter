package cn.soilove.cache.service.impl;

import cn.soilove.cache.config.CacheStarterException;
import cn.soilove.cache.service.RedisService;
import cn.soilove.cache.utils.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.params.SetParams;

import java.nio.charset.StandardCharsets;
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
            throw new CacheStarterException("[错误]redis命令执行异常，msg="+SerializeUtil.getStackTraceAsString(e));
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
    public void setObj(String key, Object obj) {
        doCommand(jedis -> jedis.set(key.getBytes(StandardCharsets.UTF_8), SerializeUtil.serialize(obj)));
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
    public void setObj(String key, Object obj, int seconds) {
        doCommand(jedis -> jedis.setex(key.getBytes(StandardCharsets.UTF_8), seconds, SerializeUtil.serialize(obj)));
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObj(String key, Class<T> clazz) {
        return doCommand(jedis -> (T) SerializeUtil.unSerialize(jedis.get(key.getBytes(StandardCharsets.UTF_8))));
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
    public Set<String> zrange(String key, Long start, Long stop){
        return doCommand(jedis -> key == null ? null : jedis.zrange(key,start,stop));
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max){
        return doCommand(jedis -> key == null ? null : jedis.zrangeByScore(key,min,max));
    }

    @Override
    public Long zrem(String key, String ... members){
        return doCommand(jedis -> key == null ? null : jedis.zrem(key,members));
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
    public boolean lockWait(String key, int seconds) {
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
                    // 轮训间隔50毫秒
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                throw new RuntimeException("spinLock error", e);
            }

            return lockWait(key, seconds);
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
    public String easyCache(String key, int seconds, int nullSeconds, Supplier<String> supplier){
        // 先获取key是否存在
        return doCommand(jedis -> {
            // 优先读取缓存
            String res = jedis.get(key);
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
                jedis.setex(key,nullSeconds,NULL_VALUE);
                return null;
            }
            // 有值设置
            jedis.setex(key,seconds,res);
            return res;
        });
    }

    @Override
    public <T> T easyLock(String key, int seconds, Supplier<T> supplier){
        // 此任务正在执行，跳过
        boolean lock = this.lock(key,seconds);
        if(!lock){
            throw new CacheStarterException("请求阻塞，请稍后再试！");
        }
        try{
            return supplier.get();
        }finally {
            // 执行完成，移除key
            this.unLock(key);
        }
    }

    @Override
    public <T> T easyWaitLock(String key, int seconds, Supplier<T> supplier){
        // 此任务正在执行，跳过
        boolean lock = this.lockWait(key,seconds);
        if(!lock){
            throw new CacheStarterException("请求阻塞，请稍后再试！");
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
        key = String.format("idempotent:%s",key);

        // 如果存在 <已执行> 标记，提示不能重复执行
        String res = this.get(key);
        if(StringUtils.isEmpty(res)){
            throw new CacheStarterException("请勿重复操作！");
        }

        // 设置 <已执行> 标记
        this.set(key,"1",seconds);

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
