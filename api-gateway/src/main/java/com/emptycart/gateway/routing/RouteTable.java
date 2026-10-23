package com.emptycart.gateway.routing;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class RouteTable {

    private final Map<String, String> routes = new LinkedHashMap<>();

    public RouteTable() {
        routes.put("/api/v1/auth", "user-service");
        routes.put("/api/v1/users", "user-service");
        routes.put("/api/v1/vendors", "user-service");
    }

    public Optional<String> serviceFor(String path) {
        for (Map.Entry<String, String> e : routes.entrySet()) {
            String prefix  = e.getKey();
            if(path.equals(prefix)||path.startsWith(prefix+"/")) {
                return Optional.of(e.getValue());
            }
        }
        return Optional.empty();
    }
}
