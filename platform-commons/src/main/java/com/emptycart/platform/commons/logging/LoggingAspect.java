package com.emptycart.platform.commons.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggingAspect {

    private static final long SLOW_MS = 500;

    @Around("within(@org.springframework.web.bind.annotation.RestController *)"+"" +
            "|| within(@org.springframework.stereotype.Service *)")
    public Object logExecution(ProceedingJoinPoint pjp) throws Throwable {
        Logger log = LoggerFactory.getLogger(pjp.getTarget().getClass());
        String name = pjp.getSignature().getName();
        long start = System.currentTimeMillis();
        log.debug("-> {}", name);
        try{
            Object result = pjp.proceed();
            long ms = System.currentTimeMillis() - start;
            if(ms > SLOW_MS){
                log.warn("<- {} took {} ms (slow)",name,ms);
            }
            else{
                log.debug("<- {} ({} ms)",name,ms);
            }
            return result;
        }
        catch (Throwable throwable){
            log.debug("<- {} threw",name, throwable.getClass().getSimpleName());
            throw throwable; //the exception handler logs the stack trace once
        }
    }
}
