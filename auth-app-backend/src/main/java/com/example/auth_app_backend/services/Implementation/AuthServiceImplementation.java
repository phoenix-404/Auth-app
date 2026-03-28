package com.example.auth_app_backend.services.Implementation;

import com.example.auth_app_backend.dtos.UserDTO;
import com.example.auth_app_backend.services.AuthService;
import com.example.auth_app_backend.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImplementation implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImplementation(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        //logics:
        //verify email
        //verify password
        //default roles
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword())); //Encoded format in DB
        UserDTO userDTO1 = userService.createUser(userDTO);
        return userDTO1;
    }
}
