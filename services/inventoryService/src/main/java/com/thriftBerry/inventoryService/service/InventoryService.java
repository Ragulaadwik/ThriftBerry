package com.thriftBerry.inventoryService.service;

import com.thriftBerry.inventoryService.dto.*;
import jakarta.validation.Valid;

import java.util.List;

public interface InventoryService {
    InventoryResponse addInventory(RequestDto requestDto);

    List<InventoryResponse> getAllInventories();

    InventoryResponse getInventoryById(Long id);

    InventoryBookingResponse reserveInventory(@Valid InventoryBookingRequest request);

    InventoryBookingResponse releaseInventory(@Valid InventoryBookingRequest request);

    InventoryBookingResponse confirmInventory(@Valid InventoryBookingRequest request);

    AvailabilityResponse checkAvailability(Long productId, Long available);
}
