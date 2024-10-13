package com.springcloud.demo.apigateway.exceptions.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponseDTO {
    String message;
    int status;
}
