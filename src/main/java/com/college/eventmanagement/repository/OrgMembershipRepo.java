package com.college.eventmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.college.eventmanagement.model.OrgMembership;
import com.college.eventmanagement.model.Organisation;
import com.college.eventmanagement.model.User;

@Repository
public interface OrgMembershipRepo extends JpaRepository<OrgMembership, Long> {

    List<OrgMembership> findByUser(User user);

    List<OrgMembership> findByOrganisation(Organisation organisation);

    Optional<OrgMembership> findByUserAndOrganisation(User user, Organisation organisation);

    boolean existsByUserAndOrganisation(User user, Organisation organisation);
}
