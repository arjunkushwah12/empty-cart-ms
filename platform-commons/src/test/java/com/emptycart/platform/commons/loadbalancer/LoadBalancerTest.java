package com.emptycart.platform.commons.loadbalancer;

import com.emptycart.platform.commons.model.InstanceInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancerTest {

    private static InstanceInfo inst(int port) {
        return new InstanceInfo("svc:localhost:" + port, "svc", "localhost", port);
    }

    @Test
    void roundRobinRotates() {
        LoadBalancer lb = new LoadBalancer(name -> List.of(inst(1), inst(2), inst(3)));
        assertEquals(1, lb.choose("svc").port());
        assertEquals(2, lb.choose("svc").port());
        assertEquals(3, lb.choose("svc").port());
        assertEquals(1, lb.choose("svc").port());
    }

    @Test
    void unhealthyInstanceIsSkipped() {
        LoadBalancer lb = new LoadBalancer(name -> List.of(inst(1), inst(2)));
        lb.choose("svc");                 // loads the list
        lb.markUnhealthy(inst(1));
        assertEquals(2, lb.choose("svc").port());
        assertEquals(2, lb.choose("svc").port());
    }

    @Test
    void noInstancesThrows() {
        LoadBalancer lb = new LoadBalancer(name -> List.of());
        assertThrows(IllegalStateException.class, () -> lb.choose("svc"));
    }

    @Test
    void listIsCached() {
        AtomicInteger calls = new AtomicInteger();
        LoadBalancer lb = new LoadBalancer(name -> {
            calls.incrementAndGet();
            return List.of(inst(1));
        });
        lb.choose("svc");
        lb.choose("svc");
        lb.choose("svc");
        assertEquals(1, calls.get());
    }
}