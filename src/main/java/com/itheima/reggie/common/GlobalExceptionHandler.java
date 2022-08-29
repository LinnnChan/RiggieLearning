package com.itheima.reggie.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理, advice注解参数意为：为所有加了RestController和Controller注解的类进行处理
 * responsebody注解：将实体转成json，方便传给前端
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理异常方法,注解参数为异常类型
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        //判断是否包含异常关键字
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" "); //按空格分割错误信息
            String msg = split[2] + "已存在"; //取到重复的用户名
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /**
     * 处理业务异常
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }

}
