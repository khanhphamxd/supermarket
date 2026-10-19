package com.example.supermarket.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.example.supermarket.model.InventoryItem;
import com.example.supermarket.service.InventoryService;
import com.example.supermarket.service.ProductService;

@Controller
public class InventoryGraphqlController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    public InventoryGraphqlController(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    @QueryMapping
    public List<InventoryItem> inventoryItems() {
        return inventoryService.getAllInventoryItems();
    }

    @QueryMapping
    public InventoryItem inventoryItem(@Argument("id") Long id) {
        return inventoryService.getInventoryItemById(id);
    }

    @MutationMapping 
    public InventoryItem createInventoryItem (
        @Argument("id") Long id,
        @Argument("quantity") int quantity ) {
        return inventoryService.createInventoryItem(productService.getProductById(id), quantity);
    }
}
