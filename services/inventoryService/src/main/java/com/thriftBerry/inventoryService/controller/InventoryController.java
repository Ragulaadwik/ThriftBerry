package com.thriftBerry.inventoryService.controller;

import com.thriftBerry.inventoryService.dto.*;
import com.thriftBerry.inventoryService.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;


    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;

    }

    @PostMapping
    public ResponseEntity<InventoryResponse> addInventory(@RequestBody @Valid RequestDto requestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.addInventory(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventories(){
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id){
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @PostMapping("/reserve")
    public ResponseEntity<InventoryBookingResponse> reserveInventory(@RequestBody @Valid InventoryBookingRequest request){
        InventoryBookingResponse response = inventoryService.reserveInventory(request);
       return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/release")
    public ResponseEntity<InventoryBookingResponse> releaseInventory(@RequestBody @Valid InventoryBookingRequest request){
        InventoryBookingResponse response = inventoryService.releaseInventory(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/confirm")
    public ResponseEntity<InventoryBookingResponse> confirmInventory(@RequestBody @Valid InventoryBookingRequest request){
        InventoryBookingResponse response = inventoryService.confirmInventory(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/{productId}/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @PathVariable Long productId,
            @RequestParam Long quantity) {

        return ResponseEntity.ok(
                inventoryService.checkAvailability(productId, quantity)
        );
    }


}
