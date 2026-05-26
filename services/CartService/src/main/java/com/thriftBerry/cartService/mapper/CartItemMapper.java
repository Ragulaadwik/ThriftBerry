package com.thriftBerry.cartService.mapper;


import com.thriftBerry.cartService.dto.CartItemRequest;
import com.thriftBerry.cartService.dto.CartItemResponse;
import com.thriftBerry.cartService.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

 CartItemResponse toDto(CartItem item);

 CartItem toEntity (CartItemRequest request);
}
