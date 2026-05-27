package com.thriftBerry.cartService.communication;

import com.thriftBerry.cartService.dto.AvailabilityResponse;
import com.thriftBerry.cartService.dto.InventoryBookingRequest;
import com.thriftBerry.cartService.dto.InventoryBookingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "InventoryService")
public interface InventoryClient {
  @GetMapping("/inventory/{productId}/availability")
  AvailabilityResponse checkAvailability(@PathVariable Long productId, @RequestParam Long  quantity);

  @PostMapping("/inventory/reserve")
  ResponseEntity<InventoryBookingResponse> reserveProduct(@RequestBody InventoryBookingRequest inventoryBookingRequest);
}

