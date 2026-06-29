package com.learn.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.learn.auth.entities.User;
import com.learn.auth.repositary.UserRepositary;

@Component
public class UserDetailsServiceImpl implements UserDetailsService  {
	
	@Autowired
	private UserRepositary userRepositary;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepositary.findByEmail(username).orElseThrow(()-> new RuntimeException("email not found"));
		return user;
	}
}
