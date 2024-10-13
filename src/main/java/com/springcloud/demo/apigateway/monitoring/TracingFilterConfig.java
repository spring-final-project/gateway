package com.springcloud.demo.apigateway.monitoring;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.springcloud.demo.apigateway.exceptions.SimpleException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TracingFilterConfig implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        Segment segment = AWSXRay.beginSegment("Gateway");

        String xRayHeader = TracingUtils.getXRayHeader();

        // Add header to request
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-Amzn-Trace-Id", xRayHeader)
                .build();

        // Add metadata to segment of trace
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("method", request.getMethod().name());
        requestInfo.put("url", request.getURI().toString());
        segment.putHttp("request", requestInfo);
        Map<String, Object> responseInfo = new HashMap<>();

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doOnSuccess(aVoid -> {
                    ServerHttpResponse response = exchange.getResponse();

                    responseInfo.put("status", response.getStatusCode() != null ? response.getStatusCode().value() : 500);

//                    Handle errors of routes services
                    if ((Integer) responseInfo.get("status") >= 400 && (Integer) responseInfo.get("status") < 500) {
                        segment.setError(true);
                    }
                    if ((Integer) responseInfo.get("status") >= 500) {
                        segment.setFault(true);
                    }
                })
//                Handle errors on this gateway or web client requests
                .doOnError(throwable -> {
                    if (throwable instanceof SimpleException simpleException && simpleException.getStatus() < 500) {
                        responseInfo.put("status", simpleException.getStatus());
                        segment.setError(true);
                        segment.setFault(false);
                        segment.setMetadata(Map.of("exception", Map.of("message", simpleException.getMessage())));
                    } else {
                        segment.setFault(true);
                        segment.addException(throwable);
                    }
                })
                .doOnTerminate(() -> {
                    segment.putHttp("response", responseInfo);
                    AWSXRay.endSegment();
                });
    }
}
