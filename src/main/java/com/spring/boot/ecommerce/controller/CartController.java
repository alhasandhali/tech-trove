package com.spring.boot.ecommerce.controller;

import com.spring.boot.ecommerce.model.*;
import com.spring.boot.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping("/user/cart")
public class CartController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    @PostMapping("/add/{pId}")
    public String addToCart(@PathVariable("pId") Long productId,
                            @RequestParam int quantity,
                            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = user.getCart();
        if (cart == null) {
            cart = Cart.builder()
                    .user(user)
                    .totalPrice(0.0)
                    .items(new ArrayList<>())  // initialize items list here
                    .build();
            cart = cartRepository.save(cart);
            user.setCart(cart);
            userRepository.save(user);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(item.getQuantity() * product.getPrice());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice() * quantity)
                    .build();
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }

        // Update total price
        double totalPrice = cart.getItems().stream()
                .mapToDouble(CartItem::getPrice)
                .sum();
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart);

        return "redirect:/user/cart/view";
    }

    @GetMapping("/view")
    public String viewCart(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUserIdWithItems(user.getId());
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
        }
        System.err.println(cart);

        LocalDateTime createdAt = user.getCreatedAt();

        // Format LocalDateTime to a String
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedDate = createdAt.format(formatter);

        model.addAttribute("cart", cart);
        model.addAttribute("user", user);
        model.addAttribute("createdAt", formattedDate);
        model.addAttribute("title", "Cart - " + user.getFullName());
        return "user/cart/view";
    }

    @PostMapping("/remove/{itemId}")
    public String removeFromCart(@PathVariable("itemId") Long itemId,
                                 Principal principal) {

        String username = principal.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = user.getCart();
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            System.out.println("cart is null");
            return "redirect:/user/cart/view";
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        System.out.println(item);

        // Remove item and update cart total
        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        System.out.println(cart);
        System.out.println(item);
        System.out.println();

        double totalPrice = cart.getItems().stream()
                .mapToDouble(CartItem::getPrice)
                .sum();
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart);

        return "redirect:/user/cart/view";
    }

}

