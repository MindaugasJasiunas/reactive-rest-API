package com.example.demo.repository;

import com.example.demo.PasswordReset;
import com.example.demo.PasswordResetPK;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PasswordResetRepository extends ReactiveCrudRepository<PasswordReset, PasswordResetPK> {
    Mono<PasswordReset> findByLink(String link);
    Mono<PasswordReset> findByUsername(String username);
    Mono<Void> deleteByLink(String link);
}
