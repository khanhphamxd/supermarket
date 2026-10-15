package com.example.supermarket.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.example.supermarket.model.InventoryItem;
import com.example.supermarket.service.InventoryService;

@Controller
public class InventoryGraphqlController {

    private final InventoryService inventoryService;

    public InventoryGraphqlController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @QueryMapping
    public List<InventoryItem> inventoryItems() {
        return inventoryService.getAllInventoryItems();
    }

    @QueryMapping
    public InventoryItem inventoryItem(@Argument("id") Long id) {
        return inventoryService.getInventoryItemById(id);
    }
}
