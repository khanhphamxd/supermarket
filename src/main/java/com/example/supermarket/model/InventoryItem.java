package com.example.supermarket.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GenerationType;

@Entity 
public class InventoryItem {
    
    //private: only allowed to be directly accessed in the InventoryItem class
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne 
    private Product product; // The product instance

    private Integer quantity; // Product's total current quantity in the inventory

    protected InventoryItem() {

    }

    public InventoryItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
        
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override // Override the default toString() method
    // Write out the product's attribute into 1 line
    public String toString() {
        return "id: " + id + " | " + product.toString() + " | quantity: " + quantity;
    }
} 
