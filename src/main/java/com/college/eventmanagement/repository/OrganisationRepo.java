package com.college.eventmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.college.eventmanagement.model.College;
import com.college.eventmanagement.model.Organisation;
import com.college.eventmanagement.model.enums.OrgType;

@Repository
public interface OrganisationRepo extends JpaRepository<Organisation, Long> {

    Optional<Organisation> findByName(String name);

    List<Organisation> findByOrgType(OrgType orgType);

    List<Organisation> findByIsActive(boolean isActive);

    List<Organisation> findByCollege(College college);

    List<Organisation> findByCollegeAndOrgType(College college, OrgType orgType);

    List<Organisation> findByCollegeId(Long collegeId);
}
