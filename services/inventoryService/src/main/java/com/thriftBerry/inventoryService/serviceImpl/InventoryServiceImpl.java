package com.thriftBerry.inventoryService.serviceImpl;

import com.thriftBerry.inventoryService.dto.*;
import com.thriftBerry.inventoryService.exception.InventoryConflictException;
import com.thriftBerry.inventoryService.exception.InventoryNotFoundException;
import com.thriftBerry.inventoryService.mapper.InventoryMapper;
import com.thriftBerry.inventoryService.model.Inventory;
import com.thriftBerry.inventoryService.repository.InventoryRepository;
import com.thriftBerry.inventoryService.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public InventoryResponse addInventory(RequestDto requestDto) {
           Inventory inventory = inventoryMapper.toEntity(requestDto);

             Inventory saved =  inventoryRepository.save(inventory);
             log.info("Inventory added for productId: {}, quantity: {}", saved.getProductId(), saved.getAvailableStock());
             return inventoryMapper.toResponse(saved);
    }

    @Override
    public List<InventoryResponse> getAllInventories() {

             List<Inventory> list =   inventoryRepository.findAll();
                log.info("Retrieved {} inventory records", list.size());
                return list.stream().map(inventoryMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public InventoryResponse getInventoryById(Long id) {
        Inventory inventory= inventoryRepository.findById(id).orElseThrow(()->new InventoryNotFoundException(id));
        log.info("Retrieved inventory for id: {}, productId: {}, availableStock: {}", inventory.getId(), inventory.getProductId(), inventory.getAvailableStock());

        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryBookingResponse reserveInventory(InventoryBookingRequest request) {
        Inventory inventory = getInventoryOrThrow(request.getProductId());
        try{
            inventory.reserveStock(request.getQuantity());
        }catch (IllegalArgumentException ex){
            log.error("Failed to reserve stock for productId: {}, requestedQuantity: {}, availableStock: {}. Reason: {}",
                    request.getProductId(), request.getQuantity(), inventory.getAvailableStock(), ex.getMessage());
            throw new InventoryConflictException(ex.getMessage());
        }
        log.info("Reserved stock for productId: {}, quantity: {}, availableStock after reservation: {}",
                request.getProductId(), request.getQuantity(), inventory.getAvailableStock());
        return buildResponse(inventory,"Inventory reserve successful");
    }

    @Override
    @Transactional
    public InventoryBookingResponse releaseInventory(InventoryBookingRequest request) {
        Inventory inventory = getInventoryOrThrow(request.getProductId());
        try{
            inventory.releaseStock(request.getQuantity());
        }catch (IllegalArgumentException ex){
            log.error("Failed to release stock for productId: {}, requestedQuantity: {}, reservedStock: {}. Reason: {}",
                    request.getProductId(), request.getQuantity(), inventory.getReservedStock(), ex.getMessage());
            throw new InventoryConflictException(ex.getMessage());
        }
        log.info("Released stock for productId: {}, quantity: {}, reservedStock after release: {}",
                request.getProductId(), request.getQuantity(), inventory.getReservedStock());
        return buildResponse(inventory,"Inventory release successful");

    }

    @Override
    @Transactional
    public InventoryBookingResponse confirmInventory(InventoryBookingRequest request) {
        Inventory inventory = getInventoryOrThrow(request.getProductId());

        try{
            inventory.confirmStock(request.getQuantity());
        }catch (IllegalArgumentException ex){
            log.error("Failed to confirm stock for productId: {}, requestedQuantity: {}, reservedStock: {}. Reason: {}",
                    request.getProductId(), request.getQuantity(), inventory.getReservedStock(), ex.getMessage());
            throw new InventoryConflictException(ex.getMessage());
        }
        log.info("Confirmed stock for productId: {}, quantity: {}, reservedStock after confirmation: {}",
                request.getProductId(), request.getQuantity(), inventory.getReservedStock());
        return buildResponse(inventory,"Inventory confirm successful");
    }

    @Override
    public AvailabilityResponse checkAvailability(Long productId, Long available) {
        Inventory product = getInventoryOrThrow(productId);

        AvailabilityResponse availabilityResponse = new AvailabilityResponse();
        availabilityResponse.setProductId(productId);
        availabilityResponse.setAvailableQuantity(product.getAvailableStock());
        availabilityResponse.setAvailable(product.getAvailableStock() >= available);
        log.info("Checked availability for productId: {}, requestedQuantity: {}, availableStock: {}, isAvailable: {}",
                productId, available, product.getAvailableStock(), availabilityResponse.getAvailable());
        return availabilityResponse;
    }

    @Override
    public InventoryBookingResponse restockInventory(InventoryBookingRequest request) {
        // Validate input quantity
        if (request == null || request.getQuantity() <= 0) {
            log.error("Invalid restock request: {}", request);
            throw new IllegalArgumentException("Restock quantity must be a positive value");
        }

        // Retrieve inventory by productId (throws InventoryNotFoundException if missing)
        Inventory inventory = getInventoryOrThrow(request.getProductId());

        // Increment available stock instead of overwriting it. This is safer for real-world restock flows.
        long currentAvailable = inventory.getAvailableStock() == null ? 0L : inventory.getAvailableStock();
        long increment = request.getQuantity();
        long updatedAvailable = currentAvailable + increment;

        inventory.setAvailableStock(updatedAvailable);

        // Persist changes
        Inventory saved = inventoryRepository.save(inventory);

        log.info("Restocked inventory for productId: {}, addedQuantity: {}, availableStock after restock: {}",
                request.getProductId(), increment, saved.getAvailableStock());

        return buildResponse(saved, "Inventory restock successful");
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
