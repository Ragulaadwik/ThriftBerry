package com.thriftBerry.inventoryService.exception;

public class InventoryNotFoundException extends RuntimeException{

    public InventoryNotFoundException(Long id){
        super("Inventory with Id:"+id+" Not Found");
    }
}
