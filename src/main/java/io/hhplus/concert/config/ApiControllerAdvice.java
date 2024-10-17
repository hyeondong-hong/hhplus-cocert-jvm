package io.hhplus.concert.config;

import io.hhplus.concert.config.dto.ErrorResult;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception e) {
        if (e instanceof InvalidDataAccessApiUsageException) {
            e = (Exception) ((InvalidDataAccessApiUsageException) e).getMostSpecificCause();
        }

        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResult("400", e.getMessage()));
        } else if (e instanceof AuthenticationException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResult("401", e.getMessage()));
        } else if (e instanceof AccessDeniedException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResult("403", e.getMessage()));
        } else if (e instanceof NoSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResult("404", e.getMessage()));
        } else if (e instanceof EntityNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResult("404", e.getMessage()));
        } else if (e instanceof IllegalStateException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResult("409", e.getMessage()));
        }

        logger.error("Error:", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResult("500", "시스템 오류"));
    }

}
