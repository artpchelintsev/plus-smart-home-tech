package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.dto.ApiError;
import ru.yandex.practicum.exceptions.NoOrderFoundException;
import ru.yandex.practicum.exceptions.NotEnoughInfoInOrderToCalculateException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoOrderFoundException.class)
    protected ApiError handleNoOrderFound(RuntimeException ex) {

        log.warn("Заказ не найден: {}", ex.getMessage());

        return ApiError.fromException(ex, HttpStatus.NOT_FOUND);
    }


    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(NotEnoughInfoInOrderToCalculateException.class)
    protected ApiError handleNotEnoughInfoInOrderToCalculate(RuntimeException ex) {

        log.warn("Недостаточно информации для расчёта: {}", ex.getMessage());

        return ApiError.fromException(ex, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiError handleGenericException(Exception ex, WebRequest request) {
        log.error("Internal server error occurred at URI: {} with message: {}", request.getDescription(false),
                ex.getMessage(), ex);

        return ApiError.fromException(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
