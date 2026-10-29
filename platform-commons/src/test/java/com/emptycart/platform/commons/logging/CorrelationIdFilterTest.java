package com.emptycart.platform.commons.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    private MockHttpServletResponse call(String headerValue) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/ping");
        if (headerValue != null) {
            request.addHeader(CorrelationIdFilter.HEADER, headerValue);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void generatesIdWhenHeaderMissing() throws Exception {
        String id = call(null).getHeader(CorrelationIdFilter.HEADER);
        assertNotNull(id);
        assertEquals(36, id.length());
    }

    @Test
    void keepsValidClientId() throws Exception {
        assertEquals("client-id-12345", call("client-id-12345").getHeader(CorrelationIdFilter.HEADER));
    }

    @Test
    void replacesInvalidClientId() throws Exception {
        String id = call("bad id!").getHeader(CorrelationIdFilter.HEADER);
        assertNotEquals("bad id!", id);
        assertEquals(36, id.length());
    }

    @Test
    void clearsMdcAfterRequest() throws Exception {
        call(null);
        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }
}