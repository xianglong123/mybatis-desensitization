package com.cas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xiang_long
 * @version 1.0
 * @date 2022/5/30 10:18 上午
 * @desc 加解密，属性注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Confidential {

    /**
     * 是否启动属性校验
     * @return
     */
    boolean value() default false;

    /**
     * 属性的正则匹配
     */
    String regular() default "";

    /**
     * 属性的长度匹配, 要求属性长度<=len()
     * @return
     */
    int len() default Integer.MAX_VALUE;

}
