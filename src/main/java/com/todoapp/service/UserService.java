package com.todoapp.service;

import com.todoapp.dto.UpdateUserRequest;
import com.todoapp.dto.UserDto;
import com.todoapp.exception.BadRequestException;
import com.todoapp.exception.ResourceNotFoundException;
import com.todoapp.exception.UnauthorizedException;
import com.todoapp.model.User;
import com.todoapp.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UUID userId) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserDto.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository
            .findAll()
            .stream()
            .map(UserDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = userRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + id)
            );
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto updateUser(
        UUID targetUserId,
        UpdateUserRequest request,
        UUID requestingUserId
    ) {
        User targetUser = userRepository
            .findById(targetUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User requestingUser = userRepository
            .findById(requestingUserId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Requesting user not found")
            );

        // Self-update: any user can update their own profile (except role)
        boolean isSelfUpdate = targetUserId.equals(requestingUserId);

        // Manager can edit user info but NOT change roles
        // Admin can do everything
        if (!isSelfUpdate) {
            if (requestingUser.getRole() == User.Role.USER) {
                throw new UnauthorizedException(
                    "You can only edit your own profile"
                );
            }
            // Managers cannot edit other managers or admins
            if (
                requestingUser.getRole() == User.Role.MANAGER &&
                (targetUser.getRole() == User.Role.MANAGER ||
                    targetUser.getRole() == User.Role.ADMIN)
            ) {
                throw new UnauthorizedException(
                    "Managers can only edit regular users"
                );
            }
        }

        // Apply updates
        if (StringUtils.hasText(request.getName())) targetUser.setName(
            request.getName()
        );
        if (request.getPhone() != null) targetUser.setPhone(request.getPhone());
        if (request.getAddress() != null) targetUser.setAddress(
            request.getAddress()
        );
        if (request.getDepartment() != null) targetUser.setDepartment(
            request.getDepartment()
        );
        if (request.getBio() != null) targetUser.setBio(request.getBio());
        if (
            request.getProfilePictureUrl() != null
        ) targetUser.setProfilePictureUrl(request.getProfilePictureUrl());

        // Only ADMIN can change roles
        if (StringUtils.hasText(request.getRole())) {
            if (requestingUser.getRole() != User.Role.ADMIN) {
                throw new UnauthorizedException(
                    "Only admins can change user roles"
                );
            }
            try {
                targetUser.setRole(
                    User.Role.valueOf(request.getRole().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                    "Invalid role: " + request.getRole()
                );
            }
        }

        targetUser = userRepository.save(targetUser);
        return UserDto.fromEntity(targetUser);
    }

    @Transactional
    public void deleteUser(UUID targetUserId, UUID requestingUserId) {
        User requestingUser = userRepository
            .findById(requestingUserId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Requesting user not found")
            );

        if (requestingUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only admins can delete users");
        }

        if (targetUserId.equals(requestingUserId)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        if (!userRepository.existsById(targetUserId)) {
            throw new ResourceNotFoundException(
                "User not found with id: " + targetUserId
            );
        }

        userRepository.deleteById(targetUserId);
    }
}
