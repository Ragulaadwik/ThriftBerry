package com.thriftBerry.cartService.communication;

import com.thriftBerry.cartService.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="ProductService")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductDto getProductById(@PathVariable Long id);
}
