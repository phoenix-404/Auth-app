package com.example.auth_app_backend.services;

import com.example.auth_app_backend.dtos.UserDTO;

public interface UserService {
    //Create User
    UserDTO createUser(UserDTO userDTO);

    //Get user by Email
    UserDTO getUserByEmail(String email);

    //Update User
    UserDTO updateUser(UserDTO userDTO, String userId);

    //Delete User
    void deleteUser(String userId);

    //Get user by Id
    UserDTO getUserById(String userId);

    //Get all Users
    Iterable<UserDTO> getAllUsers();

}
