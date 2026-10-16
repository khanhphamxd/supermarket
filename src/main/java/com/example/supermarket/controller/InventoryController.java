package com.example.supermarket.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.supermarket.model.InventoryItem;
import com.example.supermarket.service.InventoryService;

@RestController 
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping 
    public List<InventoryItem> getAllInventoryItems() {
        return inventoryService.getAllInventoryItems();
    }

    @GetMapping("/{id}")
    public InventoryItem getInventoryItemById(@PathVariable Long id) {
        return inventoryService.getInventoryItemById(id);
    }

    @PostMapping 
    public String createInventoryItem(@RequestBody InventoryItem inventoryItem) {
        inventoryService.createInventoryItem(inventoryItem.getProduct(), inventoryItem.getQuantity());
        return inventoryItem.getQuantity() + " " + inventoryItem.getProduct().getName() + " is added to the inventory";
    }

    @PutMapping("/{id}")
    public String updateInventoryItem(@PathVariable Long id, @RequestBody InventoryItem inventoryItem) {
        inventoryService.updateInventoryItem(id, inventoryItem.getQuantity());
        return "Inventory item ID " + id + " amount updated to " + inventoryItem.getQuantity();
    }

    @DeleteMapping("/{id}")
    public String deleteInventoryItem(@PathVariable Long id) {
        inventoryService.deleteInventoryItem(id);
        return "Inventory item ID " + id + " removed";
    }
}
