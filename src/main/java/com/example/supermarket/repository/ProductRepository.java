package com.example.supermarket.repository;

import com.example.supermarket.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/* interface: a blueprint in which any class that implements the interface has to find a way 
    to satisfy all of what the blueprint includes
    extends: Allow inheritance of operations from an interface 
    (being able to utilize all of their methods)
    JpaRepository<Product, Long>: Create a repository definition with Product as 
    its database entity & primary key type Long */
public interface ProductRepository extends JpaRepository<Product, Long> {
    
}
