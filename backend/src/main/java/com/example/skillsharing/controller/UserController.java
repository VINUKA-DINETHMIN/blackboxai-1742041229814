package com.example.skillsharing.controller;

import com.example.skillsharing.dto.UserDTO;
import com.example.skillsharing.security.CurrentUser;
import com.example.skillsharing.security.UserPrincipal;
import com.example.skillsharing.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public UserDTO getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userService.getCurrentUserDTO();
    }

    @GetMapping("/{username}")
    public UserDTO getUserProfile(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public UserDTO updateUser(@CurrentUser UserPrincipal userPrincipal,
                            @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(userPrincipal.getId(), userDTO);
    }

    @PostMapping("/me/profile-picture")
    @PreAuthorize("hasRole('USER')")
    public UserDTO updateProfilePicture(@CurrentUser UserPrincipal userPrincipal,
                                      @RequestParam("file") MultipartFile file) {
        UserDTO userDTO = userService.getCurrentUserDTO();
        userDTO.setProfilePicture(file.getOriginalFilename()); // In real app, save file and get URL
        return userService.updateUser(userPrincipal.getId(), userDTO);
    }

    @PostMapping("/{userId}/follow")
    @PreAuthorize("hasRole('USER')")
    public UserDTO followUser(@CurrentUser UserPrincipal userPrincipal,
                            @PathVariable Long userId) {
        return userService.followUser(userPrincipal.getId(), userId);
    }

    @PostMapping("/{userId}/unfollow")
    @PreAuthorize("hasRole('USER')")
    public UserDTO unfollowUser(@CurrentUser UserPrincipal userPrincipal,
                              @PathVariable Long userId) {
        return userService.unfollowUser(userPrincipal.getId(), userId);
    }

    @GetMapping("/{userId}/followers")
    public Page<UserDTO> getUserFollowers(@PathVariable Long userId, Pageable pageable) {
        return userService.getFollowers(userId, pageable);
    }

    @GetMapping("/{userId}/following")
    public Page<UserDTO> getUserFollowing(@PathVariable Long userId, Pageable pageable) {
        return userService.getFollowing(userId, pageable);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteUser(@CurrentUser UserPrincipal userPrincipal) {
        userService.deleteUser(userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/is-following")
    @PreAuthorize("hasRole('USER')")
    public boolean isFollowing(@CurrentUser UserPrincipal userPrincipal,
                             @PathVariable Long userId) {
        return userService.isFollowing(userPrincipal.getId(), userId);
    }
}
