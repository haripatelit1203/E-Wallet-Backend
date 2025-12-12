package com.example.controller;

import com.example.dto.UserDto;
import com.example.dto.UserProfileDto;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user-service")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user")
    public Long createUser(@RequestBody @Valid  UserDto userDto) throws ExecutionException, InterruptedException {
        return userService.createUser(userDto);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable Long id){
        UserProfileDto userProfileDto = userService.userProfileDto(id);
        return ResponseEntity.ok(userProfileDto);
    }
}
