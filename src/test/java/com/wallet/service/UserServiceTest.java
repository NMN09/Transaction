package com.wallet.service;

import com.wallet.dto.request.UpdateProfileRequest;
import com.wallet.dto.response.UserProfileResponse;
import com.wallet.entity.Role;
import com.wallet.entity.User;
import com.wallet.exception.PhoneAlreadyExistsException;
import com.wallet.exception.ResourceNotFoundException;
import com.wallet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setName("Naman");
        user.setEmail("naman@gmail.com");
        user.setPhone("9876543210");
        user.setRole(Role.USER);
    }

    @Test
    @DisplayName("Get user profile returns safe DTO without passwordHash")
    void getUserProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getUserProfile(1L);

        assertNotNull(response);
        assertEquals("Naman", response.getName());
        assertEquals("naman@gmail.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());
    }

    @Test
    @DisplayName("Get user profile throws ResourceNotFoundException if user missing")
    void getUserProfile_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserProfile(99L));
    }

    @Test
    @DisplayName("Update user profile successfully changes name and phone")
    void updateUserProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Naman Updated");
        request.setPhone("9876543211");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneAndIdNot("9876543211", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.updateUserProfile(1L, request);

        assertNotNull(response);
        assertEquals("Naman Updated", response.getName());
        assertEquals("9876543211", response.getPhone());
    }

    @Test
    @DisplayName("Update user profile throws PhoneAlreadyExistsException if phone taken by another user")
    void updateUserProfile_DuplicatePhone_ThrowsException() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Naman Updated");
        request.setPhone("9999999999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneAndIdNot("9999999999", 1L)).thenReturn(true);

        assertThrows(PhoneAlreadyExistsException.class, () -> userService.updateUserProfile(1L, request));
        verify(userRepository, never()).save(any());
    }
}
