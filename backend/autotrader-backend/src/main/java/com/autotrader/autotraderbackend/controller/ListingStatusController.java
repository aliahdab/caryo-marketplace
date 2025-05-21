package com.autotrader.autotraderbackend.controller;

import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
<<<<<<< HEAD
import com.autotrader.autotraderbackend.payload.response.ApiResponse;
=======
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.service.CarListingStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
<<<<<<< HEAD
=======
import io.swagger.v3.oas.annotations.media.ExampleObject;
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * REST controller for managing car listing statuses.
 * Provides endpoints for all status-related operations including:
 * - Marking listings as sold
 * - Approving listings
 * - Pausing/resuming listings
 * - Archiving/unarchiving listings
 *
 * All endpoints return standardized responses using ApiResponse wrapper with the following:
 * - Success responses: HTTP 200 with ApiResponse<CarListingResponse>
 * - Error responses: Appropriate HTTP status with ApiResponse<ErrorResponse>
 *
 * Common error responses across all endpoints:
 * - 400 Bad Request: Invalid listing ID or validation errors
 * - 401 Unauthorized: Missing authentication token 
 * - 403 Forbidden: Insufficient permissions
 * - 404 Not Found: Listing does not exist
 * - 409 Conflict: Invalid state transition
 * - 500 Internal Server Error: Unexpected server error
 *
 * Authentication is required for all endpoints. Most endpoints enforce ownership verification
 * except admin-specific operations which bypass ownership checks.
 *
 * @author AutoTrader Team
 * @version 2.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/listings/{id}/status")
@RequiredArgsConstructor
@Tag(name = "Listing Status", description = "Operations for managing car listing status")
@SecurityRequirement(name = "bearer-token")
public class ListingStatusController {

    private final CarListingStatusService carListingStatusService;

    @Operation(
        summary = "Mark a listing as sold",
        description = "Marks the specified car listing as sold. Only the owner of the listing can perform this action. " +
                     "The listing must not be archived. Once marked as sold, the listing will no longer appear in public searches.",
        security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Listing successfully marked as sold",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CarListingResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid listing ID supplied",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "User is not authorized to mark this listing as sold",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Listing not found",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Listing cannot be marked as sold (e.g., already sold or archived)",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/sold")
    public ResponseEntity<ApiResponse<CarListingResponse>> markListingAsSold(
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
        )
    })
    @PostMapping("/sold")
    public ResponseEntity<com.autotrader.autotraderbackend.payload.response.ApiResponse<CarListingResponse>> markListingAsSold(
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            @Parameter(description = "ID of the listing to mark as sold", required = true)
            @PathVariable("id") @NotNull Long id,
            Principal principal) {
        log.debug("Request to mark listing {} as sold by user {}", id, principal.getName());
        
        try {
            CarListingResponse response = carListingStatusService.markListingAsSold(id, principal.getName());
            log.info("Successfully marked listing {} as sold by user {}", id, principal.getName());
<<<<<<< HEAD
            return ResponseEntity.ok(ApiResponse.success(response, "Listing marked as sold successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to mark as sold", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot mark listing {} as sold: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to mark listing {} as sold", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to mark this listing as sold"));
        } catch (Exception e) {
            log.error("Unexpected error when marking listing {} as sold", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
=======
            return ResponseEntity.ok(com.autotrader.autotraderbackend.payload.response.ApiResponse.success(response, "Listing marked as sold successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to mark as sold", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot mark listing {} as sold: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to mark listing {} as sold", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to mark this listing as sold"));
        } catch (Exception e) {
            log.error("Unexpected error when marking listing {} as sold", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        }
    }

    @Operation(
        summary = "Mark a listing as sold (Admin)",
        description = "Marks the specified car listing as sold. Admin-only operation that bypasses ownership checks. " +
                     "The listing must not be archived. Once marked as sold, the listing will no longer appear in public searches.",
        security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Listing successfully marked as sold by admin",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CarListingResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid listing ID supplied",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Admin privileges required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Listing not found",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Listing cannot be marked as sold (e.g., already sold or archived)",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        )
    })
    @PostMapping("/sold/admin")
    @PreAuthorize("hasRole('ADMIN')")
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<CarListingResponse>> markListingAsSoldByAdmin(
=======
    public ResponseEntity<com.autotrader.autotraderbackend.payload.response.ApiResponse<CarListingResponse>> markListingAsSoldByAdmin(
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            @Parameter(description = "ID of the listing to mark as sold", required = true)
            @PathVariable("id") @NotNull Long id) {
        log.debug("Admin request to mark listing {} as sold", id);
        
        try {
            CarListingResponse response = carListingStatusService.markListingAsSoldByAdmin(id);
            log.info("Admin successfully marked listing {} as sold", id);
<<<<<<< HEAD
            return ResponseEntity.ok(ApiResponse.success(response, "Listing marked as sold by admin successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when admin attempting to mark as sold", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Admin cannot mark listing {} as sold: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error when admin marking listing {} as sold", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
=======
            return ResponseEntity.ok(com.autotrader.autotraderbackend.payload.response.ApiResponse.success(response, "Listing marked as sold by admin successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when admin attempting to mark as sold", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.NOT_FOUND, "Listing not found. Please check the ID and try again."));
        } catch (IllegalStateException e) {
            log.warn("Admin cannot mark listing {} as sold: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.CONFLICT, "Listing cannot be marked as sold in its current state. It may already be sold or archived."));
        } catch (Exception e) {
            log.error("Unexpected error when admin marking listing {} as sold", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred. Please contact support if the problem persists."));
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        }
    }

    @Operation(
        summary = "Approve a listing",
<<<<<<< HEAD
        description = "Approves a car listing for public display. Admin-only operation. " +
                     "The listing must not already be approved. Once approved, the listing will appear in public searches.",
=======
        description = "Approves a car listing, making it visible in public searches. Admin-only operation. " +
                     "A listing must be approved before it can be viewed by the public.",
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
<<<<<<< HEAD
            responseCode = "200", 
=======
            responseCode = "200",
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            description = "Listing successfully approved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CarListingResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid listing ID supplied",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Admin privileges required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Listing not found",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Listing cannot be approved (e.g., already approved)",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        )
    })
    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<CarListingResponse>> approveListing(
=======
    public ResponseEntity<com.autotrader.autotraderbackend.payload.response.ApiResponse<CarListingResponse>> approveListing(
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            @Parameter(description = "ID of the listing to approve", required = true)
            @PathVariable("id") @NotNull Long id) {
        log.debug("Request to approve listing {}", id);
        
        try {
            CarListingResponse response = carListingStatusService.approveListing(id);
            log.info("Successfully approved listing {}", id);
<<<<<<< HEAD
            return ResponseEntity.ok(ApiResponse.success(response, "Listing approved successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to approve", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot approve listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied when attempting to approve listing {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to approve listings"));
        } catch (Exception e) {
            log.error("Unexpected error when approving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
=======
            return ResponseEntity.ok(com.autotrader.autotraderbackend.payload.response.ApiResponse.success(response, "Listing approved successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to approve", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot approve listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied when attempting to approve listing {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to approve listings"));
        } catch (Exception e) {
            log.error("Unexpected error when approving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        }
    }

    @Operation(
        summary = "Pause a listing",
<<<<<<< HEAD
        description = "Temporarily pauses a car listing from being displayed in searches. Only the listing owner can perform this action. " +
                     "The listing must not be sold or archived.",
=======
        description = "Temporarily hides a car listing from public view. Only the owner can pause their listing. " +
                     "The listing must not be sold or archived. Use this to temporarily remove a listing from searches.",
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Listing successfully paused",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CarListingResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid listing ID supplied",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "User is not authorized to pause this listing",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Listing not found",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Listing cannot be paused (e.g., already paused, sold, or archived)",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<CarListingResponse>> pauseListing(
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
        )
    })
    @PostMapping("/pause")
    public ResponseEntity<com.autotrader.autotraderbackend.payload.response.ApiResponse<CarListingResponse>> pauseListing(
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            @Parameter(description = "ID of the listing to pause", required = true)
            @PathVariable("id") @NotNull Long id,
            Principal principal) {
        log.debug("Request to pause listing {} by user {}", id, principal.getName());
        
        try {
            CarListingResponse response = carListingStatusService.pauseListing(id, principal.getName());
            log.info("Successfully paused listing {} by user {}", id, principal.getName());
<<<<<<< HEAD
            return ResponseEntity.ok(ApiResponse.success(response, "Listing paused successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to pause", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot pause listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to pause listing {}", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to pause this listing"));
        } catch (Exception e) {
            log.error("Unexpected error when pausing listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
=======
            return ResponseEntity.ok(com.autotrader.autotraderbackend.payload.response.ApiResponse.success(response, "Listing paused successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to pause", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot pause listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to pause listing {}", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to pause this listing"));
        } catch (Exception e) {
            log.error("Unexpected error when pausing listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        }
    }

    @Operation(
        summary = "Resume a listing",
<<<<<<< HEAD
        description = "Resumes a previously paused car listing so it appears in searches again. Only the listing owner can perform this action. " +
                     "The listing must be paused and not sold or archived.",
=======
        description = "Makes a paused car listing visible in public searches again. Only the owner can resume their listing. " +
                     "The listing must be paused and not be sold or archived.",
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Listing successfully resumed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CarListingResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid listing ID supplied",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "User is not authorized to resume this listing",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Listing not found",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Listing cannot be resumed (e.g., not paused, sold, or archived)",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<CarListingResponse>> resumeListing(
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
        )
    })
    @PostMapping("/resume")
    public ResponseEntity<com.autotrader.autotraderbackend.payload.response.ApiResponse<CarListingResponse>> resumeListing(
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            @Parameter(description = "ID of the listing to resume", required = true)
            @PathVariable("id") @NotNull Long id,
            Principal principal) {
        log.debug("Request to resume listing {} by user {}", id, principal.getName());
        
        try {
            CarListingResponse response = carListingStatusService.resumeListing(id, principal.getName());
            log.info("Successfully resumed listing {} by user {}", id, principal.getName());
<<<<<<< HEAD
            return ResponseEntity.ok(ApiResponse.success(response, "Listing resumed successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to resume", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot resume listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to resume listing {}", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to resume this listing"));
        } catch (Exception e) {
            log.error("Unexpected error when resuming listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
=======
            return ResponseEntity.ok(com.autotrader.autotraderbackend.payload.response.ApiResponse.success(response, "Listing resumed successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to resume", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot resume listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to resume listing {}", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to resume this listing"));
        } catch (Exception e) {
            log.error("Unexpected error when resuming listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        }
    }

    @Operation(
        summary = "Archive a listing",
<<<<<<< HEAD
        description = "Archives a car listing, removing it from public searches. Only the listing owner can perform this action. " +
                     "This operation is used for listings that are no longer relevant but should be preserved for records.",
=======
        description = "Permanently archives a car listing, hiding it from public view. Only the owner can archive their listing. " +
                     "Archived listings cannot be modified without being unarchived first.",
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
<<<<<<< HEAD
            responseCode = "200", 
=======
            responseCode = "200",
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            description = "Listing successfully archived",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CarListingResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid listing ID supplied",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "User is not authorized to archive this listing",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Listing not found",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Listing cannot be archived (e.g., already archived)",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/archive")
    public ResponseEntity<ApiResponse<CarListingResponse>> archiveListing(
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
        )
    })
    @PostMapping("/archive")
    public ResponseEntity<com.autotrader.autotraderbackend.payload.response.ApiResponse<CarListingResponse>> archiveListing(
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            @Parameter(description = "ID of the listing to archive", required = true)
            @PathVariable("id") @NotNull Long id,
            Principal principal) {
        log.debug("Request to archive listing {} by user {}", id, principal.getName());
<<<<<<< HEAD
        try {
            CarListingResponse response = carListingStatusService.archiveListing(id, principal.getName());
            log.info("Successfully archived listing {} by user {}", id, principal.getName());
            return ResponseEntity.ok(ApiResponse.success(response, "Listing archived successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to archive", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot archive listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to archive listing {}", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to archive this listing"));
        } catch (Exception e) {
            log.error("Unexpected error when archiving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
=======
        
        try {
            CarListingResponse response = carListingStatusService.archiveListing(id, principal.getName());
            log.info("Successfully archived listing {} by user {}", id, principal.getName());
            return ResponseEntity.ok(com.autotrader.autotraderbackend.payload.response.ApiResponse.success(response, "Listing archived successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to archive", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot archive listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to archive listing {}", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to archive this listing"));
        } catch (Exception e) {
            log.error("Unexpected error when archiving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        }
    }

    @Operation(
        summary = "Archive a listing (Admin)",
<<<<<<< HEAD
        description = "Archives a car listing, removing it from public searches. Admin-only operation that bypasses ownership checks. " +
                     "This operation is used for listings that are no longer relevant but should be preserved for records.",
=======
        description = "Permanently archives a car listing, hiding it from public view. Admin-only operation that bypasses " +
                     "ownership checks. Archived listings cannot be modified without being unarchived first.",
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
<<<<<<< HEAD
            description = "Listing successfully archived by admin",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CarListingResponse.class)
=======
            description = "Listing successfully archived",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "Listing archived by admin successfully",
                        "data": {
                            "id": 123,
                            "archived": true,
                            "modifiedAt": "2024-03-21T10:15:30.123Z"
                        }
                    }
                    """
                )
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid listing ID supplied",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Admin privileges required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Listing not found",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Listing cannot be archived (e.g., already archived)",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        )
    })
    @PostMapping("/archive/admin")
    @PreAuthorize("hasRole('ADMIN')")
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<CarListingResponse>> archiveListingByAdmin(
=======
    public ResponseEntity<com.autotrader.autotraderbackend.payload.response.ApiResponse<CarListingResponse>> archiveListingByAdmin(
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            @Parameter(description = "ID of the listing to archive", required = true)
            @PathVariable("id") @NotNull Long id) {
        log.debug("Admin request to archive listing {}", id);
        try {
            CarListingResponse response = carListingStatusService.archiveListingByAdmin(id);
            log.info("Admin successfully archived listing {}", id);
<<<<<<< HEAD
            return ResponseEntity.ok(ApiResponse.success(response, "Listing archived by admin successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when admin attempting to archive", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Admin cannot archive listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error when admin archiving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
=======
            return ResponseEntity.ok(com.autotrader.autotraderbackend.payload.response.ApiResponse.success(response, "Listing archived by admin successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when admin attempting to archive", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Admin cannot archive listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error when admin archiving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        }
    }

    @Operation(
        summary = "Unarchive a listing",
<<<<<<< HEAD
        description = "Unarchives a car listing, making it available in public searches again. Only the listing owner can perform this action. " +
                     "The listing must be archived.",
=======
        description = "Makes an archived car listing visible and modifiable again. Only the owner can unarchive their listing.",
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Listing successfully unarchived",
            content = @Content(
                mediaType = "application/json",
<<<<<<< HEAD
                schema = @Schema(implementation = CarListingResponse.class)
=======
                schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "Listing unarchived successfully",
                        "data": {
                            "id": 123,
                            "archived": false,
                            "modifiedAt": "2024-03-21T10:15:30.123Z"
                        }
                    }
                    """
                )
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid listing ID supplied",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "User is not authorized to unarchive this listing",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Listing not found",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Listing cannot be unarchived (e.g., not archived)",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/unarchive")
    public ResponseEntity<ApiResponse<CarListingResponse>> unarchiveListing(
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
        )
    })
    @PostMapping("/unarchive")
    public ResponseEntity<com.autotrader.autotraderbackend.payload.response.ApiResponse<CarListingResponse>> unarchiveListing(
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            @Parameter(description = "ID of the listing to unarchive", required = true)
            @PathVariable("id") @NotNull Long id,
            Principal principal) {
        log.debug("Request to unarchive listing {} by user {}", id, principal.getName());
        try {
            CarListingResponse response = carListingStatusService.unarchiveListing(id, principal.getName());
            log.info("Successfully unarchived listing {} by user {}", id, principal.getName());
<<<<<<< HEAD
            return ResponseEntity.ok(ApiResponse.success(response, "Listing unarchived successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to unarchive", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Cannot unarchive listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to unarchive listing {}", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to unarchive this listing"));
        } catch (Exception e) {
            log.error("Unexpected error when unarchiving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
=======
            return ResponseEntity.ok(com.autotrader.autotraderbackend.payload.response.ApiResponse.success(response, "Listing unarchived successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when attempting to unarchive", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.NOT_FOUND, "Listing not found. Please check the ID and try again."));
        } catch (IllegalStateException e) {
            log.warn("Cannot unarchive listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.CONFLICT, "Listing cannot be unarchived in its current state."));
        } catch (AccessDeniedException e) {
            log.warn("Access denied for user {} attempting to unarchive listing {}", principal.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to unarchive this listing."));
        } catch (Exception e) {
            log.error("Unexpected error when unarchiving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred. Please contact support if the problem persists."));
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        }
    }

    @Operation(
        summary = "Unarchive a listing (Admin)",
<<<<<<< HEAD
        description = "Unarchives a car listing, making it available in public searches again. Admin-only operation that bypasses ownership checks. " +
                     "The listing must be archived.",
=======
        description = "Makes an archived car listing visible and modifiable again. Admin-only operation that bypasses " +
                     "ownership checks.",
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
<<<<<<< HEAD
            description = "Listing successfully unarchived by admin",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CarListingResponse.class)
=======
            description = "Listing successfully unarchived",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "Listing unarchived by admin successfully",
                        "data": {
                            "id": 123,
                            "archived": false,
                            "modifiedAt": "2024-03-21T10:15:30.123Z"
                        }
                    }
                    """
                )
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid listing ID supplied",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Admin privileges required",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Listing not found",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Listing cannot be unarchived (e.g., not archived)",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
<<<<<<< HEAD
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
=======
            content = @Content(schema = @Schema(implementation = com.autotrader.autotraderbackend.payload.response.ApiResponse.class))
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        )
    })
    @PostMapping("/unarchive/admin")
    @PreAuthorize("hasRole('ADMIN')")
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<CarListingResponse>> unarchiveListingByAdmin(
=======
    public ResponseEntity<com.autotrader.autotraderbackend.payload.response.ApiResponse<CarListingResponse>> unarchiveListingByAdmin(
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
            @Parameter(description = "ID of the listing to unarchive", required = true)
            @PathVariable("id") @NotNull Long id) {
        log.debug("Admin request to unarchive listing {}", id);
        try {
            CarListingResponse response = carListingStatusService.unarchiveListingByAdmin(id);
            log.info("Admin successfully unarchived listing {}", id);
<<<<<<< HEAD
            return ResponseEntity.ok(ApiResponse.success(response, "Listing unarchived by admin successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when admin attempting to unarchive", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Admin cannot unarchive listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error when admin unarchiving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
=======
            return ResponseEntity.ok(com.autotrader.autotraderbackend.payload.response.ApiResponse.success(response, "Listing unarchived by admin successfully"));
        } catch (ResourceNotFoundException e) {
            log.warn("Listing {} not found when admin attempting to unarchive", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Admin cannot unarchive listing {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error when admin unarchiving listing {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.autotrader.autotraderbackend.payload.response.ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
>>>>>>> 0848d33 (Refactor CarListingStatusService and ListingStatusControllerTest)
        }
    }
}
