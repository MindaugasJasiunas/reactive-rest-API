package com.example.demo;

import com.example.demo.repository.UserRepository;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@EnableDiscoveryClient
@EnableConfigurationProperties({JwtTokenProvider.class})
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	UserRepository userRepo;
//	UserService userService;
	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			// initial users
//			User simpleUser = new User(null, UUID.randomUUID(), "johndoe", "password", "John", "Doe", LocalDateTime.now(), LocalDateTime.now(), 1L, null, true, true, true, true);
//			userRepo.save(simpleUser).subscribe();
//
//			User adminUser = new User(null, UUID.randomUUID(), "admin", "password", "Admin", "Admin", LocalDateTime.now(), LocalDateTime.now(), 3L, null, true, true, true, true);
//			userRepo.save(adminUser).subscribe();
//
//			User courier = new User(null, UUID.randomUUID(), "janedoe", "password", "Jane", "Doe", LocalDateTime.now(), LocalDateTime.now(), 4L, null, true, true, true, true);
//			userRepo.save(courier).subscribe();
		};
	}


}
