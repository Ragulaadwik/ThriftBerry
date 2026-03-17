package com.thriftBerry.ProductService.serviceImpl;

import com.thriftBerry.ProductService.dto.ProductRequestDto;
import com.thriftBerry.ProductService.dto.ProductRequestOptionalFields;
import com.thriftBerry.ProductService.entity.Product;
import com.thriftBerry.ProductService.exception.ProductNotFoundException;
import com.thriftBerry.ProductService.mapper.ProductMapper;
import com.thriftBerry.ProductService.repository.ProductRepository;
import com.thriftBerry.ProductService.service.ProductService;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public Product addProduct(ProductRequestDto productRequestDto) {
        Product product = productMapper.toProduct(productRequestDto);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id,ProductRequestDto productRequestDto) {
        Product existingProduct = productRepository.findById(id).orElseThrow(()-> new ProductNotFoundException(id));
           productMapper.updateProductByDto(productRequestDto,existingProduct);
           return productRepository.save(existingProduct);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(()->new ProductNotFoundException(id));
    }

    @Override
    @Transactional
    public void deleteProductById(Long id) {
       Product product = productRepository.findById(id).orElseThrow(()->new ProductNotFoundException(id));
       productRepository.delete(product);
    }

    @Override
    @Transactional
    public Product patchProductById(Long id, ProductRequestOptionalFields request) {
        Product product = productRepository.findById(id)
                .orElseThrow(()->new ProductNotFoundException(id));

        productMapper.patchProduct(request,product);

        return productRepository.save(product);
    }
}
