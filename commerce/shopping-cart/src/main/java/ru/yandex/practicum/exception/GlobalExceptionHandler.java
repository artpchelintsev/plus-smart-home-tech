package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.dto.ApiError;
import ru.yandex.practicum.exceptions.ProductNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProductNotFoundException.class)
    protected ApiError handleNoProductInfoFound(RuntimeException ex) {

        log.warn("Товар не найден: {}", ex.getMessage());

        return ApiError.fromException(ex, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiError handleGenericException(Exception ex, WebRequest request) {
        log.error("Internal server error occurred at URI: {} with message: {}", request.getDescription(false),
                ex.getMessage(), ex);

        return ApiError.fromException(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
