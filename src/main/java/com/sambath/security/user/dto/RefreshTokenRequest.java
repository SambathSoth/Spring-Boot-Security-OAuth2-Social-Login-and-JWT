package com.sambath.security.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    @NotNull(message = "Refresh token cannot be null")
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}
