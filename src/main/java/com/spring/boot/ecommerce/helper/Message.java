package com.spring.boot.ecommerce.helper;

import lombok.*;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String content;
    private String type;
}
