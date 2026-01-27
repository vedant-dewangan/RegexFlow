package com.regexflow.backend.Config;

import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    // Default admin configuration from application.properties
    @Value("${admin.default.name:System Admin}")
    private String defaultAdminName;

    @Value("${admin.default.email:admin@regexflow.com}")
    private String defaultAdminEmail;

    @Value("${admin.default.password:admin123}")
    private String defaultAdminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Check if any admin exists
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == UserRole.ADMIN);

        // If no admin exists, create a default one
        if (!adminExists) {
            Users defaultAdmin = new Users();
            defaultAdmin.setName(defaultAdminName);
            defaultAdmin.setEmail(defaultAdminEmail);
            defaultAdmin.setPasswordHash(passwordEncoder.encode(defaultAdminPassword));
            defaultAdmin.setRole(UserRole.ADMIN);

            userRepository.save(defaultAdmin);
            System.out.println("==========================================");
            System.out.println("Default ADMIN user created!");
            System.out.println("Name: " + defaultAdminName);
            System.out.println("Email: " + defaultAdminEmail);
            System.out.println("Password: " + defaultAdminPassword);
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
