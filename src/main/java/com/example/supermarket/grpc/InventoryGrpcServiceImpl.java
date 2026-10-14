package com.example.supermarket.grpc;

import java.util.List;

import org.springframework.grpc.server.service.GrpcService;

import com.example.supermarket.model.InventoryItem;
import com.example.supermarket.model.Product;
import com.example.supermarket.service.InventoryService;
import com.example.supermarket.service.ProductService;

import io.grpc.stub.StreamObserver;

@GrpcService
public class InventoryGrpcServiceImpl
        extends InventoryGrpcServiceGrpc.InventoryGrpcServiceImplBase {

    private final InventoryService inventoryService;
    private final ProductService productService;

    public InventoryGrpcServiceImpl(
            InventoryService inventoryService,
            ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    // Generate the responses (Product & InventoryItem) outside for reusability
    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.newBuilder()
            .setId(product.getId())
            .setName(product.getName())
            .setDescription(product.getDescription())
            .setCategory(product.getCategory())
            .setPrice(product.getPrice().doubleValue())
            .build();
    }

    private InventoryItemResponse toInventoryItemResponse(InventoryItem item) {
        return InventoryItemResponse.newBuilder()
            .setId(item.getId())
            // don't remove toProductResponse, it's used here XD
            .setProduct(toProductResponse(item.getProduct()))
            .setQuantity(item.getQuantity())
            .build();
    }

    @Override
    public void getInventoryItems(
            Empty request,
            StreamObserver<InventoryItemList> responseObserver) {

        List<InventoryItem> items =
            inventoryService.getAllInventoryItems();

        InventoryItemList.Builder result = InventoryItemList.newBuilder();

        for (InventoryItem item : items) {
            // add response immediately using toInventoryItemResponse
            result.addInventoryItems(toInventoryItemResponse(item));
        }

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getInventoryItem(
            InventoryItemRequest request,
            StreamObserver<InventoryItemResponse> responseObserver) {

        InventoryItem item =
            inventoryService.getInventoryItemById(request.getId());
        
        // Forward a response immediately using toInventoryItemResponse
        responseObserver.onNext(toInventoryItemResponse(item));
        responseObserver.onCompleted();
    }

    @Override
    public void createInventoryItem(
            CreateInventoryItemRequest request,
            StreamObserver<Empty> responseObserver) {

        Product product =
            productService.getProductById(request.getProductId());

        inventoryService.createInventoryItem(
            product,
            request.getQuantity()
        );

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateInventoryItem(
            UpdateInventoryItemRequest request,
            StreamObserver<Empty> responseObserver) {
            Integer quantity = request.hasQuantity() ? request.getQuantity() : null;
            inventoryService.updateInventoryItem(
                request.getId(),
                quantity
            );
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteInventoryItem(
            InventoryItemRequest request,
            StreamObserver<Empty> responseObserver) {

        inventoryService.deleteInventoryItem(request.getId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
