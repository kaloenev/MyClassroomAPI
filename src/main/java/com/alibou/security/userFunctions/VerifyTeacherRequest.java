package com.alibou.security.userFunctions;

import com.alibou.security.user.City;
import com.alibou.security.user.Degree;
import com.alibou.security.user.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTeacherRequest {
    @NotNull
    @Size(min = 1)
    private String name;
    @NotNull
    @Size(min = 1)
    private String surname;
    @NotNull
    private Gender gender;
    @NotNull
    private City city;
    @NotNull
    @Size(min = 1)
    private String description;
    @NotNull
    @Size(min = 1)
    private String specialties;
    @NotNull
    private Degree degree;
    @NotNull
    @Size(min = 1)
    private String school;
    @NotNull
    @Size(min = 1)
    private String university;
    private String experience;
}
