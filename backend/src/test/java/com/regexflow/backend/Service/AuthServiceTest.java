package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.LoginRequest;
import com.regexflow.backend.Dto.LoginResponse;
import com.regexflow.backend.Dto.RegisterRequest;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthService authService;

    private Users testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setUId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword123");
        testUser.setRole(UserRole.CUSTOMER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setName("New User");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_WithValidCredentials_ShouldReturnLoginResponse() {
            // Arrange
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
            when(session.getId()).thenReturn("session123");

            // Act
            LoginResponse response = authService.login(loginRequest, session);

            // Assert
            assertNotNull(response);
            assertEquals("Login successful", response.getMessage());
            assertNotNull(response.getToken());
            assertEquals("session123", response.getSessionId());
            assertNotNull(response.getUser());
            assertEquals(testUser.getName(), response.getUser().getName());
            assertEquals(testUser.getEmail(), response.getUser().getEmail());

            verify(session).setAttribute(eq("token"), anyString());
            verify(session).setAttribute("userId", testUser.getUId());
            verify(session).setAttribute("userRole", testUser.getRole().name());
        }

        @Test
        @DisplayName("Should throw exception when email not found")
        void login_WithInvalidEmail_ShouldThrowException() {
            // Arrange
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.login(loginRequest, session));
            assertEquals("Invalid email or password", exception.getMessage());

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void login_WithIncorrectPassword_ShouldThrowException() {
            // Arrange
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.login(loginRequest, session));
            assertEquals("Invalid email or password", exception.getMessage());

            verify(session, never()).setAttribute(anyString(), any());
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register successfully with valid data")
        void register_WithValidData_ShouldReturnLoginResponse() {
            // Arrange
            Users savedUser = new Users();
            savedUser.setUId(2L);
            savedUser.setName(registerRequest.getName());
            savedUser.setEmail(registerRequest.getEmail());
            savedUser.setPasswordHash("encodedPassword");
            savedUser.setRole(UserRole.CUSTOMER);

            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(Users.class))).thenReturn(savedUser);
            when(session.getId()).thenReturn("session456");

            // Act
            LoginResponse response = authService.register(registerRequest, session);

            // Assert
            assertNotNull(response);
            assertEquals("Registration successful", response.getMessage());
            assertNotNull(response.getToken());
            assertEquals("session456", response.getSessionId());
            assertNotNull(response.getUser());
            assertEquals(savedUser.getName(), response.getUser().getName());
            assertEquals(savedUser.getEmail(), response.getUser().getEmail());
            assertEquals(UserRole.CUSTOMER, response.getUser().getRole());

            verify(userRepository).save(any(Users.class));
            verify(session).setAttribute(eq("token"), anyString());
            verify(session).setAttribute("userId", savedUser.getUId());
            verify(session).setAttribute("userRole", savedUser.getRole().name());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_WithExistingEmail_ShouldThrowException() {
            // Arrange
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.register(registerRequest, session));
            assertEquals("Email already exists", exception.getMessage());

            verify(userRepository, never()).save(any(Users.class));
        }

        @Test
        @DisplayName("Should set default role as CUSTOMER")
        void register_ShouldSetDefaultRoleAsCustomer() {
            // Arrange
            Users savedUser = new Users();
            savedUser.setUId(3L);
            savedUser.setName(registerRequest.getName());
            savedUser.setEmail(registerRequest.getEmail());
            savedUser.setPasswordHash("encodedPassword");
            savedUser.setRole(UserRole.CUSTOMER);

            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
                Users user = invocation.getArgument(0);
                assertEquals(UserRole.CUSTOMER, user.getRole());
                user.setUId(3L);
                return user;
            });
            when(session.getId()).thenReturn("session789");

            // Act
            LoginResponse response = authService.register(registerRequest, session);

            // Assert
            assertNotNull(response);
            assertEquals(UserRole.CUSTOMER, response.getUser().getRole());
        }
    }
}
