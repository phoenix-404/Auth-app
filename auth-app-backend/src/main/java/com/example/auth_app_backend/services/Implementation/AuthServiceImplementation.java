package com.example.auth_app_backend.services.Implementation;

import com.example.auth_app_backend.dtos.UserDTO;
import com.example.auth_app_backend.services.AuthService;
import com.example.auth_app_backend.services.UserService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImplementation implements AuthService {
    private final UserService userService;

    public AuthServiceImplementation(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        //login
        //verify email
        //verify password
        //default roles
        UserDTO userDTO1 = userService.createUser(userDTO);
        return userDTO1;
    }
}
