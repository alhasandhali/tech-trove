package com.spring.boot.ecommerce.model;
import lombok.Data;

@Data
public class CheckoutForm {
    private String fullName;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String phone;

    private PaymentMethod paymentMethod;
}

