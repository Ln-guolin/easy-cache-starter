package cn.soilove.cache.aspect;

import cn.soilove.cache.annotations.EasyIdempotent;
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
 * redis幂等控制切面
 *
 * @author: Chen GuoLin
 * @create: 2020-10-26 17:14
 **/
@Order(-1)
@Slf4j
@Aspect
@Component
public class IdempotentAspect extends SpELAspectHandler {

    @Autowired
    private RedisService redisService;

    /**
     * 幂等控制
     * @param joinPoint
     * @param annotation
     * @return
     */
    @Around("@annotation(cn.soilove.cache.annotations.EasyIdempotent) && @annotation(annotation)")
    public Object easyLocalCache(ProceedingJoinPoint joinPoint, EasyIdempotent annotation){

        Method method = filterMethod(joinPoint);
        Object[] args = joinPoint.getArgs();

        // 获取表达式内容
        String key = parseSpel(method, args,annotation.key(),String.class,null);

        // 幂等控制
        return redisService.easyIdempotent(key,annotation.timeout(), () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
    }

}