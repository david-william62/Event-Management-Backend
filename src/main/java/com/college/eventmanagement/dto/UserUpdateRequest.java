package com.college.eventmanagement.dto;

import com.college.eventmanagement.model.enums.SubRole;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String department;
    private String institution;
    private String rollOrEmpNo;
    private String year;
    private String avatarColor;
    private SubRole subRole;
}
