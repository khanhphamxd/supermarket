package com.example.supermarket.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.supermarket.model.Cart;
import com.example.supermarket.model.CartItem;
import com.example.supermarket.service.CartService;

@RestController 
@RequestMapping("/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Carts CRUD (more like CRD but whatever xd)
    @GetMapping
    public List<Cart> getAllCarts() {
        return cartService.getAllCarts();
    }

    @GetMapping("/{id}")
    public Cart getCartById(@PathVariable Long id) {
        return cartService.getCartById(id);
    }
    
    @PostMapping
    public String createCart() {
        cartService.createCart();
        return "New cart added!";
    }

    @DeleteMapping("/{id}")
    public String deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return "Cart ID " + id + " removed!";
    }

    // CartItem CRUD
    @GetMapping("/{id}/items")
    public List<CartItem> getAllCartItems(@PathVariable Long id) {
        return cartService.getAllCartItems(id);
    }
    
    @PostMapping("/{id}/items")
    public String createItemInCart(@PathVariable Long id, @RequestBody CartItem cartItem) {
        cartService.createItemInCart(id, cartItem.getProduct(), cartItem.getQuantity());
        return cartItem.getQuantity() + " " + cartItem.getProduct().getName() + " successfully added to cart ID " + id;
    }

    @PutMapping("/{id}/items/{itemId}")
    public String updateCartItem(@PathVariable Long id, @PathVariable Long itemId, @RequestBody CartItem cartItem) {
        cartService.updateCartItem(id, itemId, cartItem.getQuantity());
        return "Item ID " + itemId + " in cart ID " + id + " has its quantity updated to " + cartItem.getQuantity();
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public String deleteCartItem(@PathVariable Long id, @PathVariable Long itemId) {
        cartService.deleteCartItem(id, itemId);
        return "Cart item ID " + itemId + " removed from cart ID " + id;
    }
}