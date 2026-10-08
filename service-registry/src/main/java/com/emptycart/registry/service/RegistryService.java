//package com.emptycart.registry.service;
//
//import com.emptycart.platform.commons.model.InstanceInfo;
//import com.emptycart.platform.commons.model.RegisterRequest;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class RegistryService {
//
//    private static final long TIMEOUT_MS = 30_000;
//
//    private static class Entry {
//        final InstanceInfo info;
//        volatile long lastSeen = System.currentTimeMillis();
//        Entry(InstanceInfo info) {
//            this.info = info;
//        }
//    }
//
//    //serviceName -> (instanceId -> entry)
//    private final Map<String, Map<String, Entry>> registry = new ConcurrentHashMap<>();
//    public InstanceInfo register(RegisterRequest request) {
//        String id = request.serviceName() + ":"+ request.host()+":"+request.port();
//        InstanceInfo info = new InstanceInfo(id, request.serviceName(),  request.host(), request.port());
//        registry.computeIfAbsent(request.serviceName(), k->new ConcurrentHashMap<>())
//                .put(id,new Entry(info));
//        return info;
//    }
//
//    public boolean heartbeat(String instanceId) {
//        Entry e = find(instanceId);
//
//    }
//
//}
package com.emptycart.registry.service;

import com.emptycart.platform.commons.model.InstanceInfo;
import com.emptycart.platform.commons.model.RegisterRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistryService {

    private static final long TIMEOUT_MS = 30_000;

    private static class Entry {
        final InstanceInfo info;
        volatile long lastSeen = System.currentTimeMillis();
        Entry(InstanceInfo info) { this.info = info; }
    }

    // serviceName -> (instanceId -> entry)
    private final Map<String, Map<String, Entry>> registry = new ConcurrentHashMap<>();

    public InstanceInfo register(RegisterRequest req) {
        String id = req.serviceName() + ":" + req.host() + ":" + req.port();
        InstanceInfo info = new InstanceInfo(id, req.serviceName(), req.host(), req.port());
        registry.computeIfAbsent(req.serviceName(), k -> new ConcurrentHashMap<>())
                .put(id, new Entry(info));   // same host+port replaces old entry
        return info;
    }

    public boolean heartbeat(String instanceId){
        Entry e = find(instanceId);
        if (e == null) {
            return false;
        }
        e.lastSeen = System.currentTimeMillis();
        return true;
    }
    public boolean deregister(String instanceId){
        Map<String, Entry> m = registry.get(serviceNameOf(instanceId));
        return m!=null && m.remove(instanceId) != null;
    }

    public List<InstanceInfo> getInstances(String serviceName){
        Map<String, Entry> m = registry.get(serviceName);
        if(m==null){
            return List.of();
        }
        return m.values().stream().map(e ->e.info).toList();
    }

    public Map<String, List<InstanceInfo>> getAll(){
        Map<String, List<InstanceInfo>> out = new HashMap<>();
        registry.forEach((name,m)-> out.put(name,m.values().stream().map(e ->e.info).toList()));
        return out;
    }

    @Scheduled(fixedDelay = 10_000)
    public void evictExpired(){
        long now = System.currentTimeMillis();
        registry.values().forEach(m -> m.values().removeIf(e ->now-e.lastSeen>TIMEOUT_MS));
    }

    private Entry find(String instanceId){
        Map<String, Entry> m = registry.get(serviceNameOf(instanceId));
        return m==null?null:m.get(instanceId);
    }

    private String serviceNameOf(String instanceId){
        int i =  instanceId.indexOf(':');
        return i<0?instanceId:instanceId.substring(0,i);
    }
}