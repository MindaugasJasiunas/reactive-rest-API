package com.example.demo.service;

import com.example.demo.PasswordReset;
import reactor.core.publisher.Mono;

public interface PasswordResetService {
    Mono<Void> deletePasswordResetByLink(String link);
    Mono<PasswordReset> getUsernameByPasswordResetLink(String link);
    Mono<PasswordReset> createPasswordResetLink(String username);
}
