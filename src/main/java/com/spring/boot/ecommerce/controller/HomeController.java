package com.spring.boot.ecommerce.controller;

import com.spring.boot.ecommerce.helper.Message;
import com.spring.boot.ecommerce.model.Brand;
import com.spring.boot.ecommerce.model.Category;
import com.spring.boot.ecommerce.model.Product;
import com.spring.boot.ecommerce.model.User;
import com.spring.boot.ecommerce.repository.ProductRepository;
import com.spring.boot.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;


    //Home handler
    @RequestMapping({"/", "/home"})
    public String home(Model model){
        model.addAttribute("title", "Home - TechTrove");
        List<Product> products = this.productRepository.findAll();
        model.addAttribute("products", products);

        return "home";
    }

    //Product details handler
    @RequestMapping("/product/{pId}")
    public String viewContact(@PathVariable("pId") Long pId,
                              Model model) {

        Optional<Product> products = this.productRepository.findById(pId);
        Product product = new Product();

        if (products.isPresent()) {
            product = products.get();
        }

        model.addAttribute("title", "TechTrove - " + product.getName());
        model.addAttribute("product", product);

        return "product-details";
    }

    //Product details handler
    @RequestMapping("/customer/{uId}")
    public String viewUser(@PathVariable("uId") Long uId,
                              Model model) {

        Optional<User> users = this.userRepository.findById(uId);
        User user = new User();

        if (users.isPresent()) {
            user = users.get();
        }

        model.addAttribute("title", "TechTrove - " + user.getFullName());
        model.addAttribute("user", user);

        return "user-details";
    }

    //Sign up handler
    @RequestMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "SignUp - TechTrove");
        return "signup-form";
    }

    //Sign up form process handler
    @PostMapping("/process-signup-form")
    public String processSignupForm(@ModelAttribute("user") User user,
                                    @RequestParam("productImage") MultipartFile file,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (file != null && !file.isEmpty()) {
            try{
                String fileName = file.getOriginalFilename();
                user.setImageUrl(fileName);

                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
                user.setCreatedAt(LocalDateTime.now());


                // Save file to disk (you can change path)
                File saveFile = new ClassPathResource("static/images/").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                //Save into Database
                this.userRepository.save(user);

                System.out.println(user);

                System.out.println("Added to database!!!");

                /* Success message */
                redirectAttributes.addFlashAttribute("message", new Message("Product added successfully!", "success"));
            } catch (IOException e) {
                e.printStackTrace();
                /* Error message */
                redirectAttributes.addFlashAttribute("message", new Message("Something went wrong!", "error"));
            }
        } else {
            //product.setImageUrl("default.jpg");
            /* Error message */
            redirectAttributes.addFlashAttribute("message", new Message("Something went wrong!", "danger"));
        }
        model.addAttribute("title", "SignUp - TechTrove");
        return "redirect:/login";
    }

    //Log in handler
    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "LogIn - TechTrove");
        return "login";
    }

    //Searching Handler
    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model) {
        String normalized = query.trim().toUpperCase();

        List<Product> results = new ArrayList<>();

        // 1. Search by name
        results.addAll(productRepository.findByNameContainingIgnoreCase(query));

        // 2. Try to match Brand
        try {
            Brand brand = Brand.valueOf(normalized);
            List<Product> brandMatches = productRepository.findAll()
                    .stream()
                    .filter(p -> p.getBrand() == brand)
                    .toList();
            results.addAll(brandMatches);
        } catch (IllegalArgumentException ignored) {}

        // 3. Try to match Category
        try {
            Category category = Category.valueOf(normalized);
            List<Product> categoryMatches = productRepository.findAll()
                    .stream()
                    .filter(p -> p.getCategory() == category)
                    .toList();
            results.addAll(categoryMatches);
        } catch (IllegalArgumentException ignored) {}

        // Optional: Remove duplicates
        List<Product> uniqueResults = results.stream()
                .distinct()
                .toList();

        model.addAttribute("products", uniqueResults);
        model.addAttribute("searchQuery", query);
        return "search-results";
    }

    //Sorting and Filtering Handler
    @GetMapping("/filter/category/{category}")
    public String filterByCategory(@PathVariable String category, Model model) {
        Category cat = Category.valueOf(category.toUpperCase());
        List<Product> products = productRepository.findAll()
                .stream().filter(p -> p.getCategory() == cat).toList();
        model.addAttribute("products", products);
        return "home";
    }

    @GetMapping("/filter/brand/{brand}")
    public String filterByBrand(@PathVariable String brand, Model model) {
        Brand br = Brand.valueOf(brand.toUpperCase());
        List<Product> products = productRepository.findAll()
                .stream().filter(p -> p.getBrand() == br).toList();
        model.addAttribute("products", products);
        return "home";
    }

    @GetMapping("/sort/price/{order}")
    public String sortByPrice(@PathVariable String order, Model model) {
        List<Product> products = productRepository.findAll();
        if (order.equalsIgnoreCase("asc")) {
            products = products.stream().sorted(Comparator.comparing(Product::getPrice)).toList();
        } else {
            products = products.stream().sorted(Comparator.comparing(Product::getPrice).reversed()).toList();
        }
        model.addAttribute("products", products);
        return "home";
    }

    @GetMapping("/sort/date/{order}")
    public String sortByDate(@PathVariable String order, Model model) {
        List<Product> products = productRepository.findAll();
        if (order.equalsIgnoreCase("newest")) {
            products = products.stream().sorted(Comparator.comparing(Product::getCreatedAt).reversed()).toList();
        } else {
            products = products.stream().sorted(Comparator.comparing(Product::getCreatedAt)).toList();
        }
        model.addAttribute("products", products);
        return "home";
    }

}
