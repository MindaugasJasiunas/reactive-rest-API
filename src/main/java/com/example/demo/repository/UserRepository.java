package com.example.demo.repository;

import com.example.demo.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findByPublicId(String publicId);
    Mono<User> findByUsername(String username);
    Mono<Void> deleteByPublicId(String publicId);
    Flux<User> findAllBy(Pageable pageable);
}
