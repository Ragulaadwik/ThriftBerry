package com.thriftBerry.cartService.communication;

import com.thriftBerry.cartService.dto.AvailabilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "InventoryService")
public interface InventoryClient {
  @GetMapping("/inventory/{productId}/availability")
  AvailabilityResponse checkAvailability(@PathVariable Long productId, @RequestParam Long  quantity);
}

