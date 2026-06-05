package com.techstore.app.testutil;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartItem;
import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.shared.Quantity;
import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.Role;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.repository.CartItemRepository;
import com.techstore.app.repository.CartRepository;
import com.techstore.app.repository.CategoryRepository;
import com.techstore.app.repository.CustomerRepository;
import com.techstore.app.repository.ProductRepository;
import com.techstore.app.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class TestDataFactory {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    public TestDataFactory(UserRepository userRepository, CustomerRepository customerRepository,
            CategoryRepository categoryRepository, ProductRepository productRepository, CartItemRepository cartItemRepository,
            CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
    }

    public User user() {
        String unique = UUID.randomUUID().toString();

        User user = new User(new Email("test-" + unique + "@example.com"), Role.CUSTOMER, new SupabaseUserId(UUID.randomUUID()));

        return userRepository.save(user);
    }

    public Customer customer() {
        User user = user();

        Customer customer = new Customer(user);

        return customerRepository.save(customer);
    }

    public Category category() {
        Category category = new Category("Electronics");

        return categoryRepository.save(category);
    }

    public Product product() {
        Category category = category();

        Product product = new Product(
                "Test Product",
                "Test product description",
                new Money(new BigDecimal("10.00")),
                category,
                new Quantity(100)
        );

        return productRepository.save(product);
    }

    public CartItem cartItem(Product product) {
        CartItem cartItem = new CartItem(1, product);

        return cartItemRepository.save(cartItem);
    }

    public Cart cartWithItem(Product product, Customer customer) {
        Cart cart = new Cart(customer);

        CartItem cartItem = cartItem(product);

        cart.addItem(cartItem);

        return cartRepository.save(cart);
    }

    public String createOrderJson(String cartId, String customerId) {
        return String.format("""
                {
                  "cartID": "%s",
                  "address": {
                    "postalCode": "4000-001",
                    "city": "Porto",
                    "country": "Portugal",
                    "street": "Rua Teste"
                  },
                  "customerID": "%s"
                }
                """, cartId, customerId);
    }
}