package com.college.eventmanagement.dto;

import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.model.enums.SubRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private SubRole subRole;
    private boolean isActive;
    private boolean isVerified;
    private Long collegeId;
    private String collegeName;
    private String department;
    private String institution;
    private String rollOrEmpNo;
    private String year;
    private String avatarColor;
    private int eventsOrganised;
    private int eventsAttended;
    private List<OrgMembershipDto> organisations;
}
