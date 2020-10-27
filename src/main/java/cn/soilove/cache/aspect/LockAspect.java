package cn.soilove.cache.aspect;

import cn.soilove.cache.annotations.EasyLock;
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
 * redis锁切面
 *
 * @author: Chen GuoLin
 * @create: 2020-10-26 17:14
 **/
@Order(-1)
@Slf4j
@Aspect
@Component
public class LockAspect extends SpELAspectHandler {

    @Autowired
    private RedisService redisService;

    /**
     * 获取并设置本地缓存
     * @param joinPoint
     * @param annotation
     * @return
     */
    @Around("@annotation(cn.soilove.cache.annotations.EasyLock) && @annotation(annotation)")
    public Object easyLocalCache(ProceedingJoinPoint joinPoint, EasyLock annotation){

        Method method = filterMethod(joinPoint);
        Object[] args = joinPoint.getArgs();

        // 获取表达式内容
        String key = parseSpel(method, args,annotation.key(),String.class,null);

        // 自旋
        if(annotation.spin()){
            return redisService.easySpinLock(key,(int)annotation.timeout(), () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        }
        // 非自旋
        else{
            return redisService.easyLock(key,(int)annotation.timeout(), () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        }
    }

}