import { check } from 'k6';
import { Client, StatusOK } from 'k6/net/grpc';
import { config } from '../config.js';

const client = new Client();

// Load the service and message definitions from your existing .proto file.
client.load(['../src/main/proto'], 'supermarket.proto');

// Only connect if gRPC is not connected & the boolean flags return false, otherwise stop trying to connect again
let grpcConnected = false;

function connectOncePerVU() {
    if (grpcConnected) {
        return;
    }

    client.connect(config.grpcAddress, {
        plaintext: true,
        timeout: '5s',
    });

    grpcConnected = true;
}

export function getProduct() {
    connectOncePerVU();

    const response = client.invoke(
        'ProductGrpcService/GetProduct',
        {
            id: Number(config.productId),
        },
        {
            timeout: '5s',
        }
    );

    check(response, {
        'gRPC: status is OK':
            (r) => r !== null && r.status === StatusOK,

        'gRPC: correct product returned':
            (r) => r !== null &&
                r.message !== null &&
                r.message !== undefined &&
                String(r.message.id) ===
                    String(config.productId),
    });

    return response;
}

export function getInventoryItem() {
    connectOncePerVU();

    const response = client.invoke(
        'InventoryGrpcService/GetInventoryItem',
        {
            id: Number(config.inventoryItemId),
        },
        {
            timeout: '5s',
        }
    );

    check(response, {
        'gRPC: status is OK':
            (r) => r !== null && r.status === StatusOK,
        'gRPC: correct inventory item returned':
            (r) => r !== null &&
                r.message !== null &&
                r.message !== undefined &&
                String(r.message.id) ===
                    String(config.inventoryItemId),
        'gRPC: correct nested product returned':
            (r) => r !== null &&
                r.message !== null &&
                r.message.product !== null &&
                r.message.product !== undefined &&
                String(r.message.product.id) ===
                    String(config.inventoryProductId),
    });

    return response;
}

export function getCart() {
    connectOncePerVU();

    const cartResponse = client.invoke(
        'CartGrpcService/GetCart',
        {
            id: Number(config.cartId),
        },
        {
            timeout: '5s',
        }
    );

    const itemsResponse = client.invoke(
        'CartGrpcService/GetItems',
        {
            id: Number(config.cartId),
        },
        {
            timeout: '5s',
        }
    );

    check(cartResponse, {
        'gRPC: cart status is OK':
            (r) => r !== null && r.status === StatusOK,

        'gRPC: correct cart returned':
            (r) =>
                r !== null &&
                r.message !== null &&
                r.message !== undefined &&
                String(r.message.id) === String(config.cartId),
    });

    check(itemsResponse, {
        'gRPC: items status is OK':
            (r) => r !== null && r.status === StatusOK,

        'gRPC: expected cart item returned':
            (r) =>
                r !== null &&
                r.message !== null &&
                Array.isArray(r.message.items) &&
                r.message.items.some(
                    (item) =>
                        String(item.id) ===
                        String(config.cartItemId)
                ),

        'gRPC: correct nested product returned':
            (r) =>
                r !== null &&
                r.message !== null &&
                Array.isArray(r.message.items) &&
                r.message.items.some(
                    (item) =>
                        String(item.id) ===
                            String(config.cartItemId) &&
                        item.product &&
                        String(item.product.id) ===
                            String(config.cartProductId)
                ),
    });

    return {
        cart: cartResponse,
        items: itemsResponse,
    };
}

export function getProducts(limit) {
    connectOncePerVU();

    const response = client.invoke(
        'ProductGrpcService/GetProducts',
        {
            limit: limit,
        },
        {
            timeout: '5s',
        }
    );

    const products = response && response.message
        ? response.message.products
        : null;

    check(response, {
        'gRPC: product list status is OK':
            (r) => r !== null && r.status === StatusOK,

        'gRPC: product list returned': () =>
            Array.isArray(products) && products.length > 0,

        'gRPC: requested product count returned': () =>
            Array.isArray(products) && products.length === limit,

        'gRPC: products sorted by ascending ID': () =>
            Array.isArray(products) &&
            products.every(
                (p, i) =>
                    i === 0 ||
                    Number(products[i - 1].id) <= Number(p.id)
            ),
    });

    return response;
}

export function updateCartItem(quantity) {
    connectOncePerVU();

    const response = client.invoke(
        'CartGrpcService/UpdateItem',
        {
            cartId: Number(config.cartId),
            itemId: Number(config.cartItemId),
            hasQuantity: true,
            quantity: Number(quantity),
        },
        {
            timeout: '5s',
        }
    );

    check(response, {
        'gRPC update status is OK': (r) =>
            r !== null && r.status === StatusOK,
    });

    return response;
}