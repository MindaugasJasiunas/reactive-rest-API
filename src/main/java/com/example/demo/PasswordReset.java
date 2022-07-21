package com.example.demo;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "password_reset")
public class PasswordReset {
    private String username;
    private String link;

    private String email;  // not used field - for future
}
