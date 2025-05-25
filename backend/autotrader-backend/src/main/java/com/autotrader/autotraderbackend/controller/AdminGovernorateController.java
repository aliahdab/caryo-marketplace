package com.autotrader.autotraderbackend.controller;

import com.autotrader.autotraderbackend.model.Governorate;
import com.autotrader.autotraderbackend.payload.request.GovernorateDTO;
import com.autotrader.autotraderbackend.payload.response.ApiResponse;
import com.autotrader.autotraderbackend.payload.response.GovernorateResponse;
import com.autotrader.autotraderbackend.service.GovernorateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/governorates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Governorate API", description = "API endpoints for admin management of governorates")
public class AdminGovernorateController {

    private final GovernorateService governorateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new governorate",
               description = "Creates a new governorate with the provided details (Admin only)")
    public ResponseEntity<GovernorateResponse> createGovernorate(@Valid @RequestBody GovernorateDTO governorateDTO) {
        log.info("Request to create governorate with slug: {}", governorateDTO.getSlug());
        
        Governorate governorate = new Governorate();
        governorate.setDisplayNameEn(governorateDTO.getDisplayNameEn());
        governorate.setDisplayNameAr(governorateDTO.getDisplayNameAr());
        governorate.setSlug(governorateDTO.getSlug());
        governorate.setCountryCode(governorateDTO.getCountryCode());
        governorate.setRegion(governorateDTO.getRegion());
        governorate.setLatitude(governorateDTO.getLatitude());
        governorate.setLongitude(governorateDTO.getLongitude());
        governorate.setIsActive(governorateDTO.getIsActive());
        
        Governorate savedGovernorate = governorateService.createGovernorate(governorate);
        return ResponseEntity.ok(GovernorateResponse.fromEntity(savedGovernorate));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a governorate",
               description = "Updates a governorate with the provided details (Admin only)")
    public ResponseEntity<GovernorateResponse> updateGovernorate(
            @PathVariable Long id, 
            @Valid @RequestBody GovernorateDTO governorateDTO) {
        log.info("Request to update governorate with ID: {}", id);
        
        Governorate governorate = new Governorate();
        governorate.setDisplayNameEn(governorateDTO.getDisplayNameEn());
        governorate.setDisplayNameAr(governorateDTO.getDisplayNameAr());
        governorate.setSlug(governorateDTO.getSlug());
        governorate.setCountryCode(governorateDTO.getCountryCode());
        governorate.setRegion(governorateDTO.getRegion());
        governorate.setLatitude(governorateDTO.getLatitude());
        governorate.setLongitude(governorateDTO.getLongitude());
        governorate.setIsActive(governorateDTO.getIsActive());
        
        return governorateService.updateGovernorate(id, governorate)
                .map(GovernorateResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a governorate",
               description = "Deletes a governorate by ID (Admin only)")
    public ResponseEntity<ApiResponse> deleteGovernorate(@PathVariable Long id) {
        log.info("Request to delete governorate with ID: {}", id);
        
        boolean deleted = governorateService.deleteGovernorate(id);
        if (deleted) {
            return ResponseEntity.ok(new ApiResponse(true, "Governorate deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a governorate",
               description = "Activates a governorate by ID (Admin only)")
    public ResponseEntity<GovernorateResponse> activateGovernorate(@PathVariable Long id) {
        log.info("Request to activate governorate with ID: {}", id);
        
        return governorateService.activateGovernorate(id)
                .map(GovernorateResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a governorate",
               description = "Deactivates a governorate by ID (Admin only)")
    public ResponseEntity<GovernorateResponse> deactivateGovernorate(@PathVariable Long id) {
        log.info("Request to deactivate governorate with ID: {}", id);
        
        return governorateService.deactivateGovernorate(id)
                .map(GovernorateResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
