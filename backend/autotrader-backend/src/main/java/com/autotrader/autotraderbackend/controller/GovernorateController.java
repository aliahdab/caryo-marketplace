package com.autotrader.autotraderbackend.controller;

import com.autotrader.autotraderbackend.model.Governorate;
import com.autotrader.autotraderbackend.payload.response.GovernorateResponse;
import com.autotrader.autotraderbackend.service.GovernorateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/governorates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Governorate API", description = "API endpoints for managing governorates")
public class GovernorateController {

    private final GovernorateService governorateService;

    @GetMapping
    @Operation(summary = "Get all active governorates",
               description = "Returns a list of all active governorates")
    public ResponseEntity<List<GovernorateResponse>> getAllGovernorates() {
        log.info("Request to get all active governorates");
        List<Governorate> governorates = governorateService.getAllActiveGovernorates();
        List<GovernorateResponse> response = governorates.stream()
                .map(GovernorateResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/country/{countryCode}")
    @Operation(summary = "Get governorates by country code",
               description = "Returns a list of governorates for a specific country code")
    public ResponseEntity<List<GovernorateResponse>> getGovernoratesByCountryCode(@PathVariable String countryCode) {
        log.info("Request to get governorates for country code: {}", countryCode);
        List<Governorate> governorates = governorateService.getGovernoratesByCountryCode(countryCode);
        List<GovernorateResponse> response = governorates.stream()
                .map(GovernorateResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get governorate by ID",
               description = "Returns a governorate by its ID")
    public ResponseEntity<GovernorateResponse> getGovernorateById(@PathVariable Long id) {
        log.info("Request to get governorate with ID: {}", id);
        return governorateService.getGovernorateById(id)
                .map(GovernorateResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get governorate by slug",
               description = "Returns a governorate by its slug")
    public ResponseEntity<GovernorateResponse> getGovernorateBySlug(@PathVariable String slug) {
        log.info("Request to get governorate with slug: {}", slug);
        return governorateService.getGovernorateBySlug(slug)
                .map(GovernorateResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search governorates by name",
               description = "Returns governorates with names containing the search term")
    public ResponseEntity<List<GovernorateResponse>> searchGovernoratesByName(@RequestParam String name) {
        log.info("Request to search governorates with name containing: {}", name);
        List<Governorate> governorates = governorateService.searchGovernoratesByName(name);
        List<GovernorateResponse> response = governorates.stream()
                .map(GovernorateResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
