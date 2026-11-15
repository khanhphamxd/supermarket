import { check, group } from 'k6';
import { config } from './config.js';
import { Trend } from 'k6/metrics';

// importing methods
import { getProduct as getRestProduct }
    from './adapters/rest.js';
import { getProducts as getRestProducts }
    from './adapters/rest.js';
import { getInventoryItem as getRestInventoryItem }
    from './adapters/rest.js';

import { getProduct as getGraphqlProduct }
    from './adapters/graphql.js';
import { getProducts as getGraphqlProducts }
    from './adapters/graphql.js';
import { getInventoryItem as getGraphqlInventoryItem }
    from './adapters/graphql.js';

import { getProduct as getGrpcProduct }
    from './adapters/grpc.js';
import { getProducts as getGrpcProducts }
    from './adapters/grpc.js';
import { getInventoryItem as getGrpcInventoryItem }
    from './adapters/grpc.js';

import { getCart as getRestCart }
    from './adapters/rest.js';
import { getCart as getGraphqlCart }
    from './adapters/graphql.js';
import { getCart as getGrpcCart }
    from './adapters/grpc.js';

import { updateCartItem as updateRestCartItem }
    from './adapters/rest.js';
import { updateCartItem as updateGraphqlCartItem }
    from './adapters/graphql.js';
import { updateCartItem as updateGrpcCartItem }
    from './adapters/grpc.js';

// constant declaration (protocol type, entity mode (load for actual testing, or smoke for fast 1-instance test), entiry (product/inventory/cart) & limit (for product count))
const protocol = (__ENV.PROTOCOL || 'all').toLowerCase();
const entity = (__ENV.ENTITY || 'product').toLowerCase();
const mode = (__ENV.MODE || 'smoke').toLowerCase();
const productLimit = Number(__ENV.LIMIT || 1);

// Check if the product limit included is integer & >=1
if (
    entity === 'products' &&
    (!Number.isInteger(productLimit) || productLimit < 1)
) {
    throw new Error(
        'LIMIT must be a positive integer when ENTITY=products'
    );
}

//Measure operation duration 
function measureOperation(protocolName, operation) {
    const groupName = `${entity}-${protocolName}-operation`;

    return group(groupName, function () {
        return operation();
    });
}

function writeQuantity() {
    return ((Number(__VU) * 37 + Number(__ITER) * 17) % 90) + 10;
}

//error-checking that the parameters be satisfied
if (!['all', 'rest', 'graphql', 'grpc'].includes(protocol)) {
    throw new Error(
        'PROTOCOL must be all, rest, graphql, or grpc'
    );
}

if (!['product', 'products', 'inventory', 'cart', 'cart-update', 'cart-mixed'].includes(entity)) {
    throw new Error(
        'ENTITY must be product, products, inventory, or cart'
    );
}

if (!['smoke', 'load'].includes(mode)) {
    throw new Error('MODE must be smoke or load');
}

if (mode === 'load' && protocol === 'all') {
    throw new Error(
        'Choose one protocol for load tests: rest, graphql, or grpc'
    );
}

// setting up benchmark variables to follow the custom load, or 1 for the smoke test
export const options = mode === 'load'
    ? {
        vus: Number(__ENV.VUS || 1),
        duration: __ENV.DURATION || '10s',
    }
    : {
        vus: 1,
        iterations: entity === 'cart-mixed' ? 2 : 1,
    };

// run the benchmark, based on the parameters given

export default function () {
    if (mode === 'smoke') {
        console.log(
            `Smoke test: ${entity}; protocol: ${protocol}; limit: ${productLimit}`
        );
    }

    if (protocol === 'all' || protocol === 'rest') {
        measureOperation('rest', () => {
            if (entity === 'product') {
                getRestProduct();
            } else if (entity === 'products') {
                getRestProducts(productLimit);
            } else if (entity === 'inventory') {
                getRestInventoryItem();
            } else if (entity === 'cart-update') {
                const quantity = writeQuantity();
                updateRestCartItem(quantity);
            } else if (entity === 'cart-mixed') {
                if (__ITER % 2 === 0) {
                    getRestCart();
                } else {
                    const quantity = writeQuantity();
                    updateRestCartItem(quantity);
                }
            } else {
                getRestCart();
            }
        });
    }

    if (protocol === 'all' || protocol === 'graphql') {
        measureOperation('graphql', () => {
            if (entity === 'product') {
                getGraphqlProduct();
            } else if (entity === 'products') {
                getGraphqlProducts(productLimit);
            } else if (entity === 'inventory') {
                getGraphqlInventoryItem();
            } else if (entity === 'cart-update') {
                const quantity = writeQuantity();
                updateGraphqlCartItem(quantity);
            } else if (entity === 'cart-mixed') {
                if (__ITER % 2 === 0) {
                    getGraphqlCart();
                } else {
                    const quantity = writeQuantity();
                    updateGraphqlCartItem(quantity);
                }
            } else {
                getGraphqlCart();
            }
        });
    }

    if (protocol === 'all' || protocol === 'grpc') {
        measureOperation('grpc', () => {
            if (entity === 'product') {
                getGrpcProduct();
            } else if (entity === 'products') {
                getGrpcProducts(productLimit);
            } else if (entity === 'inventory') {
                getGrpcInventoryItem();
            } else if (entity === 'cart-update') {
                const quantity = writeQuantity();
                updateGrpcCartItem(quantity);
            } else if (entity === 'cart-mixed') {
                if (__ITER % 2 === 0) {
                    getGrpcCart();
                } else {
                    const quantity = writeQuantity();
                    updateGrpcCartItem(quantity);
                }
            } else {
                getGrpcCart();
            }
        });
    }
}
