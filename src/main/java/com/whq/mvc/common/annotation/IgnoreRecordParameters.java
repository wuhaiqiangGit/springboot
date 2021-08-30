package com.whq.mvc.common.annotation;

import java.lang.annotation.*;

/**
 * @Description: 开关注解，忽略@RecordParameters注解
 * @Author: whq
 * @Date: 2021/8/31 0:04
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreRecordParameters {
}
