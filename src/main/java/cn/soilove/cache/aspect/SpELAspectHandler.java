package cn.soilove.cache.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * SpEL表达式工具
 *
 * @author: Chen GuoLin
 * @create: 2020-10-27 23:21
 **/
@Component
public class SpELAspectHandler {
    /**
     * SpEL表达式解析器
     */
    protected ExpressionParser parser = new SpelExpressionParser();

    /**
     * 参数匹配器
     */
    protected LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

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
    protected  <T> T parseSpel(Method method, Object[] arguments, String spel, Class<T> clazz, T defaultResult) {
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

    /**
     * 筛选方法
     * @param joinPoint
     * @return
     */
    protected Method filterMethod(JoinPoint joinPoint) {
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
}



