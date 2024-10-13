package com.springcloud.demo.apigateway.exceptions;

import com.springcloud.demo.apigateway.exceptions.dto.ErrorResponseDTO;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class HandlerExceptions {

    @ExceptionHandler(SimpleException.class)
    public ResponseEntity<ErrorResponseDTO> handleSimpleException(SimpleException e) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .message(e.getMessage())
                .status(e.getStatus())
                .build();

        return ResponseEntity.status(e.getStatus()).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleServiceUnavailableException(NotFoundException e) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .message(e.getMessage())
                .status(e.getStatusCode().value())
                .build();

        return ResponseEntity.status(e.getStatusCode().value()).body(response);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleWebClientRequestException(WebClientRequestException e) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponseDTO> handleWebClientResponseException(WebClientResponseException e) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
    }
}
