package com.college.eventmanagement.controller.graphql;

import com.college.eventmanagement.model.Organisation;
import com.college.eventmanagement.repository.OrganisationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrganisationGraphController {

    private final OrganisationRepo orgRepo;

    @QueryMapping
    public List<Organisation> allOrganisations() {
        return orgRepo.findAll();
    }

    @QueryMapping
    public Organisation organisationById(@Argument Long id) {
        return orgRepo.findById(id).orElse(null);
    }
}
