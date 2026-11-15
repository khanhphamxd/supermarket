import http from 'k6/http';
import { check } from 'k6';
import { config } from '../config.js';

export function getProduct() {
    const response = http.get(
        `${config.restBaseUrl}/products/${config.productId}`
    );

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,
        'correct product returned': (r) =>
            String(r.json().id) === String(config.productId),
    });

    return response;
}

export function getInventoryItem() {
    const response = http.get(
        `${config.restBaseUrl}/inventory/${config.inventoryItemId}`
    );

    const body = response.body ? response.json() : null;

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,
        'correct inventory item returned': () =>
            Boolean(
                body &&
                String(body.id) === String(config.inventoryItemId)
            ),
        'correct nested product returned': () =>
            Boolean(
                body &&
                body.product &&
                String(body.product.id) ===
                    String(config.inventoryProductId)
            ),
    });

    return response;
}

export function getCart() {
    const response = http.get(
        `${config.restBaseUrl}/carts/${config.cartId}`
    );

    const body = response.body ? response.json() : null;

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,

        'correct cart returned': () =>
            Boolean(
                body &&
                String(body.id) === String(config.cartId)
            ),

        'expected cart item returned': () =>
            Boolean(
                body &&
                Array.isArray(body.items) &&
                body.items.some(
                    (item) =>
                        String(item.id) ===
                        String(config.cartItemId)
                )
            ),

        'correct nested product returned': () =>
            Boolean(
                body &&
                Array.isArray(body.items) &&
                body.items.some(
                    (item) =>
                        String(item.id) ===
                            String(config.cartItemId) &&
                        item.product &&
                        String(item.product.id) ===
                            String(config.cartProductId)
                )
            ),
    });

    return response;
}

export function getProducts(limit) {
    const response = http.get(
        `${config.restBaseUrl}/products?limit=${limit}`,
        { timeout: '5s' }
    );

    const products = response.body ? response.json() : null;

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,

        'product list returned': () =>
            Array.isArray(products) && products.length > 0,

        'requested product count returned': () =>
            Array.isArray(products) && products.length === limit,

        'products sorted by ascending ID': () =>
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
    const response = http.put(
        `${config.restBaseUrl}/carts/${config.cartId}/items/${config.cartItemId}`,
        JSON.stringify({ quantity: quantity }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    const expectedText =
        `Item ID ${config.cartItemId} in cart ID ${config.cartId} has its quantity updated to ${quantity}`;

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,
        'update acknowledgement matches target': (r) =>
            Boolean(r.body && r.body.includes(expectedText)),
    });

    return response;
}