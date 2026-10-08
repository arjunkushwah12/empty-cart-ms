package com.emptycart.registry.controller;

import com.emptycart.platform.commons.model.ApiResponse;
import com.emptycart.platform.commons.model.InstanceInfo;
import com.emptycart.platform.commons.model.RegisterRequest;
import com.emptycart.registry.service.RegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/registry")
public class RegistryController{

    private final RegistryService service;

    public RegistryController(RegistryService service){
        this.service = service;
    }

    @PostMapping("/register")
    public ApiResponse<InstanceInfo>register(@RequestBody RegisterRequest req){
        return ApiResponse.ok(service.register(req));
    }

    @PutMapping("/heartbeat/{instanceId}")
    public ResponseEntity<Void>heartbeat(@PathVariable String instanceId){
        return service.
                heartbeat(instanceId)?ResponseEntity
                .ok()
                .build()
                :ResponseEntity
                .notFound()
                .build();
    }
    @DeleteMapping("/deregister/{instanceId}")
    public ResponseEntity<Void> deregister(@PathVariable String instanceId) {
        return service.deregister(instanceId) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/instances/{serviceName}")
    public ApiResponse<List<InstanceInfo>> instances(@PathVariable String serviceName) {
        return ApiResponse.ok(service.getInstances(serviceName));
    }

    @GetMapping("/services")
    public ApiResponse<Map<String, List<InstanceInfo>>> services() {
        return ApiResponse.ok(service.getAll());
    }


}