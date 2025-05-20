package com.autotrader.autotraderbackend.controller;

import com.autotrader.autotraderbackend.payload.response.ApiResponse;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.service.CarListingStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/listings/{id}/status")
@RequiredArgsConstructor
@Tag(name = "Listing Status", description = "Operations for managing car listing status")
public class ListingStatusController {

    private final CarListingStatusService carListingStatusService;

    @Operation(summary = "Mark a listing as sold")
    @PostMapping("/sold")
    public ResponseEntity<ApiResponse<CarListingResponse>> markListingAsSold(
            @PathVariable("id") Long id,
            Principal principal) {
        try {
            CarListingResponse response = carListingStatusService.markListingAsSold(id, principal.getName());
            return ResponseEntity.ok(ApiResponse.success(response, "Listing marked as sold successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        }
    }

    @Operation(summary = "Mark a listing as sold (Admin)")
    @PostMapping("/sold/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarListingResponse>> markListingAsSoldByAdmin(@PathVariable("id") Long id) {
        try {
            CarListingResponse response = carListingStatusService.markListingAsSoldByAdmin(id);
            return ResponseEntity.ok(ApiResponse.success(response, "Listing marked as sold by admin successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        }
    }

    @Operation(summary = "Approve a listing")
    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarListingResponse>> approveListing(@PathVariable("id") Long id) {
        try {
            CarListingResponse response = carListingStatusService.approveListing(id);
            return ResponseEntity.ok(ApiResponse.success(response, "Listing approved successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        }
    }

    @Operation(summary = "Pause a listing")
    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<CarListingResponse>> pauseListing(
            @PathVariable("id") Long id,
            Principal principal) {
        try {
            CarListingResponse response = carListingStatusService.pauseListing(id, principal.getName());
            return ResponseEntity.ok(ApiResponse.success(response, "Listing paused successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        }
    }

    @Operation(summary = "Resume a listing")
    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<CarListingResponse>> resumeListing(
            @PathVariable("id") Long id,
            Principal principal) {
        try {
            CarListingResponse response = carListingStatusService.resumeListing(id, principal.getName());
            return ResponseEntity.ok(ApiResponse.success(response, "Listing resumed successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        }
    }

    @Operation(summary = "Archive a listing")
    @PostMapping("/archive")
    public ResponseEntity<ApiResponse<CarListingResponse>> archiveListing(
            @PathVariable("id") Long id,
            Principal principal) {
        try {
            CarListingResponse response = carListingStatusService.archiveListing(id, principal.getName());
            return ResponseEntity.ok(ApiResponse.success(response, "Listing archived successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        }
    }

    @Operation(summary = "Archive a listing (Admin)")
    @PostMapping("/archive/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarListingResponse>> archiveListingByAdmin(@PathVariable("id") Long id) {
        try {
            CarListingResponse response = carListingStatusService.archiveListingByAdmin(id);
            return ResponseEntity.ok(ApiResponse.success(response, "Listing archived by admin successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        }
    }

    @Operation(summary = "Unarchive a listing")
    @PostMapping("/unarchive")
    public ResponseEntity<ApiResponse<CarListingResponse>> unarchiveListing(
            @PathVariable("id") Long id,
            Principal principal) {
        try {
            CarListingResponse response = carListingStatusService.unarchiveListing(id, principal.getName());
            return ResponseEntity.ok(ApiResponse.success(response, "Listing unarchived successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        }
    }

    @Operation(summary = "Unarchive a listing (Admin)")
    @PostMapping("/unarchive/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarListingResponse>> unarchiveListingByAdmin(@PathVariable("id") Long id) {
        try {
            CarListingResponse response = carListingStatusService.unarchiveListingByAdmin(id);
            return ResponseEntity.ok(ApiResponse.success(response, "Listing unarchived by admin successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        }
    }
}
