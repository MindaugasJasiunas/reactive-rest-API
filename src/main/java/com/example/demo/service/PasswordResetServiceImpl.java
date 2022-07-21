package com.example.demo.service;

import com.example.demo.PasswordReset;
import com.example.demo.repository.PasswordResetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor

@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private final PasswordResetRepository passwordResetRepository;

    @Override
    public Mono<Void> deletePasswordResetByLink(String link){
        return passwordResetRepository.deleteByLink(link);
    }

    @Override
    public Mono<PasswordReset> getUsernameByPasswordResetLink(String link) {
        return passwordResetRepository.findByLink(link);
    }

    @Override
    public Mono<PasswordReset> createPasswordResetLink(String username){
        return passwordResetRepository.findByUsername(username)
                .switchIfEmpty(generateAndSaveNewPasswordReset(username));
    }

    private Mono<PasswordReset> generateAndSaveNewPasswordReset(String username){
        PasswordReset pr = new PasswordReset();
        pr.setUsername(username);
        pr.setLink(UUID.randomUUID().toString());
        return passwordResetRepository.save(pr);
    }
}
