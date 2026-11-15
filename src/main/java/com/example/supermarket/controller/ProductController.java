package com.example.supermarket.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.supermarket.model.Product;
import com.example.supermarket.service.ProductService;

@RestController // Tell Spring this class is a REST controller
@RequestMapping("/products") // Handle requests whose URL starts with "/products"
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

   @GetMapping
    public List<Product> getAllProducts(
        @RequestParam(required = false) Integer limit) {

        List<Product> products = productService.getAllProducts()
            .stream()
            .sorted(Comparator.comparing(Product::getId))
            .toList();

        if (limit == null) {
            return products;
        }

        if (limit < 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "limit must be at least 1"
            );
        }

        return products.stream()
            .limit(limit)
            .toList();
    }

    @GetMapping("/{id}") // Handle GET requests for a specific product ID
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping // Handle POST requests for a specific product ID
    public String createProduct(@RequestBody Product product) {
        productService.createProduct(
            product.getName(), 
            product.getDescription(), 
            product.getCategory(), 
            product.getPrice()
        );
        return "Product " + product.getName() + " added!";
    }

    @PutMapping("/{id}")
    public String updateProduct(@PathVariable Long id, @RequestBody Product product) {
        productService.updateProduct(
            id,
            product.getName(), 
            product.getDescription(), 
            product.getCategory(), 
            product.getPrice()
        );
        return "ID " + id + " updated! New values: " + product.getName() + " - " + product.getDescription() + " - " + product.getCategory() + " - " + product.getPrice();
    }
    
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "Product with ID " + id + " deleted!";
    }
}
