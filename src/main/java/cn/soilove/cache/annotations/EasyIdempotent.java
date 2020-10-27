package cn.soilove.cache.annotations;

import java.lang.annotation.*;

/**
 * redis幂等控制注解
 * @author: Chen GuoLin
 * @create: 2020-10-26 19:32
 **/
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EasyIdempotent {
    /**
     * 缓存key - spel表达式
     * <pre>
     * 示例：
     *  key="#id"
     *  key="#user.id"
     *  key="'name:' + #user.name"
     * </pre>
     * @return
     */
    String key();

    /**
     * 超时时间 - 单位:秒
     * @return
     */
    long timeout() default 60;
}
