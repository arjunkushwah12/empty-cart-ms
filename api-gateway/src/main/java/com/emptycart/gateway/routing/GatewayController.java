package com.emptycart.gateway.routing;

import com.emptycart.gateway.exception.ServiceUnavailableException;
import com.emptycart.platform.commons.constants.HeaderNames;
import com.emptycart.platform.commons.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@RestController
public class GatewayController {

    private static final List<String> FORWARDED_REQUEST_HEADERS = List.of(
            HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.ACCEPT_LANGUAGE, HttpHeaders.AUTHORIZATION);

    private final RestClient client;
    private final RouteTable routes;

    public GatewayController(@Qualifier("loadBalancedRestClient") RestClient client, RouteTable routes) {
        this.client = client;
        this.routes = routes;
    }

    @RequestMapping(path = "/api/v1/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<byte[]> forward(HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body,
                                          @AuthenticationPrincipal Jwt jwt) {
        String path = request.getRequestURI();
        String service = routes.serviceFor(path)
                .orElseThrow(() -> new NotFoundException("No route for " + path));

        String query = request.getQueryString();
        URI target = URI.create("http://" + service + path + (query == null ? "" : "?" + query));

        RestClient.RequestBodySpec spec = client.method(HttpMethod.valueOf(request.getMethod()))
                .uri(target)
                .headers(h -> copyRequestHeaders(request, jwt, h));
        if (body != null && body.length > 0) {
            spec.body(body);
        }

        ResponseEntity<byte[]> response;
        try {
            response = spec.retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> { })   // pass 4xx and 5xx through
                    .toEntity(byte[].class);
        } catch (IllegalStateException | ResourceAccessException e) {
            throw new ServiceUnavailableException("Cannot reach " + service, e);
        }

        HttpHeaders out = new HttpHeaders();
        copyIfPresent(response.getHeaders(), out, HttpHeaders.CONTENT_TYPE);
        copyIfPresent(response.getHeaders(), out, HttpHeaders.LOCATION);
        return ResponseEntity.status(response.getStatusCode()).headers(out).body(response.getBody());
    }

    private void copyRequestHeaders(HttpServletRequest request, Jwt jwt, HttpHeaders target) {
        // allow-list only: client-sent X-User-* headers are never forwarded
        for (String name : FORWARDED_REQUEST_HEADERS) {
            String value = request.getHeader(name);
            if (value != null) {
                target.set(name, value);
            }
        }
        // identity headers come only from the verified token
        if (jwt != null) {
            target.set(HeaderNames.USER_ID, jwt.getSubject());
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null && !roles.isEmpty()) {
                target.set(HeaderNames.USER_ROLE, roles.get(0));
            }
        }
    }

    private void copyIfPresent(HttpHeaders from, HttpHeaders to, String name) {
        String value = from.getFirst(name);
        if (value != null) {
            to.set(name, value);
        }
    }
}