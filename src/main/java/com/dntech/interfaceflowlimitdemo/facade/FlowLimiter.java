package com.dntech.interfaceflowlimitdemo.facade;

import com.dntech.interfaceflowlimitdemo.enums.LimitType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FlowLimiter {

    String key() default "flow_limit:";

    int time() default 20;

    int count() default 5;

    LimitType limitType() default LimitType.DEFAULT;
}
