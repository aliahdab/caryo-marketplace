package com.autotrader.autotraderbackend.config;

import com.autotrader.autotraderbackend.model.Role;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.repository.RoleRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import com.autotrader.autotraderbackend.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Initializes default data in the database at application startup.
 * This ensures development users are always available, even after rebuilds.
 * Creates two users: one with regular user role and one with admin role.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    // Regular user credentials
    private static final String USER_USERNAME = "user";
    private static final String USER_EMAIL = "user@autotrader.com";
    private static final String USER_PASSWORD = "Password123!";
    
    // Admin user credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@autotrader.com";
    private static final String ADMIN_PASSWORD = "Admin123!";

    @Override
    public void run(String... args) {
        User regularUser = createRegularUser();
        User adminUser = createAdminUser();
        generateAndPrintDevTokens(regularUser, adminUser);
    }
    
    private User createRegularUser() {
        // Check if user already exists
        User user = null;
        
        try {
            if (!userRepository.existsByUsername(USER_USERNAME)) {
                log.info("Creating regular development user: {}", USER_USERNAME);
                
                // Create the user
                user = new User(USER_USERNAME, USER_EMAIL, passwordEncoder.encode(USER_PASSWORD));
                
                // Set role (only USER role)
                Set<Role> roles = new HashSet<>();
                roleRepository.findByName("ROLE_USER").ifPresent(roles::add);
                // If role doesn't exist, create it
                if (roles.isEmpty()) {
                    try {
                        Role userRole = new Role("ROLE_USER");
                        userRole = roleRepository.save(userRole);
                        roles.add(userRole);
                    } catch (Exception e) {
                        // Role might have been created by another process
                        log.warn("Error creating ROLE_USER, trying to fetch it again: {}", e.getMessage());
                        roleRepository.findByName("ROLE_USER").ifPresent(roles::add);
                    }
                }
                
                if (roles.isEmpty()) {
                    log.error("Failed to create or retrieve ROLE_USER");
                    return null;
                }
                
                user.setRoles(roles);
                
                // Save the user
                user = userRepository.save(user);
                log.info("Regular user created successfully");
            } else {
                log.info("Regular user already exists: {}", USER_USERNAME);
                try {
                    user = userRepository.findByUsername(USER_USERNAME).orElse(null);
                    if (Objects.isNull(user)) {
                        log.error("User exists but couldn't be retrieved: {}", USER_USERNAME);
                    }
                } catch (Exception e) {
                    log.error("Error retrieving existing user {}: {}", USER_USERNAME, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error creating or retrieving regular user: {}", e.getMessage());
        }
        
        return user;
    }
    
    private User createAdminUser() {
        // Check if admin user already exists
        User admin = null;
        
        try {
            if (!userRepository.existsByUsername(ADMIN_USERNAME)) {
                log.info("Creating admin development user: {}", ADMIN_USERNAME);
                
                // Create the user
                admin = new User(ADMIN_USERNAME, ADMIN_EMAIL, passwordEncoder.encode(ADMIN_PASSWORD));
                
                // Set roles (ADMIN and USER roles)
                Set<Role> roles = new HashSet<>();
                
                try {
                    // Find or create USER role
                    Role userRole = roleRepository.findByName("ROLE_USER").orElse(null);
                    if (Objects.isNull(userRole)) {
                        userRole = new Role("ROLE_USER");
                        userRole = roleRepository.save(userRole);
                    }
                    roles.add(Objects.requireNonNull(userRole, "User role cannot be null"));
                    
                    // Find or create ADMIN role
                    Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
                    if (Objects.isNull(adminRole)) {
                        adminRole = new Role("ROLE_ADMIN");
                        adminRole = roleRepository.save(adminRole);
                    }
                    roles.add(Objects.requireNonNull(adminRole, "Admin role cannot be null"));
                } catch (Exception e) {
                    log.error("Error creating or retrieving roles: {}", e.getMessage());
                    return null;
                }
                
                admin.setRoles(roles);
                
                // Save the user
                admin = userRepository.save(admin);
                log.info("Admin user created successfully");
            } else {
                log.info("Admin user already exists: {}", ADMIN_USERNAME);
                try {
                    admin = userRepository.findByUsername(ADMIN_USERNAME).orElse(null);
                    if (Objects.isNull(admin)) {
                        log.error("Admin user exists but couldn't be retrieved: {}", ADMIN_USERNAME);
                    }
                } catch (Exception e) {
                    log.error("Error retrieving existing admin user {}: {}", ADMIN_USERNAME, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error creating or retrieving admin user: {}", e.getMessage());
        }
        
        return admin;
    }
    
    private void generateAndPrintDevTokens(User regularUser, User adminUser) {
        try {
            if (Objects.isNull(regularUser)) {
                log.warn("Regular user is null, skipping token generation for regular user");
                return;
            }
            
            if (Objects.isNull(adminUser)) {
                log.warn("Admin user is null, skipping token generation for admin user");
                return;
            }
            
            // Generate token for regular user
            String regularUserToken = generateTokenForUser(regularUser);
            
            // Generate token for admin user
            String adminUserToken = generateTokenForUser(adminUser);
            
            // Print tokens in a nice format
            log.info("\n\n====== DEVELOPMENT AUTHENTICATION TOKENS ======");
            log.info("These tokens can be used for testing without login:");
            log.info("");
            
            if (Objects.nonNull(regularUser) && Objects.nonNull(regularUserToken)) {
                log.info("REGULAR USER TOKEN ({})", regularUser.getUsername());
                log.info("--------------------------------------------");
                log.info("{}", regularUserToken);
                log.info("");
            }
            
            if (Objects.nonNull(adminUser) && Objects.nonNull(adminUserToken)) {
                log.info("ADMIN USER TOKEN ({})", adminUser.getUsername());
                log.info("--------------------------------------------");
                log.info("{}", adminUserToken);
                log.info("");
            }
            
            log.info("To use: Add the following header to your HTTP requests:");
            log.info("Authorization: Bearer <token>");
            log.info("==============================================\n");
        } catch (Exception e) {
            log.error("Error generating development tokens", e);
        }
    }
    
    private String generateTokenForUser(User user) {
        // Check if user is null using Objects utility
        if (Objects.isNull(user)) {
            log.error("Cannot generate token for null user");
            return "TOKEN_GENERATION_FAILED_NULL_USER";
        }
        
        // Check if user roles is null using Objects utility
        if (Objects.isNull(user.getRoles())) {
            log.error("Cannot generate token for user with null roles: {}", user.getUsername());
            return "TOKEN_GENERATION_FAILED_NULL_ROLES";
        }
        
        try {
            // Create authorities from roles - using safe null handling
            Set<Role> roles = Objects.requireNonNull(user.getRoles(), "User roles cannot be null");
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .filter(Objects::nonNull)
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());
            
            // Create a UserDetails object - safe access to required fields
            String username = Objects.requireNonNull(user.getUsername(), "Username cannot be null");
            String password = Objects.requireNonNull(user.getPassword(), "Password cannot be null");
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    username,
                    password,
                    authorities
            );
            
            // Create Authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    authorities
            );
            
            // Generate JWT token
            return jwtUtils.generateJwtToken(authentication);
        } catch (Exception e) {
            log.error("Error generating token for user {}: {}", user.getUsername(), e.getMessage());
            return "TOKEN_GENERATION_FAILED: " + e.getMessage();
        }
    }
}
