package com.example.skillsharing.service;

import com.example.skillsharing.dto.UserDTO;
import com.example.skillsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    
    UserDTO updateUser(Long userId, UserDTO userDTO);
    
    UserDTO getUserById(Long userId);
    
    UserDTO getUserByUsername(String username);
    
    Page<UserDTO> getAllUsers(Pageable pageable);
    
    void deleteUser(Long userId);
    
    UserDTO followUser(Long userId, Long targetUserId);
    
    UserDTO unfollowUser(Long userId, Long targetUserId);
    
    Page<UserDTO> getFollowers(Long userId, Pageable pageable);
    
    Page<UserDTO> getFollowing(Long userId, Pageable pageable);
    
    boolean isFollowing(Long userId, Long targetUserId);
    
    User getCurrentUser();
    
    UserDTO getCurrentUserDTO();
    
    User getUserEntityById(Long userId);
    
    UserDTO convertToDTO(User user);
    
    User convertToEntity(UserDTO userDTO);
}
