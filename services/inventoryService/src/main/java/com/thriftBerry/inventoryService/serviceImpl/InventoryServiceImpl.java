package com.thriftBerry.inventoryService.serviceImpl;

import com.thriftBerry.inventoryService.dto.*;
import com.thriftBerry.inventoryService.exception.InventoryConflictException;
import com.thriftBerry.inventoryService.exception.InventoryNotFoundException;
import com.thriftBerry.inventoryService.mapper.InventoryMapper;
import com.thriftBerry.inventoryService.model.Inventory;
import com.thriftBerry.inventoryService.repository.InventoryRepository;
import com.thriftBerry.inventoryService.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public InventoryResponse addInventory(RequestDto requestDto) {
           Inventory inventory = inventoryMapper.toEntity(requestDto);
        System.out.println( "Inventory after Mapper" + inventory);
             Inventory saved =  inventoryRepository.save(inventory);
             return inventoryMapper.toResponse(saved);
    }

    @Override
    public List<InventoryResponse> getAllInventories() {

             List<Inventory> list =   inventoryRepository.findAll();

                return list.stream().map(inventoryMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public InventoryResponse getInventoryById(Long id) {
        Inventory inventory= inventoryRepository.findById(id).orElseThrow(()->new InventoryNotFoundException(id));
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryBookingResponse reserveInventory(InventoryBookingRequest request) {
        Inventory inventory = getInventoryOrThrow(request.getProductId());
        try{
            inventory.reserveStock(request.getQuantity());
        }catch (IllegalArgumentException ex){
            throw new InventoryConflictException(ex.getMessage());
        }
        return buildResponse(inventory,"Inventory reserve successful");
    }

    @Override
    @Transactional
    public InventoryBookingResponse releaseInventory(InventoryBookingRequest request) {
        Inventory inventory = getInventoryOrThrow(request.getProductId());
        try{
            inventory.releaseStock(request.getQuantity());
        }catch (IllegalArgumentException ex){
            throw new InventoryConflictException(ex.getMessage());
        }
        return buildResponse(inventory,"Inventory release successful");

    }

    @Override
    @Transactional
    public InventoryBookingResponse confirmInventory(InventoryBookingRequest request) {
        Inventory inventory = getInventoryOrThrow(request.getProductId());

        try{
            inventory.confirmStock(request.getQuantity());
        }catch (IllegalArgumentException ex){
            throw new InventoryConflictException(ex.getMessage());
        }
        return buildResponse(inventory,"Inventory confirm successful");
    }

    @Override
    public AvailabilityResponse checkAvailability(Long productId, Long available) {
        Inventory product = getInventoryOrThrow(productId);

        AvailabilityResponse availabilityResponse = new AvailabilityResponse();
        availabilityResponse.setProductId(productId);
        availabilityResponse.setAvailableQuantity(product.getAvailableStock());
        availabilityResponse.setAvailable(product.getAvailableStock() >= available);
        return availabilityResponse;
    }

    private Inventory getInventoryOrThrow(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
    }

    private InventoryBookingResponse buildResponse(Inventory inventory, String message) {

        InventoryBookingResponse inventoryBookingResponse = new InventoryBookingResponse();
                inventoryBookingResponse.setReservedQuantity(inventory.getReservedStock());
                inventoryBookingResponse.setMessage(message);
                inventoryBookingResponse.setAvailableQuantity(inventory.getAvailableStock());
                inventoryBookingResponse.setProductId(inventory.getProductId());

                return inventoryBookingResponse;
    }

}
