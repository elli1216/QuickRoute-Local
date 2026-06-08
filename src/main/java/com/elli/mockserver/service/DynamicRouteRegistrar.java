package com.elli.mockserver.service;

import com.elli.mockserver.exception.RouteRegistrationException;
import com.elli.mockserver.handler.MockRequestHandler;
import com.elli.mockserver.model.RouteDefinition;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@Component
public class DynamicRouteRegistrar {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private MockRequestHandler mockHandler;

    private Method handlerMethod;

    public DynamicRouteRegistrar() {
        try {
            handlerMethod = MockRequestHandler.class.getMethod("handle",
                    HttpServletRequest.class, HttpServletResponse.class);
        } catch (NoSuchMethodException e) {
            throw new RouteRegistrationException("Failed to find handler method", e);
        }
    }

    public void registerRoute(String mockId, RouteDefinition route) {
        String fullPath = "/mock/" + mockId + toSpringPattern(route.getPathPattern());
        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(fullPath)
                .methods(RequestMethod.valueOf(route.getMethod().toUpperCase()))
                .build();
        handlerMapping.registerMapping(mappingInfo, mockHandler, handlerMethod);
    }

    public void unregisterRoute(String mockId, RouteDefinition route) {
        String fullPath = "/mock/" + mockId + toSpringPattern(route.getPathPattern());
        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(fullPath)
                .methods(RequestMethod.valueOf(route.getMethod().toUpperCase()))
                .build();
        handlerMapping.unregisterMapping(mappingInfo);
    }

    private static String toSpringPattern(String pattern) {
        return pattern.replaceAll(":([a-zA-Z_][a-zA-Z0-9_]*)", "{$1}");
    }
}
