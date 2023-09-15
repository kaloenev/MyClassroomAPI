package com.alibou.security.auth;

import com.alibou.security.user.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  @NotNull
  @Size(min = 3, max = 64)
  private String username;
  @NotNull
  @Size(min = 3, max = 254)
  private String email;
  @NotNull
  @Size(min = 6, max = 254)
  private String password;
  @NotNull
  private Role role;
}
