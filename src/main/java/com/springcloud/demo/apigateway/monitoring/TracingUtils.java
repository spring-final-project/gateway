package com.springcloud.demo.apigateway.monitoring;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;

public class TracingUtils {

    public static String getXRayHeader() {
        Segment segment = AWSXRay.getCurrentSegment();
        String traceId = segment.getTraceId().toString();
        String parentId = segment.getId();

        // Create header to send to microservices
        return String.format("Root=%s;Parent=%s;Sampled=1", traceId, parentId);
    }
}
