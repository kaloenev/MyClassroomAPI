package com.alibou.security.userFunctions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private int id;
    private String name;
    private String surname;
    private String role;
    private boolean isVerified;
    private boolean isBeingVerified;
}
