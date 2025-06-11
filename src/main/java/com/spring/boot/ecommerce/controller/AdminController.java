package com.spring.boot.ecommerce.controller;

import com.spring.boot.ecommerce.helper.Message;
import com.spring.boot.ecommerce.model.*;
import com.spring.boot.ecommerce.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BillingAddressRepository billingAddressRepository;

    @RequestMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Optional<User> user = userRepository.findByEmail(principal.getName());
        User currentUser = new User();
        if (user.isPresent()) {
            currentUser = user.get();
        }

        LocalDateTime createdAt = user.get().getCreatedAt();

        List<Order> orders = orderRepository.findAll();

        // Format LocalDateTime to a String
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedDate = createdAt.format(formatter);


        model.addAttribute("title", currentUser.getFullName() + " - TechTrove");
        model.addAttribute("user", currentUser);
        model.addAttribute("createdAt", formattedDate);
        model.addAttribute("currentUser", user);
        model.addAttribute("orders", orders);
        model.addAttribute("status", ShippingStatus.values());
        return "/admin/dashboard";
    }

    //Add contact handler
    @GetMapping("/add-product")
    public String addContact(Model model, Principal principal) {
        model.addAttribute("title", "Add Product - TechTrove");
        model.addAttribute("product", new Product());
        model.addAttribute("categories", Category.values());
        model.addAttribute("brands", Brand.values());
        return "admin/add-product";
    }

    //Add contact process handler
    @PostMapping("/process-product")
    public String processProduct(@ModelAttribute("product") Product product,
                                 @RequestParam("productImage") MultipartFile file,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                product.setImageUrl(fileName);

                product.setCreatedAt(LocalDateTime.now());


                // Save file to disk (you can change path)
                File saveFile = new ClassPathResource("static/images/").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                this.productRepository.save(product);

                System.out.println(product);

                System.out.println("Added to database!!!");

                /* Success message */
                redirectAttributes.addFlashAttribute("message", new Message("Product added successfully!", "success"));
            } catch (IOException e) {
                e.printStackTrace();
                /* Error message */
                redirectAttributes.addFlashAttribute("message", new Message("Something went wrong!", "error"));
            }
        } else {
            product.setImageUrl("default.jpg");
            /* Error message */
            redirectAttributes.addFlashAttribute("message", new Message("Something went wrong!", "danger"));
        }

        return "redirect:/admin/add-product";
    }

    //View product handler
    @GetMapping("/view-products")
    public String viewProducts(Model model) {
        model.addAttribute("title", "Products List - TechTrove");

        List<Product> products = this.productRepository.findAll();
        model.addAttribute("products", products);
        return "admin/view-products";
    }

    //Delete product
    @GetMapping("/product/delete/{pId}")
    public String deleteProduct(@PathVariable("pId") Long pId, HttpSession session) {
        Optional<Product> optionalProduct = this.productRepository.findById(pId);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();

            //Remove from all wishlists
            List<Wishlist> wishlists = wishlistRepository.findAllByProduct(product);
            for (Wishlist wishlist : wishlists) {
                wishlist.getProducts().remove(product);
                wishlistRepository.save(wishlist);
            }

            //Remove from cart items
            cartItemRepository.deleteByProductId(product.getId());

            //Finally, delete the product itself
            productRepository.delete(product);

            System.out.println(pId);

            session.setAttribute("message", new Message("Product deleted successfully!", "success"));
        } else {
            session.setAttribute("message", new Message("Product not found!", "danger"));
        }

        return "redirect:/admin/view-products";
    }

    @RequestMapping("/product/update/{pId}")
    public String updateProduct(@PathVariable("pId") Long pId, Model model) {
        Optional<Product> pro = this.productRepository.findById(pId);
        Product product = new Product();
        if (pro.isPresent()) {
            product = pro.get();
            model.addAttribute("product", product);
            model.addAttribute("categories", Category.values());
            model.addAttribute("brands", Brand.values());
            model.addAttribute("title", "TechTrove - " + product.getName());
            return "admin/update-product";
        } else {
            // Redirect to error or list page if not found
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/product/update/{pId}")
    public String processUpdateProduct(@PathVariable("pId") Long pId,
                                       @ModelAttribute("product") Product product,
                                       @RequestParam("productImage") MultipartFile file,
                                       @RequestParam("oldImage") String oldImage,
                                       RedirectAttributes redirectAttributes) {
        // Handle image upload logic here...
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                product.setImageUrl(fileName);

                product.setCreatedAt(LocalDateTime.now());

                // Save file to disk (you can change path)
                File saveFile = new ClassPathResource("static/images/").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                this.productRepository.save(product);

                System.out.println("Updated to database!!!");

                /* Success message */
                redirectAttributes.addFlashAttribute("message", new Message("Product added successfully!", "success"));
            } catch (IOException e) {
                e.printStackTrace();
                /* Error message */
                redirectAttributes.addFlashAttribute("message", new Message("Something went wrong!", "error"));
            }
        } else {
            // Save updated product
            // fallback only if oldImage is valid
            if (oldImage != null && !oldImage.trim().isEmpty()) {
                product.setImageUrl(oldImage);
            } else {
                product.setImageUrl("default.jpg"); // or leave as is
            }
            product.setId(pId);
            productRepository.save(product);
            System.out.println("Updated to database!!!");
            /* Success message */
            redirectAttributes.addFlashAttribute("message", new Message("Product added successfully!", "success"));
        }
        return "redirect:/admin/view-products";
    }

    //View users handler
    @GetMapping("/view-users")
    public String viewUsers(Model model) {
        model.addAttribute("title", "Users List - TechTrove");

        List<User> users = this.userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/view-users";
    }

    @PostMapping("/users/{id}/update-role-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUserRoleAndStatus(@PathVariable("id") Long id,
                                          @RequestParam("role") String role,
                                          @RequestParam("enabled") boolean enabled,
                                          RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(Role.valueOf(role));
            user.setEnabled(enabled);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "✅ User updated successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ User not found.");
        }
        return "redirect:/admin/view-users";
    }

    @PostMapping("/update-status")
    public String updateShippingStatus(@RequestParam("orderId") Long orderId,
                                       @RequestParam("status") String status) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            try {
                order.setShippingStatus(ShippingStatus.valueOf(status));
                orderRepository.save(order);
                System.out.println("Updated to database!!!");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Order not found!!!");
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/order-details/{orderId}")
    public String getOrderDetails(@PathVariable("orderId") Long orderId, Model model, Principal principal) {

        // Fetch order
        Optional<Order> orders = orderRepository.findById(orderId);
        Order order = new Order();
        if (orders.isPresent()) {
            order = orders.get();
        }

        List<OrderItem> items = order.getItems();

        model.addAttribute("customer", order.getUser().getFullName());
        model.addAttribute("order", order);
        model.addAttribute("billingAddress", order.getBillingAddress());
        model.addAttribute("orderItem", items);


        return "admin/order-details"; // Thymeleaf view name
    }
}
