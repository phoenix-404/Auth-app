package com.example.auth_app_backend.services;

import com.example.auth_app_backend.dtos.UserDTO;

import java.util.UUID;

public interface UserService {
    //Create User
    UserDTO createUser(UserDTO userDTO);

    //Get user by Email
    UserDTO getUserByEmail(String email);

    //Update User
    UserDTO updateUser(UserDTO userDTO, UUID userId);

    //Delete User
    void deleteUser(UUID userId);

    //Get user by Id
    UserDTO getUserById(UUID userId);

    //Get all Users
    Iterable<UserDTO> getAllUsers();

}
