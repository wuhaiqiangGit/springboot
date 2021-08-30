package com.whq.mvc.common.annotation;

import java.lang.annotation.*;

/**
 * @Description: 记录入参、出参策略，日志级别
 * @Author: whq
 * @Date: 2021/8/30 23:59
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RecordParameters {

    LogLevel loglevel() default LogLevel.DEBUG;
    Strategy strategy() default Strategy.INPUT_OUTPUT;


    /**
     * @Description: 日志级别
     * @Author: whq
     * @Date: 2021/8/31 0:01
     */
    enum LogLevel{
        DEBUG,INFO,WARN
    }

    /**
     * @Description: 日志策略，出入参数
     * @Author: whq
     * @Date: 2021/8/31 0:02
     */
    enum Strategy{
        INPUT,OUTPUT,INPUT_OUTPUT
    }
}
