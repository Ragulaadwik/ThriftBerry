package com.thriftBerry.orderService.mapper;

import com.thriftBerry.orderService.dto.ItemResponse;
import com.thriftBerry.orderService.entity.OrderItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
          List<ItemResponse> toItemResponseList(List<OrderItem> orderItems);
}
