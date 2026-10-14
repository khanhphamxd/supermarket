package com.example.supermarket.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.supermarket.model.Cart;
import com.example.supermarket.model.CartItem;
import com.example.supermarket.model.Product;
import com.example.supermarket.repository.CartItemRepository;
import com.example.supermarket.repository.CartRepository;

@Service 
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Cart createCart() {
        return cartRepository.save(new Cart());
    }
    
    public Cart getCartById(Long id) {
        return cartRepository.findById(id).orElseThrow();
    }

    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    public void deleteCart(Long id) {
        Cart cart = getCartById(id);
        cartRepository.delete(cart);
    }

    public List<CartItem> getAllCartItems(Long id) {
        return getCartById(id).getItems();
    }

    public CartItem getCartItemInCart(Long cartId, Long itemId) {
        return getCartById(cartId).findItem(itemId);
    }

    public void createItemInCart(Long id, Product product, int amount) {
        CartItem item = new CartItem(product, amount);
        Cart cart = getCartById(id);
        cart.addItem(item);
        cartRepository.save(cart);
    }

    public void updateCartItem(Long id, Long itemId, Integer newQuantity) {
        Cart cart = getCartById(id);
        CartItem item = cart.findItem(itemId);
        if (newQuantity != null) {item.setQuantity(newQuantity);}
        cartItemRepository.save(item);
    }

    public void deleteCartItem(Long id, Long itemId) {
        Cart cart = getCartById(id);
        cart.removeItem(itemId);
        cartRepository.save(cart);
    }
}
