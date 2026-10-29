package com.emptycart.platform.commons.model;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> errors,
        String requestId) {
    public ErrorResponse(Instant timestamp,int status, String error,String message,
                         String path, Map<String, String> errors){
        this(timestamp,status,error,message,path,errors, MDC.get("correlationId"));
    }
}