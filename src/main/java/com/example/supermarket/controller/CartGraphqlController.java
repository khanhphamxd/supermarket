package com.example.supermarket.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.example.supermarket.model.Cart;
import com.example.supermarket.model.CartItem;
import com.example.supermarket.model.Product;
import com.example.supermarket.service.CartService;
import com.example.supermarket.service.ProductService;

@Controller
public class CartGraphqlController {

    private final CartService cartService;
    private final ProductService productService;

    public CartGraphqlController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @QueryMapping
    public List<Cart> carts() {
        return cartService.getAllCarts();
    }

    @QueryMapping
    public Cart cart(@Argument("id") Long id) {
        return cartService.getCartById(id);
    }

    //Convert createdAt to String
    @SchemaMapping(typeName = "Cart", field = "createdAt")
    public String createdAt(Cart cart) {
        return cart.getCreatedAt().toString();
    }

    //Call service's .getAllCartItems() to dodge the lazy init issue from .getItems()
    @SchemaMapping(typeName = "Cart", field = "items")
    public List<CartItem> items(Cart cart) {
        return cartService.getAllCartItems(cart.getId());
    }

    @MutationMapping
    public Cart createCart() {
        return cartService.createCart();
    }

    @MutationMapping
    public Boolean deleteCart(@Argument("id") Long id) {
        cartService.deleteCart(id);
        return true;
    }

    @MutationMapping
    public CartItem createCartItem(
            @Argument("cartId") Long cartId,
            @Argument("productId") Long productId,
            @Argument("quantity") int quantity) {

        Product product = productService.getProductById(productId);

        return cartService.createItemInCart(
                cartId, product, quantity);
    }

    @MutationMapping
    public CartItem updateCartItem(
            @Argument("cartId") Long cartId,
            @Argument("itemId") Long itemId,
            @Argument("quantity") Integer quantity) {

        return cartService.updateCartItem(
                cartId, itemId, quantity);
    }

    @MutationMapping
    public Boolean deleteCartItem(
            @Argument("cartId") Long cartId,
            @Argument("itemId") Long itemId) {

        cartService.deleteCartItem(cartId, itemId);
        return true;
    }
}