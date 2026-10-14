package com.emptycart.platform.commons.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import com.emptycart.platform.commons.model.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Object>build(HttpStatusCode status, String message, WebRequest request, Map<String, String> errors ) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String reason = resolved != null ? resolved.getReasonPhrase() : "Error";
        String path = request.getDescription(false).replace("uri=","");
        ErrorResponse body = new ErrorResponse(Instant.now(),status.value(),reason,message,path,errors);
        return ResponseEntity.status(status).body(body);
    }

    /* Bean Validation failures (@Valid)*/

    @Override
    protected  ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,HttpHeaders headers, HttpStatusCode status, WebRequest request){
        Map<String,String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> errors.put(f.getField(),f.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST,"Validation failed",request,errors);
    }

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<Object> notFound(NotFoundException ex, WebRequest request)
    {
        return build(HttpStatus.NOT_FOUND,ex.getMessage(),request,null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> conflict(ConflictException e, WebRequest r) {
        return build(HttpStatus.CONFLICT, e.getMessage(), r, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> badRequest(BadRequestException e, WebRequest r) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), r, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> unauthorized(UnauthorizedException e, WebRequest r) {
        return build(HttpStatus.UNAUTHORIZED, e.getMessage(), r, null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> forbidden(ForbiddenException e, WebRequest r) {
        return build(HttpStatus.FORBIDDEN, e.getMessage(), r, null);
    }

    // Fallback: never leak internal details to the client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpected(Exception e, WebRequest r) {
        logger.error("Unexpected error", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", r, null);
    }
}
