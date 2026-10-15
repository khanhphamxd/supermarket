package com.example.supermarket.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.example.supermarket.model.Cart;
import com.example.supermarket.model.CartItem;
import com.example.supermarket.service.CartService;

@Controller
public class CartGraphqlController {

    private final CartService cartService;

    public CartGraphqlController(CartService cartService) {
        this.cartService = cartService;
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
}