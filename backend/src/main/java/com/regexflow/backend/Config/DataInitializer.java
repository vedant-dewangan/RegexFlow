package com.regexflow.backend.Config;

import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * This class runs when the application starts
 * It creates a default ADMIN user if no admin exists
 * This solves the "chicken and egg" problem - you need an admin to create admins!
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if any admin exists
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == UserRole.ADMIN);

        // If no admin exists, create a default one
        if (!adminExists) {
            Users defaultAdmin = new Users();
            defaultAdmin.setName("System Admin");
            defaultAdmin.setEmail("admin@regexflow.com");
            defaultAdmin.setPasswordHash(passwordEncoder.encode("admin123")); // Default password
            defaultAdmin.setRole(UserRole.ADMIN);

            userRepository.save(defaultAdmin);
            System.out.println("==========================================");
            System.out.println("Default ADMIN user created!");
            System.out.println("Email: admin@regexflow.com");
            System.out.println("Password: admin123");
            System.out.println("Please change this password after first login!");
            System.out.println("==========================================");
        }

        // Optional: Create sample MAKER and CHECKER users for testing
        // Uncomment below if you want sample users created automatically
        
        /*
        if (!userRepository.existsByEmail("maker@regexflow.com")) {
            Users maker = new Users();
            maker.setName("Test Maker");
            maker.setEmail("maker@regexflow.com");
            maker.setPasswordHash(passwordEncoder.encode("maker123"));
            maker.setRole(UserRole.MAKER);
            userRepository.save(maker);
            System.out.println("Sample MAKER user created: maker@regexflow.com / maker123");
        }

        if (!userRepository.existsByEmail("checker@regexflow.com")) {
            Users checker = new Users();
            checker.setName("Test Checker");
            checker.setEmail("checker@regexflow.com");
            checker.setPasswordHash(passwordEncoder.encode("checker123"));
            checker.setRole(UserRole.CHECKER);
            userRepository.save(checker);
            System.out.println("Sample CHECKER user created: checker@regexflow.com / checker123");
        }
        */
    }
}
