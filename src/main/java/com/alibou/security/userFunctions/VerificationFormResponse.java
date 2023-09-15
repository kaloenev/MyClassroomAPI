package com.alibou.security.userFunctions;

import com.alibou.security.user.City;
import com.alibou.security.user.Degree;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationFormResponse {
    private City[] cities;
    private Degree[] degrees;
}
