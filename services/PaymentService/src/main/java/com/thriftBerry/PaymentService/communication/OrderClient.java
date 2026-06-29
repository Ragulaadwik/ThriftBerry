package com.thriftBerry.PaymentService.communication;

import com.thriftBerry.PaymentService.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "OrderService")
public interface OrderClient {

      @GetMapping("/order/{orderId}")
      OrderResponse getOrder(@PathVariable Long orderId);
}
