package org.example.bookstore.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LogAspect {
    @Around("execution(* org.example.bookstore.controller.*.*(..))")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        Object result = joinPoint.proceed();
        logger.info("请求参数: {}", Arrays.toString(joinPoint.getArgs()));
        return result;
    }
}