package com.learn.auth.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.learn.auth.dtos.UserDto;
import com.learn.auth.entities.AppRole;
import com.learn.auth.entities.Role;
import com.learn.auth.entities.User;
import com.learn.auth.repositary.RoleRepositary;
import com.learn.auth.repositary.UserRepositary;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private UserRepositary userRepositary;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired 
	private RoleRepositary roleRepositary;

	@Override
	public UserDto createUser(UserDto userDto) {
		User user = modelMapper.map(userDto, User.class);
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));
		Role role = roleRepositary.findByRoleName(AppRole.ROLE_USER).orElseThrow(()->new RuntimeException("Role Not Found"));
		user.setRole(role);
		User savedUser = userRepositary.save(user);
		UserDto userdto = modelMapper.map(savedUser, UserDto.class);
		return userdto;
	}

}
