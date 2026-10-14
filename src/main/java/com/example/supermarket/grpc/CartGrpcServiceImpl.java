package com.example.supermarket.grpc;

import java.util.List;

import org.springframework.grpc.server.service.GrpcService;

import com.example.supermarket.model.Cart;
import com.example.supermarket.model.CartItem;
import com.example.supermarket.model.Product;
import com.example.supermarket.service.CartService;
import com.example.supermarket.service.ProductService;

import io.grpc.stub.StreamObserver;

@GrpcService
public class CartGrpcServiceImpl
        extends CartGrpcServiceGrpc.CartGrpcServiceImplBase {

    private final CartService cartService;
    private final ProductService productService;

    public CartGrpcServiceImpl(
            CartService cartService,
            ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    // Convert a Java Product into a protobuf ProductResponse.
    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.newBuilder()
            .setId(product.getId())
            .setName(product.getName())
            .setDescription(product.getDescription())
            .setCategory(product.getCategory())
            .setPrice(product.getPrice().doubleValue())
            .build();
    }

    // Convert a Java Cart into a protobuf CartResponse.
    private CartResponse toCartResponse(Cart cart) {
        return CartResponse.newBuilder()
            .setId(cart.getId())
            .setCreatedAt(cart.getCreatedAt().toString())
            .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        return CartItemResponse.newBuilder()
            .setId(item.getId())
            .setProduct(toProductResponse(item.getProduct()))
            .setQuantity(item.getQuantity())
            .build();
    }

    // ---- Cart operations ----

    @Override
    public void getCart(
            CartRequest request,
            StreamObserver<CartResponse> responseObserver) {

        Cart cart = cartService.getCartById(request.getId());

        responseObserver.onNext(toCartResponse(cart));
        responseObserver.onCompleted();
    }

    @Override
    public void getCarts(
            Empty request,
            StreamObserver<CartList> responseObserver) {

        List<Cart> carts = cartService.getAllCarts();

        CartList.Builder result = CartList.newBuilder();

        for (Cart cart : carts) {
            result.addCarts(toCartResponse(cart));
        }

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createCart(
            Empty request,
            StreamObserver<CartResponse> responseObserver) {

        Cart cart = cartService.createCart();

        responseObserver.onNext(toCartResponse(cart));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteCart(
            CartRequest request,
            StreamObserver<Empty> responseObserver) {

        cartService.deleteCart(request.getId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    // ---- Cart-item operations ----

    @Override
    public void getItems(
            CartRequest request,
            StreamObserver<CartItemList> responseObserver) {
        List<CartItem> cartItems = cartService.getAllCartItems(request.getId());
        
        CartItemList.Builder result = CartItemList.newBuilder();

        for (CartItem item: cartItems) {
            result.addItems(toCartItemResponse(item));
        }

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createItem(
            CreateCartItemRequest request,
            StreamObserver<Empty> responseObserver) {
        Product product = productService.getProductById(request.getProductId());
        cartService.createItemInCart(request.getCartId(), product, request.getQuantity());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateItem(
            UpdateCartItemRequest request,
            StreamObserver<Empty> responseObserver) {
        Integer quantity = request.hasQuantity() ? request.getQuantity() : null;
        cartService.updateCartItem(request.getCartId(), request.getItemId(), quantity);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteItem(
            CartItemRequest request,
            StreamObserver<Empty> responseObserver) {
        cartService.deleteCartItem(request.getCartId(), request.getItemId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
