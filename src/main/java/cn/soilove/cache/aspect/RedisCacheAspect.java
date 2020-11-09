package cn.soilove.cache.aspect;

import cn.soilove.cache.annotations.EasyRedisCache;
import cn.soilove.cache.annotations.EasyRedisCacheClean;
import cn.soilove.cache.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * redis缓存切面
 *
 * @author: Chen GuoLin
 * @create: 2020-10-26 17:14
 **/
@Order(-1)
@Slf4j
@Aspect
@Component
public class RedisCacheAspect extends SpELAspectHandler {

    @Autowired
    private RedisService redisService;

    /**
     * 获取并设置缓存
     * @param joinPoint
     * @param annotation
     * @return
     */
    @Around("@annotation(cn.soilove.cache.annotations.EasyRedisCache) && @annotation(annotation)")
    public Object easyCache(ProceedingJoinPoint joinPoint, EasyRedisCache annotation) {

        Method method = filterMethod(joinPoint);
        Object[] args = joinPoint.getArgs();

        // 获取表达式内容
        String key = parseSpel(method, args,annotation.key(),String.class,null);

        // 集合缓存
        if(annotation.array()){
            return redisService.easyCache4Array(key,annotation.timeout(),annotation.timeout4none(),annotation.classz(),() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        }
        // 字符串缓存
        else if(annotation.classz().equals(String.class)){
            return redisService.easyCache(key,annotation.timeout(),annotation.timeout4none(),() -> {
                try {
                    return (String) joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        }
        // 对象缓存
        else{
            return redisService.easyCache(key,annotation.timeout(),annotation.timeout4none(),annotation.classz(),() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        }
    }

    /**
     * 清空缓存
     * @param joinPoint
     * @param annotation
     * @return
     * @throws Throwable
     */
    @Around("@annotation(cn.soilove.cache.annotations.EasyRedisCacheClean) && @annotation(annotation)")
    public Object easyCacheClean(ProceedingJoinPoint joinPoint, EasyRedisCacheClean annotation) throws Throwable {

        Method method = filterMethod(joinPoint);
        Object[] args = joinPoint.getArgs();

        // 获取表达式内容
        String key = parseSpel(method, args,annotation.key(),String.class,null);

        // 业务代码执行
        Object obj = joinPoint.proceed();

        // 清理缓存
        redisService.del(key);

        return obj;
    }
}