package com.thriftBerry.ProductService.controller;

import com.thriftBerry.ProductService.dto.ProductRequestDto;
import com.thriftBerry.ProductService.dto.ProductRequestOptionalFields;
import com.thriftBerry.ProductService.entity.Product;
import com.thriftBerry.ProductService.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody @Valid ProductRequestDto productRequestDto) {
        Product product = productService.addProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProductById(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestDto productRequestDto) {

        return ResponseEntity.ok(productService.updateProduct(id, productRequestDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> patchProductById(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestOptionalFields request) {

        return ResponseEntity.ok(productService.patchProductById(id, request));
    }
}
