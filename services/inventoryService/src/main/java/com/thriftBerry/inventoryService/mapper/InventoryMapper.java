package com.thriftBerry.inventoryService.mapper;

import com.thriftBerry.inventoryService.dto.InventoryResponse;
import com.thriftBerry.inventoryService.dto.RequestDto;
import com.thriftBerry.inventoryService.model.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

     Inventory toEntity(RequestDto dto);

     RequestDto toDto(Inventory entity);

     InventoryResponse toResponse(Inventory inventory);
}
