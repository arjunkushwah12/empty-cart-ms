package com.emptycart.platform.commons.discovery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@EnableScheduling
@ConditionalOnProperty(prefix = "registry",name="url")
public class RegistryClientAutoConfiguration {

    @Bean
    public RegistryClient registryClient(
    @Value("${registry.url}") String url,
    @Value("${spring.application.name}") String name,
    @Value("${registry.instance-host:localhost}") String host,
    @Value("${server.port:8080}") int port){
        return new RegistryClient(RestClient.create(),url,name,host,port);
    }
}
