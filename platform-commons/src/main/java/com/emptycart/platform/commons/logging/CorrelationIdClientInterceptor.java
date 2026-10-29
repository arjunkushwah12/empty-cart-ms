package com.emptycart.platform.commons.logging;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class CorrelationIdClientInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException{
        String id = MDC.get(CorrelationIdFilter.MDC_KEY);
        if(id != null){
            request.getHeaders().set(CorrelationIdFilter.HEADER,id);
        }
        return execution.execute(request,body);
    }
}
