package com.nexusflow.server.service;

import com.nexusflow.server.entity.Product;
import com.nexusflow.server.exception.ResourceNotFoundException;
import com.nexusflow.server.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public List<Product> getAllProducts() {
        log.debug("Fetching all products");
        return repository.findAll();
    }

    public Product getProductById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        log.debug("Fetching product with ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        log.info("Creating new product: {}", product.getName());
        return repository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        log.info("Updating product with ID: {}", id);
        
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setName(productDetails.getName());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());

        return repository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        log.info("Deleting product with ID: {}", id);
        
        if (!repository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }
        
        repository.deleteById(id);
    }
}
