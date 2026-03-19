package com.example.auth_app_backend.controllers;


import com.example.auth_app_backend.dtos.UserDTO;
import com.example.auth_app_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


}
