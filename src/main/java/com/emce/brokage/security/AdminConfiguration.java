package com.emce.brokage.security;

import com.emce.brokage.auth.CustomerRepository;
import com.emce.brokage.auth.UserService;
import com.emce.brokage.auth.entity.Customer;
import com.emce.brokage.auth.entity.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminConfiguration {

    @Bean
    public CommandLineRunner init(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (customerRepository.findByEmail("admin").isEmpty()) {
                customerRepository.save(Customer.builder()
                        .name("admin")
                        .email("admin")
                        .password(passwordEncoder.encode("adminpw"))
                        .role(Role.ADMIN)
                        .build());
            }
        };
    }
}
