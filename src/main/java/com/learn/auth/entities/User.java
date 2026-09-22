package com.learn.auth.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseModel implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;
	private String name;

	@Column(unique = true)
	private String email;

	@JsonIgnore
	private String password;

	@ManyToOne
	@JsonBackReference
	private Role role;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<UserPermission> userPermissions = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tenant_id",nullable = true)
	private Tenant tenant;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		if (role != null) {
			authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
		}

		Set<String> effectivePermissions = new HashSet<>();
		if (role != null && role.getPermissions() != null) {
			for (Permission permission : role.getPermissions()) {
				effectivePermissions.add(permission.getName());
			}
		}

		if (userPermissions != null) {
			for (UserPermission up : userPermissions) {
				if (up.getPermission() != null && up.getEffect() != null) {
					if (up.getEffect() == PermissionEffect.ALLOW) {
						effectivePermissions.add(up.getPermission().getName());
					} else if (up.getEffect() == PermissionEffect.DENY) {
						effectivePermissions.remove(up.getPermission().getName());
					}
				}
			}
		}

		for (String perm : effectivePermissions) {
			authorities.add(new SimpleGrantedAuthority(perm));
		}

		return authorities;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
