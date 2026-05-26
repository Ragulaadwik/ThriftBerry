package com.thriftBerry.cartService.communication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="ProductService")
public interface ProductClient {

    @GetMapping("/products/{id}")
    Object getProductById(@PathVariable Long id);
}
