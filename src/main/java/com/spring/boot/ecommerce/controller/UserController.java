package com.spring.boot.ecommerce.controller;

import com.spring.boot.ecommerce.helper.Message;
import com.spring.boot.ecommerce.model.Order;
import com.spring.boot.ecommerce.model.OrderItem;
import com.spring.boot.ecommerce.model.Product;
import com.spring.boot.ecommerce.model.User;
import com.spring.boot.ecommerce.repository.OrderRepository;
import com.spring.boot.ecommerce.repository.ProductRepository;
import com.spring.boot.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;

    @RequestMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Optional<User> user = userRepository.findByEmail(principal.getName());
        User currentUser = new User();
        if (user.isPresent()) {
            currentUser = user.get();
        }

        List<Order> orders = orderRepository.findByUser(currentUser);

        LocalDateTime createdAt = user.get().getCreatedAt();

        // Format LocalDateTime to a String
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedDate = createdAt.format(formatter);

        model.addAttribute("title", currentUser.getFullName() + " - TechTrove");
        model.addAttribute("user", currentUser);
        model.addAttribute("createdAt", formattedDate);
        model.addAttribute("currentUser", user);
        model.addAttribute("orders", orders);

        return "/user/dashboard";
    }

    @PostMapping("/process-update-form")
    public String processUpdateForm(@ModelAttribute("user") User user,
                                    @RequestParam("productImage") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userRepository.findById(user.getId()).orElseThrow();

            // Handle password
            if (!existingUser.getPassword().equals(user.getPassword())) {
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            }

            // Handle image
            if (file != null && !file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                user.setImageUrl(fileName);
                File saveFile = new ClassPathResource("static/images/").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath(), fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            } else {
                user.setImageUrl(existingUser.getImageUrl());
                System.err.println(existingUser.getImageUrl());
            }

            user.setCreatedAt(existingUser.getCreatedAt());// preserve creation date
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.err.println(user);

            System.out.println("Updated successfully");

            redirectAttributes.addFlashAttribute("message", new Message("User updated successfully!", "success"));
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", new Message("Update failed!", "error"));
        }

        return "redirect:/user/dashboard";
    }

    @GetMapping("/order-details/{orderId}")
    public String getOrderDetails(@PathVariable("orderId") Long orderId, Model model, Principal principal) {
        // Get user details
        Optional<User> user = userRepository.findByEmail(principal.getName());
        User currentUser = new User();
        if (user.isPresent()) {
            currentUser = user.get();
        }

        // Fetch order
        Optional<Order> order = orderRepository.findById(orderId);
        Order currentOrder = new Order();
        if (order.isPresent()) {
            currentOrder = order.get();
        }

        // Optional: Validate if the order belongs to the current user
        if (!currentOrder.getUser().getId().equals(currentUser.getId())) {
            return "error/403"; // or redirect to error page
        }

        // Assuming 1 order has 1 item for now (adjust if multiple)
        List<OrderItem> orderItem = currentOrder.getItems();

//        System.out.println(currentOrder.getBillingAddress());
//        System.out.println(currentOrder.getPayment().getMethod());

        // Add attributes
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("order", currentOrder);
        model.addAttribute("orderItem", orderItem);
        model.addAttribute("totalPrice", currentOrder.getTotalAmount());
        model.addAttribute("billingAddress", currentOrder.getBillingAddress());
        model.addAttribute("title", "Order Details - " + currentUser.getFullName());

        return "user/order-details"; // Thymeleaf view name
    }

}
