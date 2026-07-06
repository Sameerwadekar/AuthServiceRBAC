package com.learn.auth.controller;

import com.learn.auth.entities.User;
import com.learn.auth.security.LoginRequest;
import com.learn.auth.security.LoginResponse;
import com.learn.auth.security.RefreshTokenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.learn.auth.dtos.UserDto;
import com.learn.auth.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {
	@Autowired
	private UserService userService;
	
	@PostMapping("/register")
	public ResponseEntity<UserDto> register(@RequestBody @Valid UserDto userDto){
		UserDto user = userService.createUser(userDto);
		return new ResponseEntity<UserDto>(user,HttpStatus.CREATED);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
		LoginResponse loginResponse = userService.login(loginRequest);
		return  ResponseEntity.ok(loginResponse);
	}

	@GetMapping("/me")
	public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
		UserDto userDto = userService.getCurrentUser(authentication);
		return ResponseEntity.ok(userDto);
	}

	@PostMapping("/refresh")
	public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
		LoginResponse loginResponse = userService.refreshToken(request);
		return ResponseEntity.ok(loginResponse);
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(Authentication authentication) {
		try{
			userService.logOut(authentication);
			return ResponseEntity.ok("Logged out successfully");
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}
}
