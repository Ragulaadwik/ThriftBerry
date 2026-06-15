package com.thriftBerry.orderService.communication;

import com.thriftBerry.orderService.dto.cart.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "CartService")
public interface CartClient {

    @GetMapping("/cart")
    CartResponse getCart(@RequestParam Long userId);

}
