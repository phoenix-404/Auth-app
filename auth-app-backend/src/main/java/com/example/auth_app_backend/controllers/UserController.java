package com.example.auth_app_backend.controllers;


import com.example.auth_app_backend.dtos.UserDTO;
import com.example.auth_app_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    //Create User API
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO){
//        return ResponseEntity.ok(userService.createUser(userDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDTO));
    }

    //Get All User api
    @GetMapping
    public  ResponseEntity<Iterable<UserDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable("email") String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    //delete user
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId")UUID userId){
        userService.deleteUser(userId);
    }

    //Update User
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updatedUser(@RequestBody UserDTO userDTO, @PathVariable("userId") UUID userId){
        return ResponseEntity.ok(userService.updateUser(userDTO,userId));
    }

}
