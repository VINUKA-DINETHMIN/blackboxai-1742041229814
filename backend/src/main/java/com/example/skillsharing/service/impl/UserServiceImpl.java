package com.example.skillsharing.service.impl;

import com.example.skillsharing.dto.UserDTO;
import com.example.skillsharing.exception.BadRequestException;
import com.example.skillsharing.exception.ResourceNotFoundException;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.UserRepository;
import com.example.skillsharing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        User user = convertToEntity(userDTO);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = getUserEntityById(userId);
        
        // Check if email is being changed and if it's already in use
        if (!user.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }
        
        // Check if username is being changed and if it's already taken
        if (!user.getUsername().equals(userDTO.getUsername()) && 
            userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setBio(userDTO.getBio());
        user.setProfilePicture(userDTO.getProfilePicture());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = getUserEntityById(userId);
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserDTO followUser(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BadRequestException("Users cannot follow themselves");
        }

        User user = getUserEntityById(userId);
        User targetUser = getUserEntityById(targetUserId);

        if (user.getFollowing().contains(targetUser)) {
            throw new BadRequestException("Already following this user");
        }

        user.getFollowing().add(targetUser);
        userRepository.save(user);
        
        return convertToDTO(targetUser);
    }

    @Override
    public UserDTO unfollowUser(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BadRequestException("Users cannot unfollow themselves");
        }

        User user = getUserEntityById(userId);
        User targetUser = getUserEntityById(targetUserId);

        if (!user.getFollowing().contains(targetUser)) {
            throw new BadRequestException("Not following this user");
        }

        user.getFollowing().remove(targetUser);
        userRepository.save(user);
        
        return convertToDTO(targetUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getFollowers(Long userId, Pageable pageable) {
        User user = getUserEntityById(userId);
        return userRepository.findAll(pageable)
            .map(u -> {
                UserDTO dto = convertToDTO(u);
                dto.setFollowing(u.getFollowing().contains(user));
                return dto;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getFollowing(Long userId, Pageable pageable) {
        User user = getUserEntityById(userId);
        return userRepository.findAll(pageable)
            .map(u -> {
                UserDTO dto = convertToDTO(u);
                dto.setFollowing(user.getFollowing().contains(u));
                return dto;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId, Long targetUserId) {
        User user = getUserEntityById(userId);
        User targetUser = getUserEntityById(targetUserId);
        return user.getFollowing().contains(targetUser);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUserDTO() {
        return convertToDTO(getCurrentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Override
    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setFollowersCount(user.getFollowers().size());
        dto.setFollowingCount(user.getFollowing().size());
        
        // Set isFollowing if there's a current user
        try {
            User currentUser = getCurrentUser();
            dto.setFollowing(currentUser.getFollowing().contains(user));
        } catch (Exception e) {
            dto.setFollowing(false);
        }
        
        return dto;
    }

    @Override
    public User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setBio(userDTO.getBio());
        user.setProfilePicture(userDTO.getProfilePicture());
        return user;
    }
}
