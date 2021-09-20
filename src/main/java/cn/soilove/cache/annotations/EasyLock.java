package cn.soilove.cache.annotations;

import java.lang.annotation.*;

/**
 * redis分布式锁注解
 * @author: Chen GuoLin
 * @create: 2020-10-26 19:32
 **/
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EasyLock {
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
    int timeout() default 60;

    /**
     * 申请锁等待时间 - 单位:秒
     * @return
     */
    int applytimeout() default 60;

    /**
     * 是否自旋 - 默认非自旋
     * @return
     */
    boolean spin() default false;
}
