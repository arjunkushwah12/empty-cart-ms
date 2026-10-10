package com.emptycart.platform.commons.loadbalancer;
import com.emptycart.platform.commons.model.ApiResponse;
import com.emptycart.platform.commons.model.InstanceInfo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LoadBalancer {

    private static final long CACHE_TTL_MS = 10_000;

    private record Cached(List<InstanceInfo> instances, long fetchedAt) {}

    private final Function<String, List<InstanceInfo>> fetcher;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final Set<String> unhealthy = ConcurrentHashMap.newKeySet();

    public LoadBalancer(RestClient http, String registryUrl) {
        this(name -> {
            ApiResponse<List<InstanceInfo>> resp = http.get()
                    .uri(registryUrl + "/registry/instances/{name}", name)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<List<InstanceInfo>>>() {});
            return resp == null || resp.data() == null ? List.<InstanceInfo>of() : resp.data();
        });
    }

    public LoadBalancer(Function<String, List<InstanceInfo>> fetcher) {
        this.fetcher = fetcher;
    }

    public InstanceInfo choose(String serviceName) {
        List<InstanceInfo> healthy = instances(serviceName).stream()
                .filter(i -> !unhealthy.contains(i.instanceId()))
                .toList();
        if (healthy.isEmpty()) {
            throw new IllegalStateException("No healthy instance for " + serviceName);
        }
        int n = counters.computeIfAbsent(serviceName, k -> new AtomicInteger()).getAndIncrement();
        return healthy.get(Math.floorMod(n, healthy.size()));
    }

    public void markUnhealthy(InstanceInfo instance) {
        unhealthy.add(instance.instanceId());
    }

    private List<InstanceInfo> instances(String serviceName) {
        Cached c = cache.get(serviceName);
        long now = System.currentTimeMillis();
        if (c != null && now - c.fetchedAt() <= CACHE_TTL_MS) {
            return c.instances();
        }
        try {
            List<InstanceInfo> fresh = fetcher.apply(serviceName);
            unhealthy.removeIf(id -> id.startsWith(serviceName + ":"));  // cache refresh clears marks
            cache.put(serviceName, new Cached(fresh, now));
            return fresh;
        } catch (RuntimeException e) {
            if (c != null) return c.instances();   // registry down: use stale list
            throw e;
        }
    }
}