package com.example.supermarket;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.supermarket.model.*;
import com.example.supermarket.service.*;

@SpringBootApplication
public class SupermarketApplication {

    SupermarketApplication() {
        
    }

    public static void main(String[] args) {
		SpringApplication.run(SupermarketApplication.class, args);
	}

	@Bean //signals Spring to manage the Object returned by this Bean
	// Create a code snippet (with an add-on runner) that will run after the application is running
	// The parameter here is ordering Spring to supply it with a ProductService
	CommandLineRunner runner(ProductService productService, InventoryService inventoryService, CartService cartService, TransactionTemplate transactionTemplate) {
		return args -> {
			List<Product> products = productService.getAllProducts();
			System.out.println("Product list:");
			for (Product product: products) {
				System.out.println(product);
			}
			List<InventoryItem> inventory = inventoryService.getAllInventoryItems();
			System.out.println("Inventory:");
			for (InventoryItem item: inventory) {
				System.out.println(item);
			}
			/* transactionTemplate: Keep the database session open while retrieving CartItems 
			Without it, Hibernate (database interaction) session ended at getAllCarts() &
			all you have for your for-loop is item bookmarks without a database session to fetch */
			transactionTemplate.executeWithoutResult(status -> {
				List<Cart> carts = cartService.getAllCarts();
				System.out.println("Cart list:");
				for (Cart cart: carts) {
					System.out.println(cart);
					System.out.println("Items:");
					for (CartItem item: cart.getItems()) {
						System.out.println(item);
					}
				}
			});
		};
	}
}
