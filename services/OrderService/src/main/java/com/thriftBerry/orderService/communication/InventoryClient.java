package com.thriftBerry.orderService.communication;

import com.thriftBerry.orderService.dto.inventory.InventoryRequest;
import com.thriftBerry.orderService.dto.inventory.InventoryResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "InventoryService")
public interface InventoryClient {

    @PostMapping("/inventory/reserve")
    InventoryResponse reserveInventory(@RequestBody @Valid InventoryRequest request);

    @PostMapping("/inventory/release")
    void releaseInventory(@RequestBody @Valid InventoryRequest request);

    @PostMapping("/inventory/confirm")
    InventoryResponse confirmInventory(@RequestBody @Valid InventoryRequest request);

    @PostMapping("/inventory/restock")
    InventoryResponse updateInventory(@RequestBody @Valid InventoryRequest request);


}
