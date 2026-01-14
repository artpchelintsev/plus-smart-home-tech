package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.dto.ApiError;
import ru.yandex.practicum.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exceptions.SpecifiedProductAlreadyInWarehouseException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    protected ApiError handleProductAlreadyExistsOnWarehouse(RuntimeException ex) {
        log.error("400 BAD_REQUEST - товар уже зарегистрирован на складе.");
        return ApiError.fromException(ex, HttpStatus.BAD_REQUEST);


    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouseException.class)
    protected ApiError handleNotEnoughProductsInStock(RuntimeException ex) {
        log.error("400 BAD_REQUEST - Недостаточно товаров на складе для добавления в корзину.");
        return ApiError.fromException(ex, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoProductInfoFound(RuntimeException e) {
        log.error("404 NOT_FOUND - {}", e.getMessage());
        return ApiError.fromException(e, HttpStatus.NOT_FOUND);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiError handleGenericException(Exception ex, WebRequest request) {
        log.error("Internal server error occurred at URI: {} with message: {}", request.getDescription(false),
                ex.getMessage(), ex);
        return ApiError.fromException(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
