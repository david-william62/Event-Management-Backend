package com.college.eventmanagement.controller;

import com.college.eventmanagement.model.Organisation;
import com.college.eventmanagement.model.enums.OrgType;
import com.college.eventmanagement.repository.OrganisationRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
public class OrgController {

    private final OrganisationRepo orgRepo;

    @GetMapping
    public ResponseEntity<List<OrgResponse>> getAll(@RequestParam(required = false) Long collegeId) {
        List<Organisation> orgs;
        if (collegeId != null) {
            orgs = orgRepo.findByCollegeId(collegeId);
        } else {
            orgs = orgRepo.findAll();
        }
        return ResponseEntity.ok(orgs.stream().map(OrgResponse::from).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrgResponse> getById(@PathVariable Long id) {
        return orgRepo.findById(id)
                .map(OrgResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<OrgResponse> create(@Valid @RequestBody OrgRequest request) {
        Organisation org = Organisation.builder()
                .name(request.getName())
                .orgType(request.getOrgType())
                .description(request.getDescription())
                .isActive(true)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(OrgResponse.from(orgRepo.save(org)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<OrgResponse> update(@PathVariable Long id, @Valid @RequestBody OrgRequest request) {
        return orgRepo.findById(id).map(org -> {
            org.setName(request.getName());
            org.setOrgType(request.getOrgType());
            org.setDescription(request.getDescription());
            return ResponseEntity.ok(OrgResponse.from(orgRepo.save(org)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!orgRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        orgRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Nested DTOs ──────────────────────────────────────────────────────────
    @Data
    public static class OrgRequest {

        @NotBlank
        private String name;
        @NotNull
        private OrgType orgType;
        private String description;
    }

    @Data
    public static class OrgResponse {

        private Long id;
        private String name;
        private OrgType orgType;
        private String description;
        private boolean isActive;
        private int memberCount;

        public static OrgResponse from(Organisation org) {
            OrgResponse r = new OrgResponse();
            r.id = org.getId();
            r.name = org.getName();
            r.orgType = org.getOrgType();
            r.description = org.getDescription();
            r.isActive = org.isActive();
            r.memberCount = org.getMemberships().size();
            return r;
        }
    }
}
