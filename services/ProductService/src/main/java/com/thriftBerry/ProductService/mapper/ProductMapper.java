package com.thriftBerry.ProductService.mapper;

import com.thriftBerry.ProductService.dto.ProductRequestDto;
import com.thriftBerry.ProductService.dto.ProductRequestOptionalFields;
import com.thriftBerry.ProductService.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    Product toProduct(ProductRequestDto productRequestDto);

    ProductRequestDto toDto(Product product);

    void patchProduct(ProductRequestOptionalFields dto, @MappingTarget Product entity);

    void updateProductByDto(ProductRequestDto dto,@MappingTarget Product entity);

}
