package com.learn.auth.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "User login credentials payload")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

	@Schema(description = "Registered email address", example = "workflowadmin@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
	private String email;

	@Schema(description = "User password", example = "Workflow@1234", requiredMode = Schema.RequiredMode.REQUIRED)
	private String password;
}

