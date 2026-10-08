package com.emptycart.platform.commons.model;

public record InstanceInfo(String instanceId, String serviceName, String host , int port) {

    public String baseUrl()
    {
        return "http://" + host + ":" + port;
    }
}


