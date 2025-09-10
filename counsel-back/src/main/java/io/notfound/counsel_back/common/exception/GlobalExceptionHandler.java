package io.notfound.counsel_back.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getErrorCode().getStatus().value());
        body.put("error", ex.getErrorCode().name());
        body.put("message", ex.getErrorCode().getMessage());

        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(body);
    }

    @ExceptionHandler(Exception.class) // 예상 못한 모든 에러
    public ResponseEntity<?> handleException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value());
        body.put("error", ErrorCode.INTERNAL_SERVER_ERROR.name());
        body.put("message", ex.getMessage());

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(body);
    }
}
