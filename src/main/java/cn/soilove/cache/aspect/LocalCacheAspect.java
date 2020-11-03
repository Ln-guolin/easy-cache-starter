package cn.soilove.cache.aspect;

import cn.soilove.cache.annotations.EasyLocalCache;
import cn.soilove.cache.annotations.EasyLocalCacheClean;
import cn.soilove.cache.utils.CaffeineCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 本地缓存切面
 *
 * @author: Chen GuoLin
 * @create: 2020-10-26 17:14
 **/
@Order(-2)
@Slf4j
@Aspect
@Component
public class LocalCacheAspect extends SpELAspectHandler {

    /**
     * 获取并设置缓存
     * @param joinPoint
     * @param annotation
     * @return
     */
    @Around("@annotation(cn.soilove.cache.annotations.EasyLocalCache) && @annotation(annotation)")
    public Object easyCache(ProceedingJoinPoint joinPoint, EasyLocalCache annotation) {

        Method method = filterMethod(joinPoint);
        Object[] args = joinPoint.getArgs();

        // 获取表达式内容
        String key = parseSpel(method, args,annotation.key(),String.class,null);

        // 从缓存获取
        return CaffeineCacheUtils.get(annotation.namespace(),key,annotation.timeout(),() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
    }

    /**
     * 清空缓存
     * @param joinPoint
     * @param annotation
     * @return
     * @throws Throwable
     */
    @Around("@annotation(cn.soilove.cache.annotations.EasyLocalCacheClean) && @annotation(annotation)")
    public Object easyCacheClean(ProceedingJoinPoint joinPoint, EasyLocalCacheClean annotation) throws Throwable {

        Method method = filterMethod(joinPoint);
        Object[] args = joinPoint.getArgs();

        // 清空缓存空间
        if (StringUtils.isEmpty(annotation.key())){
            CaffeineCacheUtils.del(annotation.namespace());
        }
        // 清空缓存空间的指定缓存key
        else {
            // 获取表达式内容
            String key = parseSpel(method, args,annotation.key(),String.class,null);
            CaffeineCacheUtils.del(annotation.namespace(),key);
        }

        return joinPoint.proceed();
    }
}