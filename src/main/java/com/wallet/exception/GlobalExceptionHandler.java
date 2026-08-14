package com.wallet.exception;

import com.wallet.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest req) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new ErrorResponse(409, "CONFLICT", ex.getMessage(), req.getRequestURI()));
        }

        @ExceptionHandler(PhoneAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handlePhoneExists(PhoneAlreadyExistsException ex, HttpServletRequest req) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new ErrorResponse(409, "CONFLICT", ex.getMessage(), req.getRequestURI()));
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
                        HttpServletRequest req) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ErrorResponse(401, "UNAUTHORIZED", ex.getMessage(), req.getRequestURI()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                        HttpServletRequest req) {
                String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse(400, "VALIDATION_ERROR", message, req.getRequestURI()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest req) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ErrorResponse(500, "INTERNAL_ERROR", "An unexpected error occurred",
                                                req.getRequestURI()));
        }
}