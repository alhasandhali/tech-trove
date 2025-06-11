package com.spring.boot.ecommerce.controller;

import com.spring.boot.ecommerce.model.Product;
import com.spring.boot.ecommerce.model.User;
import com.spring.boot.ecommerce.model.Wishlist;
import com.spring.boot.ecommerce.repository.ProductRepository;
import com.spring.boot.ecommerce.repository.UserRepository;
import com.spring.boot.ecommerce.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("/user/wishlist")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    public WishlistController(WishlistRepository wishlistRepository,
                              ProductRepository productRepository,
                              UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/add/{productId}")
    public String addToWishlist(@PathVariable Long productId, Principal principal) {
        // Get the logged-in user
        String username = principal.getName();
        Optional<User> u = userRepository.findByEmail(username); // Adjust if you use username instead of email
        User user = new User();
        if (u.isPresent()) {
            user = u.get();
        }
        // Get or create wishlist
        User finalUser = user;
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(finalUser);
                    return wishlistRepository.save(newWishlist);
                });

        // Add product
        Product product = productRepository.findById(productId).orElseThrow();
        if (!wishlist.getProducts().contains(product)) {
            wishlist.getProducts().add(product);
            wishlistRepository.save(wishlist);
        }

        // Redirect back to product page or wishlist
        return "redirect:/user/wishlist/view";
    }

    @GetMapping("/view")
    public String viewWishlist(Model model, Principal principal) {
        String username = principal.getName();
        Optional<User> u = userRepository.findByEmail(username);
        User user = new User();
        if (u.isPresent()) {
            user = u.get();
        }
        LocalDateTime createdAt = user.getCreatedAt();

        // Format LocalDateTime to a String
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedDate = createdAt.format(formatter);
        // Get or create wishlist
        Wishlist wishlist = wishlistRepository.findByUser(user).orElse(new Wishlist());
        model.addAttribute("user", user);
        model.addAttribute("wishlistProducts", wishlist.getProducts());
        model.addAttribute("title", "Wishlist - " + user.getFullName());
        model.addAttribute("currentUser", user.getFullName());
        model.addAttribute("createdAt", formattedDate);
        return "user/wishlist/view"; // Thymeleaf page
    }

    @GetMapping("/delete/{pId}")
    public String deleteWishlist(@PathVariable("pId") Long pId, Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));

        Product product = productRepository.findById(pId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        wishlist.getProducts().remove(product);
        wishlistRepository.save(wishlist);

        return "redirect:/user/wishlist/view";
    }
}

