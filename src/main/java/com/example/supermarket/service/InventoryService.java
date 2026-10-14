package com.example.supermarket.service;

import com.example.supermarket.model.InventoryItem;
import com.example.supermarket.model.Product;
import com.example.supermarket.repository.InventoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service /*Observe the class & create/connect the dependencies of any methods/declarations 
as they are being initialized*/
public class InventoryService {

    // final: not reassignable (aka can't go productRepository = someOtherStuff no more)
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryItem getInventoryItemById (Long id) {
        return inventoryRepository.findById(id).orElseThrow();       
    }

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryRepository.findAll();
    }

    public void createInventoryItem(Product product, int quantity) {
        InventoryItem item = new InventoryItem(product, quantity);
        inventoryRepository.save(item);
    }

    public void updateInventoryItem(Long id, Integer newQuantity) {
        InventoryItem item = inventoryRepository.findById(id).orElseThrow();
        if (newQuantity != null) {item.setQuantity(newQuantity);}
        inventoryRepository.save(item);
    }

    public void deleteInventoryItem(Long id) {
        inventoryRepository.delete(getInventoryItemById(id));
    }
}
