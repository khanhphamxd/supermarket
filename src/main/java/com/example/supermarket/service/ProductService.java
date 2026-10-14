package com.example.supermarket.service;

import com.example.supermarket.model.Product;
import com.example.supermarket.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.math.BigDecimal;
import java.util.List;

@Service /*Observe the class & create/connect the dependencies of any methods/declarations 
as they are being initialized*/
public class ProductService {

    // final: not reassignable (aka can't go productRepository = someOtherStuff no more)
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProductById(Long id) {
        /*findById returns an Optional<Product> instance: Return the Product instance if 
        there's a match, otherwise Optional.empty() if no match 
        Optional<class> is a wrapper where if not empty, contain the instance of that class
        as a variable*/
        Optional<Product> searchedProduct = productRepository.findById(id); 
        /*orElseThrow: Retrieve the wrapped Product instance if not empty, 
        otherwise throw a default NoSuchElementException unchecked 
        (don't have to catch, but still should consider an exception to be handled) exception */
        Product product = searchedProduct.orElseThrow();
        return product;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void createProduct(String name, String description, String category, BigDecimal price) {
        Product product = new Product(name, description, category, price);
        saveProduct(product);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public void deleteProductById(Long id) {
        productRepository.delete(getProductById(id));
    }

    public void updateProduct(Long id, String name, String description, String category, BigDecimal price) {
        Product product = getProductById(id);
        if (!name.isBlank()) {product.setName(name);}
        if (!description.isBlank()) {product.setDescription(description);}
        if (!category.isBlank()) {product.setCategory(category);}
        if (price != null) {product.setPrice(price);}
        saveProduct(product);
    }
}
