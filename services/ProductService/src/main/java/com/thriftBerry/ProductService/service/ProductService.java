package com.thriftBerry.ProductService.service;

import com.thriftBerry.ProductService.dto.ProductRequestDto;
import com.thriftBerry.ProductService.dto.ProductRequestOptionalFields;
import com.thriftBerry.ProductService.entity.Product;

import java.util.List;

public interface ProductService {

    Product addProduct(ProductRequestDto productRequestDto);

    Product updateProduct(Long id,ProductRequestDto productRequestDto);

   List<Product> getAllProducts();

   Product getProductById(Long id);

   void deleteProductById(Long id);

   Product patchProductById(Long id, ProductRequestOptionalFields productRequestOptionalFields);
}
