package ru.yandex.practicum.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ProductRepository;
import ru.yandex.practicum.request.SetProductQuantityStateRequest;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingStoreServiceImpl implements ShoppingStoreService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductDto createNewProduct(ProductDto productDto) {
        log.info("Создание нового товара: {}", productDto);
        Product product = repository.save(mapper.mapToProduct(productDto));
        log.info("Создан новый товар: {}", product);
        return mapper.mapToDto(product);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        checkExistsProduct(productDto.getProductId());
        log.info("Обновление товара: {}", productDto);
        Product product = findProductById(productDto.getProductId());
        mapper.update(productDto, product);
        Product updatedProduct = repository.save(product);
        log.info("Обновлен товар: {}", updatedProduct);
        return mapper.mapToDto(updatedProduct);
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        checkExistsProduct(productId);
        log.info("Получение информации о товаре с id: {}", productId);
        Product product = findProductById(productId);
        log.info("Получена информация о товаре: {}", product);
        return mapper.mapToDto(product);
    }

    @Override
    public Page<ProductDto> getProducts(ProductCategory productCategory, Pageable pageable) {
        log.info("Получение списка товаров по категории: {}", productCategory);
        Page<ProductDto> products = repository.findAllByProductCategory(productCategory, pageable)
                .map(mapper::mapToDto);
        log.info("Получен список товаров по категории: {}", products);
        return products;
    }

    @Override
    @Transactional
    public boolean deleteProduct(UUID productId) {
        log.info("Удаление товара с id: {}", productId);
        Product product = findProductById(productId);
        product.setProductState(ProductState.DEACTIVATE);
        repository.save(product);
        log.info("Удален товар: {}", product);
        return true;
    }

    @Override
    @Transactional
    public boolean setQuantityState(SetProductQuantityStateRequest request) {
        checkExistsProduct(request.getProductId());
        log.info("Установка статуса: {}", request);
        Product product = findProductById(request.getProductId());
        product.setQuantityState(request.getQuantityState());
        repository.save(product);
        log.info("Установлен статус: {}", request.getQuantityState());
        return true;
    }

    private Product findProductById(UUID productId) {
        checkExistsProduct(productId);
        return repository.findById(productId).get();
    }

    private void checkExistsProduct(UUID productId) {
        boolean exists = productRepository.existsById(productId);

        if (!exists) {
            log.error("Товар с id: {} не найден", productId);
            throw new NotFoundException();
        }
    }
}
