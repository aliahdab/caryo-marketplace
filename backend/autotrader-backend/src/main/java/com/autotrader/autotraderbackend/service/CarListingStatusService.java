package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import com.autotrader.autotraderbackend.events.ListingApprovedEvent;
import com.autotrader.autotraderbackend.events.ListingArchivedEvent;
import com.autotrader.autotraderbackend.events.ListingMarkedAsSoldEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarListingStatusService {

    private final CarListingRepository carListingRepository;
    private final UserRepository userRepository;
    private final CarListingMapper carListingMapper;
    private final ApplicationEventPublisher eventPublisher;

    // --- Helper Methods (moved from CarListingService, now public) ---

    public User findUserByUsername(String username) {
        Objects.requireNonNull(username, "Username cannot be null");
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User lookup failed for username: {}", username);
                    return new ResourceNotFoundException("User", "username", username);
                });
    }

    public CarListing findListingById(Long listingId) {
        Objects.requireNonNull(listingId, "Listing ID cannot be null");
        return carListingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.warn("CarListing lookup failed for ID: {}", listingId);
                    return new ResourceNotFoundException("CarListing", "id", listingId);
                });
    }

    public void authorizeListingModification(CarListing listing, User user, String action) {
        Objects.requireNonNull(listing, "CarListing cannot be null for authorization");
        Objects.requireNonNull(user, "User cannot be null for authorization");
        Objects.requireNonNull(action, "Action cannot be null for authorization");

        if (Objects.isNull(listing.getSeller()) || !listing.getSeller().getId().equals(user.getId())) {
            log.warn("Authorization failed: User '{}' (ID: {}) attempted to {} listing ID {} owned by '{}' (ID: {})",
                    user.getUsername(), user.getId(), action, listing.getId(),
                    Objects.nonNull(listing.getSeller()) ? listing.getSeller().getUsername() : "unknown",
                    Objects.nonNull(listing.getSeller()) ? listing.getSeller().getId() : "unknown");
            throw new SecurityException("User does not have permission to modify this listing.");
        }
    }

    // This method can remain private as it's only used internally by other public methods in this service.
    private CarListing findListingByIdAndAuthorize(Long listingId, String username, String action) {
        User user = findUserByUsername(username);
        CarListing listing = findListingById(listingId);
        authorizeListingModification(listing, user, action);
        return listing;
    }

    // --- Status Transition Methods ---

    @Transactional
    public CarListingResponse approveListing(Long id) {
        log.info("Attempting to approve listing with ID: {}", id);
        CarListing carListing = findListingById(id);

        if (Boolean.TRUE.equals(carListing.getApproved())) {
            log.warn("Listing ID {} is already approved. No action taken.", id);
            throw new IllegalStateException("Listing with ID " + id + " is already approved.");
        }

        carListing.setApproved(true);
        CarListing approvedListing = carListingRepository.save(carListing);
        log.info("Successfully approved listing ID: {}", approvedListing.getId());

        eventPublisher.publishEvent(new ListingApprovedEvent(this, approvedListing));
        log.info("Published ListingApprovedEvent for listing ID: {}", approvedListing.getId());

        return carListingMapper.toCarListingResponse(approvedListing);
    }

    @Transactional
    public CarListingResponse pauseListing(Long listingId, String username) {
        log.info("User {} attempting to pause listing ID {}", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "pause");

        if (!Boolean.TRUE.equals(listing.getApproved())) {
            log.warn("User {} attempted to pause unapproved listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot pause a listing that is not yet approved.");
        }
        if (Boolean.TRUE.equals(listing.getSold())) {
            log.warn("User {} attempted to pause sold listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot pause a listing that has been marked as sold.");
        }
        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("User {} attempted to pause archived listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot pause a listing that has been archived.");
        }
        if (!Boolean.TRUE.equals(listing.getIsUserActive())) { // Check if already paused
            log.warn("Listing ID {} is already paused by user {}. Throwing IllegalStateException.", listingId, username);
            throw new IllegalStateException("Listing with ID " + listingId + " is already paused.");
        }

        listing.setIsUserActive(false);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Successfully paused listing ID {} by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    @Transactional
    public CarListingResponse resumeListing(Long listingId, String username) {
        log.info("User {} attempting to resume listing ID {}", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "resume");

        if (Boolean.TRUE.equals(listing.getSold())) {
            log.warn("User {} attempted to resume sold listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot resume a listing that has been marked as sold.");
        }
        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("User {} attempted to resume archived listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot resume a listing that has been archived. Please contact support or renew if applicable.");
        }
        if (Boolean.TRUE.equals(listing.getIsUserActive())) { // Check if already active
            log.warn("Listing ID {} is already active for user {}. Throwing IllegalStateException.", listingId, username);
            throw new IllegalStateException("Listing with ID " + listingId + " is already active.");
        }

        listing.setIsUserActive(true);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Successfully resumed listing ID {} by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    @Transactional
    public CarListingResponse markListingAsSold(Long listingId, String username) {
        log.info("User {} attempting to mark listing ID {} as sold", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "mark as sold");

        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Attempt to mark archived listing ID {} as sold by user {}", listingId, username);
            throw new IllegalStateException("Cannot mark an archived listing as sold. Please unarchive first.");
        }
        if (Boolean.TRUE.equals(listing.getSold())) {
            log.warn("Listing ID {} is already marked as sold. Throwing exception for user {}.", listingId, username);
            throw new IllegalStateException("Listing with ID " + listingId + " is already marked as sold.");
        }

        listing.setSold(true);
        CarListing updatedListing = carListingRepository.save(listing);

        eventPublisher.publishEvent(new ListingMarkedAsSoldEvent(this, updatedListing, false));
        log.info("Successfully marked listing ID {} as sold by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    @Transactional
    public CarListingResponse markListingAsSoldByAdmin(Long listingId) {
        log.info("Admin attempting to mark listing ID {} as sold", listingId);
        CarListing listing = findListingById(listingId);

        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Admin attempt to mark archived listing ID {} as sold", listingId);
            throw new IllegalStateException("Cannot mark an archived listing as sold. Please unarchive first.");
        }

        if (Boolean.TRUE.equals(listing.getSold())) {
            log.warn("Listing ID {} is already marked as sold. Throwing exception for admin.", listingId);
            throw new IllegalStateException("Listing with ID " + listingId + " is already marked as sold.");
        }
        
        listing.setSold(true);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Admin successfully marked listing ID {} as sold", listingId);
        // Publish event with isAdminAction = true
        eventPublisher.publishEvent(new ListingMarkedAsSoldEvent(this, updatedListing, true));
        log.info("Published ListingMarkedAsSoldEvent for listing ID: {} (isAdminAction: true)", updatedListing.getId());
        return carListingMapper.toCarListingResponseForAdmin(updatedListing); // Assuming this method exists
    }

    @Transactional
    public CarListingResponse archiveListing(Long listingId, String username) {
        log.info("User {} attempting to archive listing ID {}", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "archive");

        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Listing ID {} is already archived. Throwing exception for user {}.", listingId, username);
            throw new IllegalStateException("Listing with ID " + listingId + " is already archived.");
        }

        listing.setArchived(true);
        CarListing updatedListing = carListingRepository.save(listing);

        eventPublisher.publishEvent(new ListingArchivedEvent(this, updatedListing, false));
        log.info("Published ListingArchivedEvent for listing ID: {} (isAdminAction: false)", updatedListing.getId());
        
        log.info("Successfully archived listing ID {} by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    @Transactional
    public CarListingResponse archiveListingByAdmin(Long listingId) {
        log.info("Admin attempting to archive listing ID {}", listingId);
        CarListing listing = findListingById(listingId);

        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Listing ID {} is already archived. Throwing exception for admin.", listingId);
            throw new IllegalStateException("Listing with ID " + listingId + " is already archived.");
        }

        listing.setArchived(true);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Admin successfully archived listing ID {}", listingId);
        // Publish event with isAdminAction = true
        eventPublisher.publishEvent(new ListingArchivedEvent(this, updatedListing, true));
        log.info("Published ListingArchivedEvent for listing ID: {} (isAdminAction: true)", updatedListing.getId());
        return carListingMapper.toCarListingResponse(updatedListing); // Or a specific admin response if available
    }

    @Transactional
    public CarListingResponse unarchiveListing(Long listingId, String username) {
        log.info("User {} attempting to unarchive listing ID {}", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "unarchive");

        if (!Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Listing ID {} is not archived. No action taken for unarchive by user {}.", listingId, username);
            throw new IllegalStateException("Listing with ID " + listingId + " is not archived.");
        }

        listing.setArchived(false);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Successfully unarchived listing ID {} by user {}", listingId, username);
        // Optionally publish an UnarchivedEvent if one exists
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    @Transactional
    public CarListingResponse unarchiveListingByAdmin(Long listingId) {
        log.info("Admin attempting to unarchive listing ID {}", listingId);
        CarListing listing = findListingById(listingId);

        if (!Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Listing ID {} is not archived. No action taken for unarchive by admin.", listingId);
            throw new IllegalStateException("Listing with ID " + listingId + " is not archived.");
        }

        listing.setArchived(false);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Admin successfully unarchived listing ID {}", listingId);
        // Optionally publish an UnarchivedEvent if one exists
        return carListingMapper.toCarListingResponse(updatedListing); // Or a specific admin response
    }
}
