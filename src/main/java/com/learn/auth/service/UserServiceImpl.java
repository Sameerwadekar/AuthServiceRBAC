package com.learn.auth.service;

import com.learn.auth.entities.RefreshToken;
import com.learn.auth.security.LoginRequest;
import com.learn.auth.security.LoginResponse;
import com.learn.auth.security.RefreshTokenRequest;
import com.learn.auth.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.learn.auth.dtos.UserDto;
import com.learn.auth.entities.Role;
import com.learn.auth.entities.User;
import com.learn.auth.repositary.RoleRepositary;
import com.learn.auth.repositary.UserRepositary;
import org.springframework.web.bind.annotation.GetMapping;

@Service
public class UserServiceImpl implements UserService {

	private final PasswordEncoder passwordEncoder;
	private final RoleRepositary roleRepositary;
	private final UserRepositary userRepositary;
	private  final JwtUtils jwtUtils;
	private final AuthenticationManager authenticationManager;
	private final RefreshTokenService refreshTokenService;

	public UserServiceImpl(PasswordEncoder passwordEncoder, RoleRepositary roleRepositary, UserRepositary userRepositary, JwtUtils jwtUtils, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService) {
		this.passwordEncoder = passwordEncoder;
		this.roleRepositary = roleRepositary;
		this.userRepositary = userRepositary;
		this.jwtUtils = jwtUtils;
		this.authenticationManager = authenticationManager;
		this.refreshTokenService = refreshTokenService;
	}

	@Override
	public UserDto createUser(UserDto userDto) {
		User user = dtoToUser(userDto);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		Role role = roleRepositary.findByRoleName("ROLE_USER")
				.orElseThrow(() -> new RuntimeException("Role Not Found"));
		user.setRole(role);
		User savedUser = userRepositary.save(user);
		return userToDto(savedUser);
	}

	@Override
	public LoginResponse login(LoginRequest loginRequest) {
		Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authenticate);
		User user = (User) authenticate.getPrincipal();
		String accessToken = jwtUtils.generateTokenFromUsername(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
		LoginResponse response = new LoginResponse();
		response.setAccessToken(accessToken);
		response.setRefreshToken(refreshToken.getToken());
		response.setUserDto(userToDto(user));
		return response;
	}

	private User dtoToUser(UserDto dto) {
		User user = new User();
		user.setId(dto.getId());
		user.setName(dto.getName());
		user.setEmail(dto.getEmail());
		user.setPassword(dto.getPassword());
		return user;
	}

	private UserDto userToDto(User user) {
		UserDto dto = new UserDto();
		dto.setId(user.getId());
		dto.setName(user.getName());
		dto.setEmail(user.getEmail());
		if (user.getRole() != null) {
			dto.setRoleName(user.getRole().getRoleName());
		}
		dto.setPassword(null);
		dto.setConfirmPassword(null);
		return dto;
	}

	@Override
	public UserDto getCurrentUser(Authentication authentication) {
		String email = authentication.getName();
		User user = userRepositary.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		return userToDto(user);
	}

	@Override
	public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
		RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());
		User user = refreshToken.getUser();
		String newAccessToken = jwtUtils.generateTokenFromUsername(user);
		LoginResponse response = new LoginResponse();
		response.setAccessToken(newAccessToken);
		response.setRefreshToken(refreshToken.getToken());
		response.setUserDto(userToDto(user));
		return response;
	}

	@Override
	public void logOut(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new RuntimeException("User is not authenticated.");
		}
		User user = (User) authentication.getPrincipal();
		refreshTokenService.deleteByUser(user);
	}
}
