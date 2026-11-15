package com.example.supermarket.controller;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.example.supermarket.model.Product;
import com.example.supermarket.service.ProductService;

@Controller
public class ProductGraphqlController {

    private final ProductService productService;

    public ProductGraphqlController(ProductService productService) {
        this.productService = productService;
    }

    // mapped this method into the "products" query
    @QueryMapping
    public List<Product> products(@Argument("limit") Integer limit) {

        List<Product> products = productService.getAllProducts()
            .stream()
            .sorted(Comparator.comparing(Product::getId))
            .toList();

        if (limit == null) {
            return products;
        }

        if (limit < 1) {
            throw new IllegalArgumentException(
                "limit must be at least 1"
            );
        }

        return products.stream()
            .limit(limit)
            .toList();
    }

    @QueryMapping
    //Explicitly extract argument "id" avoid relying on Java-side parameter name
    public Product product(@Argument("id") Long id) {
        return productService.getProductById(id);
    }

    @MutationMapping 
    public Product createProduct(
        @Argument("name") String name,
        @Argument("description") String description,
        @Argument("category") String category,
        @Argument("price") Double price) {
        return productService.createProduct(
            name, 
            description, 
            category, 
            BigDecimal.valueOf(price)
        );
    }

    @MutationMapping 
    public Product updateProduct(
        @Argument("id") Long id,
        @Argument("name") String name,
        @Argument("description") String description,
        @Argument("category") String category,
        @Argument("price") Double price) {
        
        BigDecimal decimalPrice =price == null ? null : BigDecimal.valueOf(price);

        return productService.updateProduct(
            id, 
            name, 
            description, 
            category, 
            decimalPrice
        );
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument("id") Long id) {
        productService.deleteProductById(id);
        return true;
    }
}

