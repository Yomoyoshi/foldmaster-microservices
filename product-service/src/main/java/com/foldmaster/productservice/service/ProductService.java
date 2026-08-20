package com.foldmaster.productservice.service;

import com.foldmaster.productservice.entity.Product;
import com.foldmaster.productservice.exception.ProductNotFoundException;
import com.foldmaster.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findAllActive() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return findAllActive();
        }
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, Product updated) {
        Product existing = findById(id);
        if (updated.getName() != null) {
            existing.setName(updated.getName());
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }
        if (updated.getPrice() != null) {
            existing.setPrice(updated.getPrice());
        }
        if (updated.getImageUrl() != null) {
            existing.setImageUrl(updated.getImageUrl());
        }
        if (updated.getStockQuantity() != null) {
            existing.setStockQuantity(updated.getStockQuantity());
        }
        if (updated.getActive() != null) {
            existing.setActive(updated.getActive());
        }
        return productRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }

    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Product product = findById(id);
        product.setStockQuantity(quantity);
        productRepository.save(product);
    }
}