package com.college.eventmanagement.dto;

import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.model.enums.SubRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private SubRole subRole;
    private Long collegeId;
    private String collegeName;
    private String department;
    private String institution;
    private String rollOrEmpNo;
    private String year;
    private String phone;
    private String avatarColor;
    private boolean isVerified;
}
