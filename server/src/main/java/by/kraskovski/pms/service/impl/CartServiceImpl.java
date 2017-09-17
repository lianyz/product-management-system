package by.kraskovski.pms.service.impl;

import by.kraskovski.pms.domain.model.Cart;
import by.kraskovski.pms.domain.model.CartProductStock;
import by.kraskovski.pms.domain.model.ProductStock;
import by.kraskovski.pms.domain.model.User;
import by.kraskovski.pms.repository.CartRepository;
import by.kraskovski.pms.service.CartService;
import by.kraskovski.pms.service.ProductStockService;
import by.kraskovski.pms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.InstanceAlreadyExistsException;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductStockService productStockService;
    private final UserService userService;

    @Autowired
    public CartServiceImpl(
            final CartRepository cartRepository,
            final ProductStockService productStockService,
            final UserService userService) {
        this.cartRepository = cartRepository;
        this.productStockService = productStockService;
        this.userService = userService;
    }

    @Override
    public void create(final String id) throws InstanceAlreadyExistsException {
        final User user = userService.find(id);
        if (user.getCart() != null) {
            throw new InstanceAlreadyExistsException("Cart with id:" + id + " already exists!");
        }
        user.addCart(new Cart());
        userService.update(user);
    }

    @Override
    @Transactional
    public boolean addProduct(final String cartId, final String productStockId, final int count) {
        final Cart cart = find(cartId);
        final ProductStock productStock = productStockService.find(productStockId);

        if (cart == null || productStock == null) {
            return false;
        }

        for (CartProductStock cartProductStock : cart.getCartProductStocks()) {
            if (cartProductStock.getProductStock().equals(productStock)) {
                return addExistingProductToCart(cartProductStock, cart, count);
            }
        }
        return addNewProductToCart(cart, productStock, count);
    }

    private boolean addExistingProductToCart(final CartProductStock cartProductStock, final Cart cart, final int count) {
        if (cartProductStock.getProductStock().getProductsCount() - (cartProductStock.getProductCount() + count) >= 0) {
            cartProductStock.setProductCount(cartProductStock.getProductCount() + count);
            cart.setTotalCost(cart.getTotalCost() + cartProductStock.getProductStock().getProduct().getCost() * count);
            cartRepository.save(cart);
            return true;
        }
        return false;
    }

    private boolean addNewProductToCart(final Cart cart, final ProductStock productStock, final int count) {
        if (productStock.getProductsCount() - count >= 0) {
            final CartProductStock cartProductStock = new CartProductStock(cart, productStock, count);
            cart.getCartProductStocks().add(cartProductStock);
            cart.setTotalCost(cart.getTotalCost() + productStock.getProduct().getCost() * count);
            cartRepository.save(cart);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteProduct(final String cartId, final String productStockId, final int count) {
        final Cart cart = find(cartId);
        final ProductStock productStock = productStockService.find(productStockId);

        if (cart == null && productStock == null) {
            return false;
        }

        return cart.getCartProductStocks().stream()
                .filter(cartProductStock -> cartProductStock.getProductStock().equals(productStock))
                .findFirst()
                .map(cartProductStock -> deleteProductFromCartProductStock(cart, cartProductStock, count))
                .orElse(false);
    }

    private boolean deleteProductFromCartProductStock(
            final Cart cart,
            final CartProductStock cartProductStock,
            final int count) {
        if (cartProductStock.getProductCount() - count > 0) {
            cartProductStock.setProductCount(cartProductStock.getProductCount() - count);
            cart.setTotalCost(cart.getTotalCost() - cartProductStock.getProductStock().getProduct().getCost() * count);
            cartRepository.save(cart);
            return true;
        } else if (cartProductStock.getProductCount() - count == 0) {
            cart.getCartProductStocks().remove(cartProductStock);
            cart.setTotalCost(cart.getTotalCost() - cartProductStock.getProductStock().getProduct().getCost() * count);
            cartRepository.save(cart);
            return true;
        }
        return false;
    }

    @Override
    public Cart find(final String id) {
        return Optional.ofNullable(cartRepository.findOne(id))
                .orElseThrow(() -> new EntityNotFoundException("Cart with id: " + id + " not found in db!"));
    }

    @Override
    public Cart update(final Cart object) {
        return cartRepository.save(object);
    }

    @Override
    public void delete(final String id) {
        final User user = userService.find(id);
        if (user.getCart() != null) {
            user.removeCart();
            userService.update(user);
        }
    }

    @Override
    public void deleteAll() {
        cartRepository.deleteAll();
    }
}
