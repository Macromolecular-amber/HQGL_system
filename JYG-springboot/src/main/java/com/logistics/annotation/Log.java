package com.logistics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解：标注在 Controller 方法上，由 AOP 切面自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    /** 模块：GC/CL/GY/ST/SYS/PAY */
    String module() default "";

    /** 操作描述 */
    String operation() default "";

    /** 操作类型：LOGIN/QUERY/ADD/UPDATE/DELETE/APPROVE/EXPORT */
    String type() default "QUERY";
}
