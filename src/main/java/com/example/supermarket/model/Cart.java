package com.example.supermarket.model;

import java.time.Instant; //java.time: modern time package of Java, Instant is a class that let you log a precise time moment (here would be the moment you create the cart)
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

@Entity 
public class Cart {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //Cart's ID

    private Instant createdAt; //When the cart was created

    // As CartItem is a child @Entity of Cart, cascade ask whether to apply operations used on
    // Cart to its child @Entity (CartItem)
    // CascadeType.ALL here means "Yes, do everything we do to Cart to them childs (jokes XD)"
    // orphanRemoval = True means "Yeah, if I say their mom class (Cart) doesn't need it, disown them from the db"
    @OneToMany (
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    //List of items the customer has selected for this cart & its quantity (init an empty list on construction)
    private List<CartItem> items = new ArrayList<CartItem>(); 

    public Cart() {
        this.createdAt = Instant.now();
    }

    public Cart(Instant createdAt, List<CartItem> items) {
        this.createdAt = createdAt;
        this.items = items;
    }
    
    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
        
    public List<CartItem> getItems() {
        return items;
    }

    public void addItem(CartItem item) {
        items.add(item);
    }

    public CartItem findItem(Long itemID) {
        for (CartItem item: items) {
            if (item.getId().equals(itemID)) {
                return item;
            }
        }
        // throw an exception if no matching item is found
        throw new NoSuchElementException("Cart item not found: " + itemID);
    }

    public void removeItem(Long itemID) {
        for (CartItem item: items) {
            if (item.getId().equals(itemID)) {
                items.remove(item);
                return;
            }
        }
        throw new NoSuchElementException("Cart item not found: " + itemID); 
    }

    @Override // Override the default toString() method
    // Write out the product's attribute into 1 line
    public String toString() {
        return "id: " + id + " - time created: " + createdAt;
    }
}
