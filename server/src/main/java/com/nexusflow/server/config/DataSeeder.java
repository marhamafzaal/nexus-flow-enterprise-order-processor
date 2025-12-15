package com.nexusflow.server.config;

import com.nexusflow.server.entity.Product;
import com.nexusflow.server.entity.Role;
import com.nexusflow.server.entity.User;
import com.nexusflow.server.repository.ProductRepository;
import com.nexusflow.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedProducts();
    }

    private void seedAdmin() {
        userRepository.findByUsername("admin").ifPresentOrElse(
                user -> log.info("Admin user already exists"),
                () -> {
                    User admin = User.builder()
                            .username("admin")
                            .password(passwordEncoder.encode("admin123"))
                            .role(Role.ROLE_ADMIN)
                            .build();
                    userRepository.save(admin);
                    log.info("Seeded default admin user (admin/admin123)");
                }
        );
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            log.info("Products already seeded");
            return;
        }

        List<Product> products = List.of(
                Product.builder().name("Laptop Pro 14").price(new BigDecimal("1899.00")).quantity(15).build(),
                Product.builder().name("Noise-Canceling Headphones").price(new BigDecimal("299.00")).quantity(40).build(),
                Product.builder().name("4K Monitor 27\"").price(new BigDecimal("449.00")).quantity(25).build(),
                Product.builder().name("Mechanical Keyboard").price(new BigDecimal("129.00")).quantity(35).build()
        );

        productRepository.saveAll(products);
        log.info("Seeded sample products");
    }
}