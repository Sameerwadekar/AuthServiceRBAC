package com.learn.auth.security;

import com.learn.auth.entities.User;
import com.learn.auth.repositary.UserRepositary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepositary userRepositary;

	public UserDetailsServiceImpl(UserRepositary userRepositary) {
		this.userRepositary = userRepositary;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepositary.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
	}
}
