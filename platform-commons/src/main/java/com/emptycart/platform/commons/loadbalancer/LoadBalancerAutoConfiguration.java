package com.emptycart.platform.commons.loadbalancer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@ConditionalOnProperty(prefix = "registry", name = "url")
public class LoadBalancerAutoConfiguration {

    @Bean
    public LoadBalancer loadBalancer(@Value("${registry.url}") String url) {
        return new LoadBalancer(RestClient.create(), url);
    }

    @Bean
    public RestClient loadBalancedRestClient(LoadBalancer lb) {
        return RestClient.builder()
                .requestInterceptor(new LoadBalancerInterceptor(lb))
                .build();
    }
}