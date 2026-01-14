package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.feignclient.ShoppingStoreFeignClient;
import ru.yandex.practicum.request.SetProductQuantityStateRequest;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-store")
public class ShoppingStoreController implements ShoppingStoreFeignClient {

    private final ShoppingStoreService service;

    @Override
    public ProductDto createNewProduct(@Valid @RequestBody ProductDto productDto) {
        log.info("Создание нового товара: {}", productDto);
        ProductDto newProductDto = service.createNewProduct(productDto);
        log.info("Создан новый товар: {}", newProductDto);
        return newProductDto;
    }

    @Override
    public ProductDto updateProduct(@Valid @RequestBody ProductDto productDto) {
        log.info("Обновление товара: {}", productDto);
        ProductDto updatedProductDto = service.updateProduct(productDto);
        log.info("Обновлен товар: {}", updatedProductDto);
        return updatedProductDto;
    }

    @Override
    public ProductDto getProduct(@NotNull @PathVariable UUID productId) {
        log.info("Получение информации о товаре с id: {}", productId);
        ProductDto dto = service.getProductById(productId);
        log.info("Получена информация о товаре с id: {}", dto);
        return dto;
    }

    @Override
    public Page<ProductDto> getProducts(@NotNull @RequestParam("category") ProductCategory category,
                                        @Valid @SpringQueryMap Pageable pageable) {
        log.info("Получение списка товаров по типу: {}", category);
        Page<ProductDto> products = service.getProducts(category, pageable);
        log.info("Cписок товаров по типу получен: {}", products);
        return products;
    }

    @Override
    public boolean removeProductFromStore(@NotNull @RequestBody UUID productId) {
        log.info("Удаление товара с id: {}", productId);
        boolean delete = service.deleteProduct(productId);
        log.info("Удален товар с id: {}", productId);
        return delete;
    }

    @Override
    public boolean setProductQuantityState(@Valid @ModelAttribute SetProductQuantityStateRequest request) {
        log.info("Установка статуса товара: {}", request);
        boolean set = service.setQuantityState(request);
        log.info("Установлен статус: {}", request);
        return set;
    }
}
