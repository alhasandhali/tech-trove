package com.spring.boot.ecommerce.controller;

import com.spring.boot.ecommerce.model.*;
import com.spring.boot.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class OrderController {

    @Autowired
    private UserRepository userService;
    @Autowired
    private ProductRepository productService;
    @Autowired
    private CartRepository cartService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BillingAddressRepository billingAddressRepository;

    @PostMapping("/product/buy/{id}")
    public String showBuyNowForm(@PathVariable("id") Long id, Model model, Principal principal) {
        Optional<User> users = userService.findByEmail(principal.getName());
        User user = new User();
        if (users.isPresent()) {
            user = users.get();
        }
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("checkoutForm", new CheckoutForm());
        model.addAttribute("currentUser", user);
        model.addAttribute("title", "Successfully buy - " + user.getFullName());
        return "/user/single-checkout";
    }

    @GetMapping("/product/buy/{userId}")
    public String showCheckoutForm(@PathVariable Long userId, Model model) {
        User user = userService.getUserById(userId);
        Cart cart = cartService.findByUserIdWithItems(user.getId());

        model.addAttribute("currentUser", user);
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutForm", new CheckoutForm());
        model.addAttribute("title", "Successfully buy - " + user.getFullName());

        System.err.println(user);
        System.err.println(cart);

        return "/user/cart-checkout";
    }

    @PostMapping("/product/checkout/{productId}")
    public String buyNow(@PathVariable("productId") Long productId,
                         @RequestParam int buyingQuantity,
                         @ModelAttribute CheckoutForm checkoutForm,
                         Principal principal,
                         Model model) {

        Optional<User> users = userService.findByEmail(principal.getName());
        User user = users.orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productService.getProductById(productId);

        // Calculate total
        double totalPrice = product.getPrice() * buyingQuantity;

        // Create Payment
        Payment payment = Payment.builder()
                .amount(totalPrice)
                .paymentDate(LocalDateTime.now())
                .method(checkoutForm.getPaymentMethod())
                .status(PaymentStatus.COMPLETED)
                .build();

        // Create Order
        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .paymentMethod(checkoutForm.getPaymentMethod())
                .paymentStatus(PaymentStatus.COMPLETED)
                .shippingStatus(ShippingStatus.NOT_SHIPPED)
                .totalAmount(totalPrice)
                .user(user)
                .payment(payment)
                .build();

        // Fix detached entity by setting back-reference
        payment.setOrder(order);

        orderRepository.save(order);

        // Create OrderItem
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(buyingQuantity)
                .price(totalPrice)
                .build();
        order.setItems(List.of(orderItem));

        // Set BillingAddress
        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setFullName(checkoutForm.getFullName());
        billingAddress.setStreet(checkoutForm.getStreet());
        billingAddress.setCity(checkoutForm.getCity());
        billingAddress.setState(checkoutForm.getState());
        billingAddress.setZipCode(checkoutForm.getZipCode());
        billingAddress.setCountry(checkoutForm.getCountry());
        billingAddress.setPhone(checkoutForm.getPhone());
        billingAddress.setOrder(order);

        billingAddressRepository.save(billingAddress);
        order.setBillingAddress(billingAddress);
        orderRepository.save(order);

        model.addAttribute("checkoutForm", checkoutForm);
        model.addAttribute("currentUser", user);
        model.addAttribute("order", order);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("orderItem", orderItem);
        model.addAttribute("billingAddress", billingAddress);

        return "/user/complete-checkout";
    }


    @PostMapping("/cart/checkout/{uId}")
    public String checkoutCart(@PathVariable("uId") Long id,
                               @RequestParam PaymentMethod paymentMethod,
                               @ModelAttribute BillingAddress billingAddress,
                               Principal principal) {
        Optional<User> users = userService.findByEmail(principal.getName());
        User user = new User();
        if (users.isPresent()) {
            user = users.get();
        }
        Cart cart = user.getCart();

        if (cart.getItems().isEmpty()) {
            return "redirect:/user/cart?empty=true"; // or show a message
        }

        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        // Create payment
        Payment payment = Payment.builder()
                .amount(totalAmount)
                .paymentDate(LocalDateTime.now())
                .method(paymentMethod)
                .status(PaymentStatus.COMPLETED)
                .build();

        // Create order
        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.COMPLETED)
                .shippingStatus(ShippingStatus.NOT_SHIPPED)
                .totalAmount(totalAmount)
                .user(user)
                .payment(payment)
                .build();

        // Fix detached entity here too
        payment.setOrder(order);

        orderRepository.save(order);

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getProduct().getPrice() * cartItem.getQuantity())
                    .build();
            orderItems.add(orderItem);

            // Optional: reduce product stock
            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productService.save(product); // if you have this method
        }

        order.setItems(orderItems);

        // Save billing address
        billingAddress.setOrder(order);
        billingAddressRepository.save(billingAddress);
        order.setBillingAddress(billingAddress);

        orderRepository.save(order); // final save

        // Clear cart
        cart.getItems().clear();
        cartService.save(cart); // if you have a cartService

        return "redirect:/user/dashboard";
    }

}
