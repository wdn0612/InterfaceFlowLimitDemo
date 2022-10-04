package com.dntech.interfaceflowlimitdemo.service;

import com.dntech.interfaceflowlimitdemo.enums.LimitType;
import com.dntech.interfaceflowlimitdemo.exception.ServiceException;
import com.dntech.interfaceflowlimitdemo.facade.FlowLimiter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

@Aspect
@Component
public class RateLimiterAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private RedisScript<Long> limitScript;

    @Before(value = "@annotation(com.dntech.interfaceflowlimitdemo.facade.FlowLimiter) && args(flowLimiter)")
    public void doBefore(JoinPoint point, FlowLimiter flowLimiter) throws Throwable {
        String key = flowLimiter.key();
        int time = flowLimiter.time();
        int count = flowLimiter.count();

        String combineKey = getCombineKey(flowLimiter, point);
        log.info("key: " + combineKey);
        List<Object> keys = Collections.singletonList(combineKey);
        log.info("List of keys : " + keys);
        try {
            Long number = redisTemplate.execute(limitScript, keys, count, time);
            if (number==null || number.intValue() > count) {
                throw new ServiceException("Please try again later.");
            }
            log.info("Limit request'{}', Current request '{}', Redis key'{}'", count, number.intValue(), key);
        } catch (ServiceException e) {
            throw e;
        }
    }

    public String getCombineKey(FlowLimiter rateLimiter, JoinPoint point) {
        StringBuffer stringBuffer = new StringBuffer(rateLimiter.key());
        if (rateLimiter.limitType() == LimitType.IP) {
            stringBuffer.append("-<ip_address>-");
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuffer.toString();
    }

}
