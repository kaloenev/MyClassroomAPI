package com.alibou.security.userFunctions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileRequest {
    private int id;
    private String name;
    private String surname;
    private String gender;
    private String imageLocation;
    private boolean clientService;
    private boolean marketingService;
    private boolean reminders;
    private boolean chatNotifications;
    private boolean savedCoursesNotifications;
}
