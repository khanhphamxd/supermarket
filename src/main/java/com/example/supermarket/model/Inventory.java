package com.example.supermarket.model;

import java.util.List;

public class Inventory {
    private List<InventoryItem> items; //Consisting of a list of inventory items & its quantity

    public Inventory(List<InventoryItem> items) {
        this.items = items;
    }
    
    public List<InventoryItem> getItems() {
        return items;
    }
}
