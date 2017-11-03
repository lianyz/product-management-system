package by.kraskovski.pms.domain.service;

import by.kraskovski.pms.domain.model.Cart;
import by.kraskovski.pms.domain.model.ProductStock;
import by.kraskovski.pms.domain.model.User;
import by.kraskovski.pms.domain.repository.CartRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.management.InstanceAlreadyExistsException;

import static by.kraskovski.pms.utils.TestUtils.prepareProduct;
import static by.kraskovski.pms.utils.TestUtils.prepareStock;
import static by.kraskovski.pms.utils.TestUtils.prepareUser;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductStockService productStockService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserCartService cartService;

    @Test
    public void createPositiveTest() throws InstanceAlreadyExistsException {
        final User user = prepareUser();
        user.removeCart();
        when(userService.find(anyString())).thenReturn(user);
        when(userService.update(anyObject())).thenReturn(user);

        cartService.create(random(40));

        verify(userService).find(anyString());
        verify(userService).update(anyObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNegativeTest() throws InstanceAlreadyExistsException {
        final User user = prepareUser();
        user.createCart();
        when(userService.find(anyString())).thenReturn(user);
        when(userService.update(anyObject())).thenReturn(user);

        cartService.create(user.getId());

        verify(userService).find(anyString());
        verify(userService).update(anyObject());
    }

    @Test
    public void addNewProductToCartPositiveTest() {
        when(cartRepository.findOne(anyString())).thenReturn(new Cart(prepareUser()));
        when(productStockService.find(anyString())).thenReturn(new ProductStock(prepareProduct(), prepareStock(), 20));

        cartService.addProduct(random(40), random(40), 10);

        verify(cartRepository).findOne(anyString());
        verify(productStockService).find(anyString());
        verify(cartRepository).save((Cart) anyObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNewProductToCartNegativeTest() {
        when(cartRepository.findOne(anyString())).thenReturn(new Cart(prepareUser()));
        when(productStockService.find(anyString())).thenReturn(new ProductStock(prepareProduct(), prepareStock(), 10));

        cartService.addProduct(random(40), random(40), 11);

        verify(cartRepository).findOne(anyString());
        verify(cartRepository, times(0)).save((Cart) anyObject());
        verify(productStockService).find(anyString());
    }

    @Test
    public void addExistingProductToCartPositiveTest() {
        final Cart cart = new Cart(prepareUser());
        final ProductStock productStock = new ProductStock(prepareProduct(), prepareStock(), 20);

        when(cartRepository.findOne(anyString())).thenReturn(cart);
        when(productStockService.find(anyString())).thenReturn(productStock);

        cartService.addProduct(random(40), random(40), 10);

        verify(cartRepository).save((Cart) anyObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addExistingProductToCartNegativeTest() {
        final Cart cart = new Cart(prepareUser());
        final ProductStock productStock = new ProductStock(prepareProduct(), prepareStock(), 10);

        when(cartRepository.findOne(anyString())).thenReturn(cart);
        when(productStockService.find(anyString())).thenReturn(productStock);

        cartService.addProduct(random(40), random(40), 11);
        verify(cartRepository, times(0)).save((Cart) anyObject());
    }
}