package com.vn.gotogether.service.user;


import com.vn.gotogether.dto.user.ChangePasswordRequest;
import com.vn.gotogether.dto.user.UserRegisterRequest;
import com.vn.gotogether.dto.user.UserResponse;
import com.vn.gotogether.dto.user.UserUpdateRequest;
import com.vn.gotogether.entity.Role;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.user.RoleRepository;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

   @Autowired
    private RoleRepository roleRepository;
   @Autowired
    private UserRepository userRepository;
   @Autowired
    private  PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public UserResponse registerUser(UserRegisterRequest request) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại, vui lòng chọn tên khác.");
        }

        // Lấy role mặc định là ROLE_USER
        Role userRole = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role USER không tồn tại"));
        if(userRole.getName().equals("ROLE_ADMIN") || userRole.getName().equals("ROLE_SUPPLIER")) {
            return null;
        }
        // Tạo user mới
        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(null)
                .previousLogin(null)
                .build();
        newUser = userRepository.save(newUser);
        return UserResponse.builder()
                .id(newUser.getId())
                .email(newUser.getEmail())
                .role(newUser.getRole().getName())
                .name(newUser.getName())
                .createdAt(newUser.getCreatedAt())
                .lastLogin(newUser.getLastLogin())
                .createdAt(newUser.getCreatedAt())
                .build();
    }

    public UserResponse registerUserSupplier(UserRegisterRequest request) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại, vui lòng chọn tên khác.");
        }

        // Lấy role mặc định là ROLE_USER
        Role userRole = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role USER không tồn tại"));

        if(userRole.getName().equals("ROLE_SUPPLIER")) {

            // Tạo user mới
            User newUser = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(userRole)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .lastLogin(null)
                    .previousLogin(null)
                    .build();
            newUser = userRepository.save(newUser);
            return UserResponse.builder()
                    .id(newUser.getId())
                    .email(newUser.getEmail())
                    .role(newUser.getRole().getName())
                    .name(newUser.getName())
                    .createdAt(newUser.getCreatedAt())
                    .lastLogin(newUser.getLastLogin())
                    .createdAt(newUser.getCreatedAt())
                    .build();
        }
        return null;
    }


    public UserResponse getUserInformation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        if (userOpt.isEmpty()) {
            throw new InvalidDataException("Account not found!");
        }

        User user = userOpt.get();

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .lastLogin(user.getLastLogin())
                .previousLogin(user.getPreviousLogin())
                .createdAt(user.getCreatedAt())
                .passwordUpdatedAt(user.getPasswordUpdatedAt())
                .build();
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public UserResponse updateUserInfo(UserUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        user.setName(request.getName());

        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidDataException("Mật khẩu hiện tại không đúng.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAllUsersWithAdminFirst();
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        return users.stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .role(u.getRole() != null ? u.getRole().getName() : null)
                        .lastLogin(u.getLastLogin())
                        .previousLogin(u.getPreviousLogin())
                        .createdAt(u.getCreatedAt())
                        .online(u.getLastActivity() != null && u.getLastActivity().isAfter(threshold))
                        .active(u.isActive())
                        .build()
                )
                .toList();
    }

    public void updateLastActivity(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public UserResponse adminUpdateUserInfo(UserUpdateRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        user.setEmail(request.getEmail());
        if(request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setName(request.getName());

        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())

                .build();
    }

    public void deleteUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));


        userRepository.deleteById(userId);
    }

    public void updateActive(String id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public List<UserResponse> getUsersByRole(String roleName) {
        List<User> users = userRepository.findByRole_Name(roleName);
        return users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(user.getRole().getName())
                        .active(user.isActive())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
    }
}
