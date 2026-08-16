package com.learn.auth.security;

import com.learn.auth.dtos.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
	private String accessToken;
	private String refreshToken;
	private UserDto userDto;
}
