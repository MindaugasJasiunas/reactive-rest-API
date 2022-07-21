package com.example.demo.service;

import com.example.demo.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> getUserByPublicId(String publicId);
    Flux<User> getUsers();
    Mono<Page<User>> getUsers(PageRequest pageRequest);
    Mono<User> createUser(User user);
    Mono<User> updateUser(User user, String publicId);
    Mono<Void> deleteUser(String publicId);
    Mono<User> patchUser(User user, String publicId);
    Mono<User> registerUser(User user);
    Mono<User> findUserByUsername(String username);
}
