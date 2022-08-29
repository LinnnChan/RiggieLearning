package com.itheima.reggie.common;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果类R
 * @param <T>
 */
@Data
public class R<T> {

    private Integer code; //编码：1成功，0和其它数字为失败 (和前端约好,vue脚本中的res变量就是 )

    private String msg; //错误信息，例如“登陆失败”

    private T data; //数据，例如实体Employee，前端会把它转成json

    private Map map = new HashMap(); //动态数据

    public static <T> R<T> success(T object) { //返回的是泛型
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
