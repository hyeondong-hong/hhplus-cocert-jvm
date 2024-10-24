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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

@ControllerAdvice
class RestControllerAdvice extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExceptionHandlerMethodResolver resolver;

    public RestControllerAdvice() {
        this.resolver = new ExceptionHandlerMethodResolver(this.getClass());
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResult> handleInvalidDataAccessApiUsage(
            InvalidDataAccessApiUsageException e, WebRequest request) throws Exception {

        Exception cause = (Exception) e.getMostSpecificCause();
        return invokeExceptionHandler(cause, request);
    }

    private ResponseEntity<ErrorResult> invokeExceptionHandler(Exception e, WebRequest request) throws Exception {
        Method method = resolver.resolveMethodByThrowable(e);

        if (method != null) {
            //noinspection unchecked
            return (ResponseEntity<ErrorResult>) method.invoke(this, e, request);
        }

        return handleGenericException(e, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResult> handleIllegalArgumentException(
            IllegalArgumentException e, WebRequest request) {

        return createErrorResponse(e, request, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResult> handleAuthenticationException(
            AuthenticationException e, WebRequest request) {

        return createErrorResponse(e, request, HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResult> handleAccessDeniedException(
            AccessDeniedException e, WebRequest request) {

        return createErrorResponse(e, request, HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResult> handleNotFoundExceptions(
            Exception e, WebRequest request) {

        return createErrorResponse(e, request, HttpStatus.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResult> handleIllegalStateException(
            IllegalStateException e, WebRequest request) {

        return createErrorResponse(e, request, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleGenericException(Exception e, WebRequest request) {
        logger.error("Unhandled exception:", e);
        return createErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR, "시스템 오류");
    }

    private ResponseEntity<ErrorResult> createErrorResponse(
            Exception e, WebRequest request, HttpStatus status, String message) {

        logRequestDetails(e, request);

        // Error code: E + status_code (400 Bad Request -> E400)
        ErrorResult errorResult = new ErrorResult("E" + status.value(), message);

        return ResponseEntity.status(status).body(errorResult);
    }

    private void logRequestDetails(Exception e, WebRequest request) {
        String description = request.getDescription(true);
        logger.debug("Exception occurred: {}, Request Details: {}", e.getMessage(), description);
    }
}
