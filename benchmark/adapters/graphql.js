import http from 'k6/http';
import { check } from 'k6';
import { config } from '../config.js';

const query = `
    query GetProduct($id: ID!) {
        product(id: $id) {
            id
            name
            description
            category
            price
        }
    }
`;

export function getProduct() {
    const response = http.post(
        config.graphqlUrl,
        JSON.stringify({
            query: query,
            variables: {
                id: String(config.productId),
            },
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    const body = response.json();

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,
        'GraphQL has no errors': () =>
            !body.errors || body.errors.length === 0,
        'correct product returned': () =>
            body.data &&
            body.data.product &&
            String(body.data.product.id) === String(config.productId),
    });

    return response;
}

const inventoryQuery = `
    query GetInventoryItem($id: ID!) {
        inventoryItem(id: $id) {
            id
            quantity
            product {
                id
                name
                description
                category
                price
            }
        }
    }
`;

export function getInventoryItem() {
    const response = http.post(
        config.graphqlUrl,
        JSON.stringify({
            query: inventoryQuery,
            variables: {
                id: String(config.inventoryItemId),
            },
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    const body = response.body ? response.json() : null;

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,
        'GraphQL has no errors': () =>
            Boolean(
                body &&
                (!body.errors || body.errors.length === 0)
            ),
        'correct inventory item returned': () =>
            Boolean(
                body &&
                body.data &&
                body.data.inventoryItem &&
                String(body.data.inventoryItem.id) ===
                    String(config.inventoryItemId)
            ),
        'correct nested product returned': () =>
            Boolean(
                body &&
                body.data &&
                body.data.inventoryItem &&
                body.data.inventoryItem.product &&
                String(body.data.inventoryItem.product.id) ===
                    String(config.inventoryProductId)
            ),
    });

    return response;
}

const cartQuery = `
    query GetCart($id: ID!) {
        cart(id: $id) {
            id
            createdAt
            items {
                id
                quantity
                product {
                    id
                    name
                    description
                    category
                    price
                }
            }
        }
    }
`;

export function getCart() {
    const response = http.post(
        config.graphqlUrl,
        JSON.stringify({
            query: cartQuery,
            variables: {
                id: String(config.cartId),
            },
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    const body = response.body ? response.json() : null;

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,

        'GraphQL has no errors': () =>
            Boolean(
                body &&
                (!body.errors || body.errors.length === 0)
            ),

        'correct cart returned': () =>
            Boolean(
                body &&
                body.data &&
                body.data.cart &&
                String(body.data.cart.id) ===
                    String(config.cartId)
            ),

        'expected cart item returned': () =>
            Boolean(
                body &&
                body.data &&
                body.data.cart &&
                body.data.cart.items &&
                body.data.cart.items.some(
                    (item) =>
                        String(item.id) ===
                        String(config.cartItemId)
                )
            ),

        'correct nested product returned': () =>
            Boolean(
                body &&
                body.data &&
                body.data.cart &&
                body.data.cart.items &&
                body.data.cart.items.some(
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

const productListQuery = `
    query GetProducts($limit: Int!) {
        products(limit: $limit) {
            id
            name
            description
            category
            price
        }
    }
`;

export function getProducts(limit) {
    const response = http.post(
        config.graphqlUrl,
        JSON.stringify({
            query: productListQuery,
            variables: { limit: limit },
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
            timeout: '5s',
        }
    );

    const body = response.body ? response.json() : null;
    const products = body && body.data
        ? body.data.products
        : null;

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,

        'GraphQL has no errors': () =>
            Boolean(
                body &&
                (!body.errors || body.errors.length === 0)
            ),

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
    const response = http.post(
        config.graphqlUrl,
        JSON.stringify({
            query: `
                mutation UpdateCartItem(
                    $cartId: ID!,
                    $itemId: ID!,
                    $quantity: Int
                ) {
                    updateCartItem(
                        cartId: $cartId,
                        itemId: $itemId,
                        quantity: $quantity
                    ) {
                        id
                        quantity
                    }
                }
            `,
            variables: {
                cartId: String(config.cartId),
                itemId: String(config.cartItemId),
                quantity: quantity,
            },
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    const body = response.body ? response.json() : null;
    const item =
        body && body.data ? body.data.updateCartItem : null;

    check(response, {
        'HTTP status is 200': (r) => r.status === 200,
        'GraphQL has no errors': () =>
            Boolean(body && (!body.errors || body.errors.length === 0)),
        'correct cart item returned': () =>
            Boolean(
                item &&
                String(item.id) === String(config.cartItemId)
            ),
        'updated quantity returned': () =>
            Boolean(
                item &&
                Number(item.quantity) === Number(quantity)
            ),
    });

    return response;
}