package com.example.demo.service;

import com.example.demo.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor

@Service
public class UserServiceImpl implements UserService, ReactiveUserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @PreAuthorize("hasAuthority('user:read')")
    @Override
    public Mono<User> getUserByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId)
                .flatMap(this::populateUserWithRolesAndAuthorities)
                .switchIfEmpty(Mono.error(() -> new RuntimeException("User doesn't exist")) );
    }

    @PreAuthorize("hasAuthority('user:read')")
    @Override
    @Deprecated
    public Flux<User> getUsers() {
        return userRepository.findAll().flatMap(this::populateUserWithRolesAndAuthorities);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @Override
    public Mono<Page<User>> getUsers(PageRequest pageRequest) {
        return userRepository.findAllBy(pageRequest)
                .flatMap(this::populateUserWithRolesAndAuthorities)
                .collectList()
                .zipWith(userRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }

    @Override
    public Mono<User> registerUser(User user){
        // workaround to register new user setting role to be null(for default to be set) and without needing 'user:create' authority
        user.setRoleId(null);
        return this.createUser(user);
    }

    @PreAuthorize("hasAuthority('user:create')")
    @Override
    public Mono<User> createUser(User user) {
        // default roleId of ROLE_USER (if no role provided)
        if(user.getRoleId() == null) user.setRoleId(1L);

        UserValidator.ValidationResult result = isUserValid(user);
        if(result != UserValidator.ValidationResult.SUCCESS){
            return Mono.error(() -> new RuntimeException("Submitted user is not valid: "+result.getReason()));
        }

        Mono<User> userMono = Mono.just(user);
        return userMono
                .filterWhen(this::userNotExistsInDBByUsername)
                .switchIfEmpty(Mono.error(() -> new RuntimeException("User with provided username already exists")))
                .zipWith(roleRepository.findById(user.getRoleId()))
                .switchIfEmpty(Mono.error(() -> new RuntimeException("Role provided does not exist")))
                .map(tuple -> {
                    tuple.getT1().setRoleId(tuple.getT2().getId());
                    return tuple.getT1();
                })
                .map(userToSave -> {
                    userToSave.setPublicId(UUID.randomUUID());
                    userToSave.setPassword(passwordEncoder.encode(userToSave.getPassword()));

                    user.setCreatedDate(LocalDateTime.now());
                    user.setLastModifiedDate(LocalDateTime.now());
                    return userToSave;
                })
                .flatMap(userRepository::save)
                .flatMap(this::populateUserWithRolesAndAuthorities)
                .switchIfEmpty(Mono.error(() -> new RuntimeException("User with provided username already exists")) );
    }

    @PreAuthorize("hasAuthority('user:update')")
    @Override
    public Mono<User> updateUser(User user, String publicId) {
        UserValidator.ValidationResult result = isUserValid(user);
        if(result != UserValidator.ValidationResult.SUCCESS){
            return Mono.error(() -> new RuntimeException("Submitted user is not valid: "+result.name()));
        }

        return roleRepository.findById(user.getRoleId())
                .switchIfEmpty(Mono.error(() -> new RuntimeException("Role provided does not exist")))
                .zipWith(userRepository.findByPublicId(publicId))
                .map(tuple -> {
                    tuple.getT2().setRoleId(tuple.getT1().getId());
                    return tuple.getT2();
                })
                .flatMap(existingUser -> {
                        user.setId(existingUser.getId());
                        user.setPublicId(existingUser.getPublicId());
                        user.setLastModifiedDate(LocalDateTime.now());
                        user.setUsername(existingUser.getUsername());  // prevent username change
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                        user.setRoleId(existingUser.getRoleId());
                        return userRepository.save(user);
                })
                .switchIfEmpty(Mono.error(() -> new RuntimeException("User doesn't exist")) );
    }

    @PreAuthorize("hasAuthority('user:delete')")
    @Override
    public Mono<Void> deleteUser(String publicId) {
        return userRepository.deleteByPublicId(publicId);
    }

//    @PreAuthorize("hasAuthority('user:update')") // added to security config instead
    @Override
    public Mono<User> patchUser(User user, String publicId) {
        return userRepository
                .findByPublicId(publicId)
                .flatMap(existingUser -> {
                    if(user.getPassword() != null){
                        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                    if(user.getFirstName() != null){
                        existingUser.setFirstName(user.getFirstName());
                    }
                    if(user.getLastName() != null){
                        existingUser.setLastName(user.getLastName());
                    }
                    if(user.getRoleId() != null){
                        existingUser.setRoleId(user.getRoleId());
                    }
                    existingUser.setEnabled(user.isEnabled());

                    return userRepository.save(existingUser);
                })
                .switchIfEmpty(Mono.error(() -> new RuntimeException("User doesn't exist")) );
    }

    //async predicate
    private Mono<Boolean> userNotExistsInDBByUsername(User user){
        return userRepository.findByUsername(user.getUsername())
                .flatMap(userFromDB -> Mono.just(false))
                .switchIfEmpty(Mono.just(true));
    }

    private UserValidator.ValidationResult isUserValid(User user){
        UserValidator.ValidationResult result = UserValidator
                .isUsernameValid()
                .and(UserValidator.isPasswordValid())
                .and(UserValidator.isFirstNameValid())
                .and(UserValidator.isLastNameValid())
                .and(UserValidator.isRoleProvided())
                .apply(user);
        return result;
    }

    // user must have roleId
    private Mono<User> populateUserWithRolesAndAuthorities(User user){
        if(user.getRoleId() == null) return Mono.just(user);
        Long roleId = user.getRoleId();
        return roleRepository.getAuthoritiesByRoleId(roleId)
                .collectList()
                .zipWith(roleRepository.findById(roleId))
                .map(tuple -> {
                    tuple.getT2().setAuthorities(tuple.getT1());
                    return tuple.getT2();
                }).map(role -> {
                    user.setRole(role);
                    return user;
                });
    }

    @Override
    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(this::populateUserWithRolesAndAuthorities)
                .switchIfEmpty(Mono.error(() -> new RuntimeException("User doesn't exist")) );
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(this::populateUserWithRolesAndAuthorities)
                .cast(UserDetails.class)
                .switchIfEmpty(Mono.error(() -> new RuntimeException("User doesn't exist")) );
    }
}
