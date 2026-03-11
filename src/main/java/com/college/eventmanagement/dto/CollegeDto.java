package com.college.eventmanagement.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollegeDto {

    private Long id;
    private String name;
    private String address;
    private String city;
    private boolean isActive;
    private int departmentCount;
    private int organisationCount;
    private int userCount;
}
