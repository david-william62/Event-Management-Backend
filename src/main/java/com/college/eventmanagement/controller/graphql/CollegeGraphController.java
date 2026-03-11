package com.college.eventmanagement.controller.graphql;

import com.college.eventmanagement.model.College;
import com.college.eventmanagement.model.Organisation;
import com.college.eventmanagement.model.enums.OrgType;
import com.college.eventmanagement.repository.CollegeRepo;
import com.college.eventmanagement.repository.OrganisationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CollegeGraphController {

    private final CollegeRepo collegeRepo;
    private final OrganisationRepo orgRepo;

    @QueryMapping
    public List<College> allColleges() {
        return collegeRepo.findByIsActive(true);
    }

    @QueryMapping
    public College collegeById(@Argument Long id) {
        return collegeRepo.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Organisation> departmentsByCollege(@Argument Long collegeId) {
        College college = collegeRepo.findById(collegeId).orElse(null);
        if (college == null) {
            return List.of();
        }
        return orgRepo.findByCollegeAndOrgType(college, OrgType.DEPARTMENT);
    }
}
