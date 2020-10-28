package cn.soilove.cache.annotations;

import java.lang.annotation.*;

/**
 * redis缓存注解
 * @author: Chen GuoLin
 * @create: 2020-10-26 19:32
 **/
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EasyRedisCache {

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
     * 缓存Class
     * @return
     */
    Class classz();

    /**
     * 是否为集合 - 默认false
     * @return
     */
    boolean array() default false;

    /**
     * 超时时间 - 单位:秒
     * @return
     */
    int timeout() default 60;

    /**
     * 空值缓存-超时时间 - 单位:秒
     * @return
     */
    int timeout4none() default 5;
}
