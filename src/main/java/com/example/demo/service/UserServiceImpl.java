package com.example.demo.service;

import com.example.demo.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    public Mono<User> getUserByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId);
    }

    @Override
    public Flux<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public Mono<User> createUser(User user) {
        Mono<User> userMono = Mono.just(user);
        return userMono
                .filterWhen(this::userNotExistsInDBByUsername)
                .map(userToSave -> {
                    // set publicId
                    userToSave.setPublicId(UUID.randomUUID());

                    user.setCreatedDate(LocalDateTime.now());
                    user.setLastModifiedDate(LocalDateTime.now());
                    return userToSave;
                })
                .flatMap(userRepository::save)
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<User> updateUser(User user, String publicId) {
        return userRepository
                .findByPublicId(publicId)
                .flatMap(existingUser -> {
                    user.setId(existingUser.getId());
                    user.setPublicId(existingUser.getPublicId());
                    user.setLastModifiedDate(LocalDateTime.now());
                    // prevent username change
                    user.setUsername(existingUser.getUsername());
                    return userRepository.save(user);
                })
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<Void> deleteUser(String publicId) {
        return userRepository.deleteByPublicId(publicId);
    }

    @Override
    public Mono<User> patchUser(User user, String publicId) {
        return userRepository
                .findByPublicId(publicId)
                .flatMap(existingUser -> {
                    if(user.getPassword() != null){
                        existingUser.setPassword(user.getPassword());
                    }
                    if(user.getFirstName() != null){
                        existingUser.setFirstName(user.getFirstName());
                    }
                    if(user.getLastName() != null){
                        existingUser.setLastName(user.getLastName());
                    }
                    return userRepository.save(existingUser);
                })
                .switchIfEmpty(Mono.empty());
    }

    //async predicate
    private Mono< Boolean> userNotExistsInDBByUsername(User user){
        return userRepository.findByUsername(user.getUsername())
                .flatMap(userFromDB -> Mono.just(false))
                .switchIfEmpty(Mono.just(true));
    }

}
