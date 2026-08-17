package com.wallet.service;

import com.wallet.dto.request.UpdateProfileRequest;
import com.wallet.dto.response.UserProfileResponse;
import com.wallet.entity.User;
import com.wallet.exception.PhoneAlreadyExistsException;
import com.wallet.exception.ResourceNotFoundException;
import com.wallet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String newName = request.getName().trim();
        String newPhone = request.getPhone().trim();

        if (userRepository.existsByPhoneAndIdNot(newPhone, userId)) {
            throw new PhoneAlreadyExistsException("Phone number already exists");
        }

        user.setName(newName);
        user.setPhone(newPhone);

        User updatedUser = userRepository.save(user);

        return new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getRole(),
                updatedUser.getCreatedAt()
        );
    }
}
