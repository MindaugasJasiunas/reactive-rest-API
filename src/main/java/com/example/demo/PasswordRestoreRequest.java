package com.example.demo;

import lombok.Data;

@Data
public class PasswordRestoreRequest {
    String password;
    String repeatedPassword;
}
