package com.wallet.service;

import com.wallet.dto.request.LoginRequest;
import com.wallet.dto.request.RegisterRequest;
import com.wallet.dto.response.LoginResponse;
import com.wallet.dto.response.RegisterResponse;
import com.wallet.entity.Role;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.exception.EmailAlreadyExistsException;
import com.wallet.exception.InvalidCredentialsException;
import com.wallet.exception.PhoneAlreadyExistsException;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Naman Sharma");
        registerRequest.setEmail("naman@gmail.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("Password123!");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("naman@gmail.com");
        loginRequest.setPassword("Password123!");
    }

    @Test
    @DisplayName("PRD Test 1: Valid registration creates User and Wallet")
    void register_Success() {
        // Arrange
        when(userRepository.existsByEmail("naman@gmail.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword123");

        // Act
        RegisterResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Registration successful", response.getMessage());

        // Verify User was saved with trimmed & normalized values
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("Naman Sharma", savedUser.getName());
        assertEquals("naman@gmail.com", savedUser.getEmail());
        assertEquals("9876543210", savedUser.getPhone());
        assertEquals("hashedPassword123", savedUser.getPasswordHash());

        // Verify Wallet was saved linked to the User
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        Wallet savedWallet = walletCaptor.getValue();
        assertEquals(savedUser, savedWallet.getUser());
    }

    @Test
    @DisplayName("PRD Test 2: Duplicate email is rejected with 409 Conflict")
    void register_DuplicateEmail_ThrowsEmailAlreadyExistsException() {
        // Arrange
        when(userRepository.existsByEmail("naman@gmail.com")).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("PRD Test 3: Duplicate phone is rejected with 409 Conflict")
    void register_DuplicatePhone_ThrowsPhoneAlreadyExistsException() {
        // Arrange
        when(userRepository.existsByEmail("naman@gmail.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543210")).thenReturn(true);

        // Act & Assert
        assertThrows(PhoneAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("PRD Test 4: Valid login returns JWT token and expiry")
    void login_Success() {
        // Arrange
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("naman@gmail.com");
        user.setPasswordHash("hashedPassword123");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("naman@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashedPassword123")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationInSeconds()).thenReturn(3600L);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals(3600L, response.getExpiresIn());
    }

    @Test
    @DisplayName("PRD Test 5: Invalid password returns 401 Unauthorized")
    void login_InvalidPassword_ThrowsInvalidCredentialsException() {
        // Arrange
        User user = new User();
        user.setEmail("naman@gmail.com");
        user.setPasswordHash("hashedPassword123");

        when(userRepository.findByEmail("naman@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword123")).thenReturn(false);

        loginRequest.setPassword("WrongPassword");

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("PRD Test 5: Non-existent email returns generic 401 Unauthorized")
    void login_NonExistentEmail_ThrowsInvalidCredentialsException() {
        // Arrange
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());
        loginRequest.setEmail("unknown@gmail.com");

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }
}
