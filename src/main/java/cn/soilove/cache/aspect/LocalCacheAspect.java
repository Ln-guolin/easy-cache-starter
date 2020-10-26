package cn.soilove.cache.aspect;

import cn.soilove.cache.annotations.EasyLocalCache;
import cn.soilove.cache.annotations.EasyLocalCacheClean;
import cn.soilove.cache.utils.CaffeineCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 本地缓存切面
 *
 * @author: Chen GuoLin
 * @create: 2020-10-26 17:14
 **/
@Order(-1)
@Slf4j
@Aspect
@Component
public class LocalCacheAspect {

    /**
     * SpEL表达式解析器
     */
    private ExpressionParser parser = new SpelExpressionParser();

    /**
     * 参数匹配器
     */
    private LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    /**
     * 获取并设置本地缓存
     * @param joinPoint
     * @param annotation
     * @return
     */
    @Around("@annotation(cn.soilove.cache.annotations.EasyLocalCache) && @annotation(annotation)")
    public Object easyLocalCache(ProceedingJoinPoint joinPoint, EasyLocalCache annotation) {

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
     * 清空本地缓存
     * @param joinPoint
     * @param annotation
     * @return
     * @throws Throwable
     */
    @Around("@annotation(cn.soilove.cache.annotations.EasyLocalCacheClean) && @annotation(annotation)")
    public Object easyLocalCacheClean(ProceedingJoinPoint joinPoint, EasyLocalCacheClean annotation) throws Throwable {

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

    /**
     * 筛选方法
     * @param joinPoint
     * @return
     */
    private Method filterMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method =  signature.getMethod();
        // 获取真实的调用对象，防止注解加在接口或抽象方法上
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
        if (targetClass == null && joinPoint.getTarget() != null) {
            targetClass = joinPoint.getTarget().getClass();
        }
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        // 防止桥接方法
        return BridgeMethodResolver.findBridgedMethod(specificMethod);
    }

    /**
     * 解析 spel 表达式
     *
     * @param method    方法
     * @param arguments 参数
     * @param spel      表达式
     * @param clazz     返回结果的类型
     * @param defaultResult 默认结果
     * @return 执行spel表达式后的结果
     */
    private <T> T parseSpel(Method method, Object[] arguments, String spel, Class<T> clazz, T defaultResult) {
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], arguments[len]);
        }
        try {
            Expression expression = parser.parseExpression(spel);
            return expression.getValue(context, clazz);
        } catch (Exception e) {
            return defaultResult;
        }
    }
}