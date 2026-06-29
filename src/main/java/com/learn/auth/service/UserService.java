package com.learn.auth.service;

import com.learn.auth.dtos.UserDto;

public interface UserService {
	UserDto createUser(UserDto userDto);
}
