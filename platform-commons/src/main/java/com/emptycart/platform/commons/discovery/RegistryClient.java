package com.emptycart.platform.commons.discovery;

import com.emptycart.platform.commons.model.ApiResponse;
import com.emptycart.platform.commons.model.InstanceInfo;
import com.emptycart.platform.commons.model.RegisterRequest;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

public class RegistryClient {

    private static  final Logger log = LoggerFactory.getLogger(RegistryClient.class);

    private final RestClient http;
    private final String registryUrl;
    private  final String serviceName;
    private final String host;
    private final int port;
    private volatile  String instanceId;

    public RegistryClient(RestClient http, String registryUrl, String serviceName, String host, int port) {
        this.http = http;
        this.registryUrl = registryUrl;
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }

    @EventListener(ApplicationReadyEvent.class)
    public  void  register(){

        try{
            ApiResponse<InstanceInfo> resp = http.post()
                    .uri(registryUrl+"/registry/register")
                    .body(new RegisterRequest(serviceName,host,port))
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<InstanceInfo>>() {});

            instanceId = resp.data().instanceId();
            log.info("Registered  as {}",instanceId);
        }
        catch (Exception e){
            log.warn("Registry unavailable, will retry: {}",e.getMessage());
        }
    }

    @Scheduled(fixedDelay=10_000)
    public void heartbeat(){
        if(instanceId == null){
            register();
            return;
        }
        try {
            http.put().uri(registryUrl+"/registry/heartbeat/{id}",instanceId)
                    .retrieve()
                    .toBodilessEntity();
        }
        catch (HttpClientErrorException.NotFound e){
            log.info("Registry forgot us, re-registering");
            register();
        }
        catch (Exception e){
            log.warn("heartbeat failed: {}",e.getMessage());
        }
    }

    @PreDestroy
    public void deregister() {
        if (instanceId == null) return;
        try {
            http.delete().uri(registryUrl + "/registry/deregister/{id}", instanceId)
                    .retrieve().toBodilessEntity();
            log.info("Deregistered {}", instanceId);
        } catch (Exception e) {
            log.warn("Deregister failed: {}", e.getMessage());
        }
    }

}
