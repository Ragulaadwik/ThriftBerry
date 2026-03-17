package com.thriftBerry.ProductService.exception;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(Long id) {
        super("Product with Id:"+id+"not found !");
    }
}
