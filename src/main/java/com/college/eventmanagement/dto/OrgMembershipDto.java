package com.college.eventmanagement.dto;

import com.college.eventmanagement.model.enums.OrgType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrgMembershipDto {
    private Long id;
    private Long orgId;
    private String orgName;
    private OrgType orgType;
    private String position;
    private String joinedYear;
    private boolean isActive;
    private int memberCount;
}
