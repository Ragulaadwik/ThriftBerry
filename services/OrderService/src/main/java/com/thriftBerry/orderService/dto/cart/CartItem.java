package com.thriftBerry.orderService.dto.cart;




public class CartItem {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    private Long productId;
    private Long quantity;
    private Long price;

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    private CartResponse cart;

    public void setCart(CartResponse cart){
        this.cart = cart;
    }

    public CartResponse getCart(){
        return cart;
    }
}
