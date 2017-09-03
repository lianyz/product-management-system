package by.kraskovski.pms.service;

import by.kraskovski.pms.domain.ProductStock;

/**
 * Service for {@link ProductStock}
 */
public interface ProductStockService {

    /**
     * Find {@link ProductStock} by id
     */
    ProductStock find(String id);

    /**
     * Find {@link ProductStock} by stock and products ids
     */
    ProductStock findByStockIdAndProductId(String stockId, String productId);
}
