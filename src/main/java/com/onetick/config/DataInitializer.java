package com.onetick.config;

import com.onetick.entity.Role;
import com.onetick.entity.enums.RoleName;
import com.onetick.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public ApplicationRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            Arrays.stream(RoleName.values()).forEach(roleName -> {
                roleRepository.findByName(roleName).orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    Role saved = roleRepository.save(role);
                    log.info("Seeded role {}", saved.getName());
                    return saved;
                });
            });
        };
    }
}
