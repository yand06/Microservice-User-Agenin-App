package com.jdt16.agenin.users.controller.handler;

import com.jdt16.agenin.users.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.users.dto.response.RestApiResponse;
import com.jdt16.agenin.users.dto.response.RestApiResponseError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestControllerAdviceHandler {

    /**
     * ✅ Handle Validation Exception dengan auto-detect @JsonProperty
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {

        log.warn("Validation error occurred");

        Map<String, Serializable> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField(); // default = nama field Java
            String errorMessage = fieldError.getDefaultMessage();

            try {
                // ✅ Auto-detect @JsonProperty annotation using reflection
                Class<?> targetClass = ex.getBindingResult().getTarget().getClass();
                Field field = targetClass.getDeclaredField(fieldName);
                com.fasterxml.jackson.annotation.JsonProperty jsonProp =
                        field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);

                if (jsonProp != null && !jsonProp.value().isEmpty()) {
                    fieldName = jsonProp.value(); // Gunakan nama dari @JsonProperty
                }
            } catch (Exception ignored) {
            }

            errors.put(fieldName, errorMessage);
        }

        RestApiResponseError error = RestApiResponseError.builder()
                .restAPIResponseRequestError(errors)
                .build();

        RestApiResponse<Void> apiResponse = RestApiResponse.<Void>builder()
                .restAPIResponseCode(BAD_REQUEST.value())
                .restAPIResponseMessage("Validasi gagal")
                .restAPIResponseResults(null)
                .restAPIResponseError(error)
                .build();

        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
    }

    /**
     * Handle IllegalStateException - User already has referral code
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RestApiResponse<Void>> handleIllegalStateException(
            IllegalStateException ex) {

        log.warn("IllegalStateException: {}", ex.getMessage());

        Map<String, Serializable> errorDetails = new HashMap<>();
        errorDetails.put("tipe", "KONFLIK");

        RestApiResponseError error = RestApiResponseError.builder()
                .restAPIResponseRequestError(errorDetails)
                .build();

        RestApiResponse<Void> response = RestApiResponse.<Void>builder()
                .restAPIResponseCode(CONFLICT.value())
                .restAPIResponseMessage(ex.getMessage())
                .restAPIResponseResults(null)
                .restAPIResponseError(error)
                .build();

        return ResponseEntity.status(CONFLICT).body(response);
    }

    /**
     * Handle ResourceNotFoundException - User not found
     */
    @ExceptionHandler(CoreThrowHandlerException.class)
    public ResponseEntity<RestApiResponse<Void>> handleResourceNotFoundException(
            CoreThrowHandlerException ex) {

        log.error("ResourceNotFoundException: {}", ex.getMessage());

        Map<String, Serializable> errorDetails = new HashMap<>();
        errorDetails.put("tipe", "TIDAK_DITEMUKAN");

        RestApiResponseError error = RestApiResponseError.builder()
                .restAPIResponseRequestError(errorDetails)
                .build();

        RestApiResponse<Void> response = RestApiResponse.<Void>builder()
                .restAPIResponseCode(NOT_FOUND.value())
                .restAPIResponseMessage(ex.getMessage())
                .restAPIResponseResults(null)
                .restAPIResponseError(error)
                .build();

        return ResponseEntity.status(NOT_FOUND).body(response);
    }

    /**
     * Handle IllegalArgumentException - Invalid input
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RestApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        log.warn("IllegalArgumentException: {}", ex.getMessage());

        Map<String, Serializable> errorDetails = new HashMap<>();
        errorDetails.put("tipe", "PERMINTAAN_TIDAK_VALID");

        RestApiResponseError error = RestApiResponseError.builder()
                .restAPIResponseRequestError(errorDetails)
                .build();

        RestApiResponse<Void> response = RestApiResponse.<Void>builder()
                .restAPIResponseCode(BAD_REQUEST.value())
                .restAPIResponseMessage(ex.getMessage())
                .restAPIResponseResults(null)
                .restAPIResponseError(error)
                .build();

        return ResponseEntity.status(BAD_REQUEST).body(response);
    }

    /**
     * Handle NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<RestApiResponse<Void>> handleNullPointerException(
            NullPointerException ex) {

        log.error("NullPointerException occurred", ex);

        Map<String, Serializable> errorDetails = new HashMap<>();
        errorDetails.put("tipe", "KESALAHAN_SERVER");

        RestApiResponseError error = RestApiResponseError.builder()
                .restAPIResponseRequestError(errorDetails)
                .build();

        RestApiResponse<Void> response = RestApiResponse.<Void>builder()
                .restAPIResponseCode(INTERNAL_SERVER_ERROR.value())
                .restAPIResponseMessage("Terjadi kesalahan pada server")
                .restAPIResponseResults(null)
                .restAPIResponseError(error)
                .build();

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<RestApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return handleAnyThrowable(ex);
    }

    /**
     * Fallback: Handle all other exceptions
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<RestApiResponse<Void>> handleAnyThrowable(Throwable ex) {
        Map<String, Serializable> errorDetails = new HashMap<>();
        errorDetails.put("tipe", "KESALAHAN_TIDAK_TERDUGA");
        log.info("INFORMATION: {}", ex.getMessage());

        RestApiResponseError error = RestApiResponseError.builder()
                .restAPIResponseRequestError(errorDetails)
                .build();

        RestApiResponse<Void> response = RestApiResponse.<Void>builder()
                .restAPIResponseCode(INTERNAL_SERVER_ERROR.value())
                .restAPIResponseMessage("Terjadi kesalahan yang tidak terduga")
                .restAPIResponseResults(null)
                .restAPIResponseError(error)
                .build();

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
    }
}
