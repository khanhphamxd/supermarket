package com.example.supermarket.grpc;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.grpc.server.service.GrpcService;

import com.example.supermarket.model.Product;
import com.example.supermarket.service.ProductService;

import io.grpc.stub.StreamObserver;

@GrpcService
public class ProductGrpcServiceImpl extends ProductGrpcServiceGrpc.ProductGrpcServiceImplBase {
    private final ProductService productService;

    public ProductGrpcServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void getProducts(Empty request, StreamObserver<ProductList> responseObserver) {
        List<Product> products = productService.getAllProducts();
        // generate a message builder
        ProductList.Builder result = ProductList.newBuilder();

        for (Product product: products) {
            // generate a ProductResponse format of each product for the builder
            ProductResponse grpcProduct = ProductResponse.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setDescription(product.getDescription())
                .setCategory(product.getCategory())
                .setPrice(product.getPrice().doubleValue())
                .build();
            
            result.addProducts(grpcProduct); //Add the products into the builder
        }
        // Observer: "U done with getting all the products? Now build!"
        responseObserver.onNext(result.build());
        // Observer: "U done building? Let's return nothing & dip"
        responseObserver.onCompleted();
    }

    @Override 
    public void getProduct(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        Product product = productService.getProductById(request.getId());
        ProductResponse.Builder result = ProductResponse.newBuilder()
            .setId(product.getId())
            .setName(product.getName())
            .setDescription(product.getDescription())
            .setCategory(product.getCategory())
            .setPrice(product.getPrice().doubleValue());
        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<Empty> responseObserver) {
        productService.createProduct(
            request.getName(), 
            request.getDescription(), 
            request.getCategory(), 
            BigDecimal.valueOf(request.getPrice())
        );

        //the observer generate a default Empty message upon creation completed
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override 
    public void updateProduct(UpdateProductRequest request, StreamObserver<Empty> responseObserver) {
        // "null" if the client returns no new price value, otherwise save that price value
        BigDecimal price = request.hasPrice()
            ? BigDecimal.valueOf(request.getPrice())
            : null;
        
        productService.updateProduct(request.getId(),
            request.getName(), 
            request.getDescription(), 
            request.getCategory(), 
            price
        );

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override 
    public void deleteProduct(ProductRequest request, StreamObserver<Empty> responseObserver) {
        productService.deleteProductById(request.getId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
