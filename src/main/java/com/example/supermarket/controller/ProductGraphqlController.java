package com.example.supermarket.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
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
    public List<Product> products() {
        return productService.getAllProducts();
    }

    @QueryMapping
    //Explicitly extract argument "id" avoid relying on Java-side parameter name
    public Product product(@Argument("id") Long id) {
        return productService.getProductById(id);
    }
}

