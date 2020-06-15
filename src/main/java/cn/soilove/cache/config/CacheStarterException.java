package cn.soilove.cache.config;

import lombok.Data;

/**
 * 件异常
 *
 * @author: Chen GuoLin
 * @create: 2020-04-11 12:41
 **/
@Data
public class CacheStarterException extends RuntimeException {

    public CacheStarterException() {
        super();
    }

    public CacheStarterException(String msg) {
        super(msg);
    }
}
