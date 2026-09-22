package com.learn.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "User transfer object representing user details and registration request")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @Schema(description = "Unique user ID (UUID)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(description = "Full name of the user", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "name should not be null")
    @NotBlank(message = "name should not be blank")
    @Size(min = 3)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain only letters and spaces, no numbers allowed")
    private String name;

    @Schema(description = "User password", example = "SecurePassword@123")
    private String password;

    @Schema(description = "Password confirmation matching the password", example = "SecurePassword@123")
    private String confirmPassword;

    @Schema(description = "User email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "email should not be null")
    @NotBlank(message = "email shoukd not be blank")
    private String email;

    @Schema(description = "Assigned role name (e.g. ROLE_USER, ROLE_ADMIN)", example = "ROLE_USER")
    private String roleName;
}

