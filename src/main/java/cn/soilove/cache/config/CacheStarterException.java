package cn.soilove.cache.config;

import cn.soilove.cache.utils.CacheStarterCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 件异常
 *
 * @author: Chen GuoLin
 * @create: 2020-04-11 12:41
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheStarterException extends RuntimeException {

    private int code;
    private String msg;

    public CacheStarterException(String msg) {
        super(msg);
        this.code = CacheStarterCode.ERROR.getCode();
        this.msg = msg;
    }

    public CacheStarterException(CacheStarterCode resCode){
        this.code = resCode.getCode();
        this.msg = resCode.getMsg();
    }

    public String parseStr(){
        return new StringBuilder().append("code=").append(this.code).append(",msg=").append(msg).toString();
    }
}
