package com.emptycart.platform.commons.loadbalancer;

import com.emptycart.platform.commons.model.InstanceInfo;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {

    private static final int MAX_ATTEMPTS = 3;
    private final LoadBalancer lb;

    public LoadBalancerInterceptor(LoadBalancer lb) {
        this.lb = lb;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        URI original = request.getURI();
        String host = original.getHost();
        // a logical service name has no port and no dot, e.g. http://product-service/items
        if (host == null || original.getPort() != -1 || host.contains(".") || host.equals("localhost")) {
            return execution.execute(request, body);
        }
        IOException last = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            InstanceInfo instance = lb.choose(host);
            URI target = UriComponentsBuilder.fromUri(original)
                    .host(instance.host()).port(instance.port()).build(true).toUri();
            HttpRequest routed = new HttpRequestWrapper(request) {
                @Override
                public URI getURI() { return target; }
            };
            try {
                return execution.execute(routed, body);
            } catch (IOException e) {
                lb.markUnhealthy(instance);
                last = e;
            }
        }
        throw last;
    }
}