package com.onetick.config;

import com.onetick.entity.Role;
import com.onetick.entity.User;
import com.onetick.entity.Workspace;
import com.onetick.entity.enums.RoleName;
import com.onetick.repository.RoleRepository;
import com.onetick.repository.UserRepository;
import com.onetick.repository.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Set;

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

    @Bean
    public ApplicationRunner seedDefaultWorkspace(WorkspaceRepository workspaceRepository) {
        return args -> workspaceRepository.findByCode("DEFAULT").orElseGet(() -> {
            Workspace workspace = new Workspace();
            workspace.setCode("DEFAULT");
            workspace.setName("Default Workspace");
            Workspace saved = workspaceRepository.save(workspace);
            log.info("Seeded workspace code={}", saved.getCode());
            return saved;
        });
    }

    @Bean
    public ApplicationRunner seedAdmin(RoleRepository roleRepository,
                                       UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       @Value("${app.bootstrap.admin.enabled:true}") boolean enabled,
                                       @Value("${app.bootstrap.admin.email:admin@onetick.local}") String email,
                                       @Value("${app.bootstrap.admin.name:Onetick Admin}") String name,
                                       @Value("${app.bootstrap.admin.password:admin12345}") String password) {
        return args -> {
            if (!enabled) {
                return;
            }

            if (userRepository.findByEmail(email).isPresent()) {
                return;
            }

            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ADMIN role not seeded"));

            User adminUser = new User();
            adminUser.setEmail(email);
            adminUser.setName(name);
            adminUser.setPasswordHash(passwordEncoder.encode(password));
            adminUser.setActive(true);
            adminUser.setRoles(Set.of(adminRole));
            userRepository.save(adminUser);
            log.warn("Bootstrapped admin account email={}. Change credentials immediately in non-local environments.", email);
        };
    }
}
