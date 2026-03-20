package com.example.auth_app_backend.services.Implementation;

import com.example.auth_app_backend.dtos.UserDTO;
import com.example.auth_app_backend.entities.Provider;
import com.example.auth_app_backend.entities.User;
import com.example.auth_app_backend.exceptions.ResourceNotFoundException;
import com.example.auth_app_backend.repositories.UserRepository;
import com.example.auth_app_backend.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserServiceImplementation implements UserService {
    private final UserRepository userRepository;
    private  final ModelMapper modelMapper;

    public UserServiceImplementation(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if(userDTO.getEmail()==null || userDTO.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is required");
        }

        if(userRepository.existsByEmail(userDTO.getEmail())){
            throw new IllegalArgumentException("User with given email already exists");
        }

        User user = modelMapper.map(userDTO, User.class);
        user.setProvider(userDTO.getProvider()!=null?userDTO.getProvider(): Provider.LOCAL);
        //role assign here to user___for authorization
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser,UserDTO.class);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found by this email"));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO, UUID userId) {
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found by this userId"));
        //We can't update email id, it's unique, rest we can update
        if(userDTO.getName() != null) existingUser.setName(userDTO.getName());
        // TODO: Change the password updation logic
        if(userDTO.getPassword() != null) existingUser.setPassword(userDTO.getPassword());
        if(userDTO.getImage() != null) existingUser.setImage(userDTO.getImage());
        if(userDTO.getProvider() != null) existingUser.setProvider(userDTO.getProvider());
        existingUser.setEnable(userDTO.isEnable());
        existingUser.setUpdatedAt(Instant.now());

        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found by this userId"));
        userRepository.delete(user);
    }

    @Override
    public UserDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found by this userId"));
        return modelMapper.map(user,UserDTO.class);
    }

    @Override
    @Transactional
    public Iterable<UserDTO> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user,UserDTO.class)).toList();
    }
}
