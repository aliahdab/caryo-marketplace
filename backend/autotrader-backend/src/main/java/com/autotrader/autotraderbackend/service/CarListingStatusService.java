package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.events.ListingApprovedEvent;
import com.autotrader.autotraderbackend.events.ListingArchivedEvent;
import com.autotrader.autotraderbackend.events.ListingExpiredEvent;
import com.autotrader.autotraderbackend.events.ListingMarkedAsSoldEvent;
import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Service responsible for managing the status of car listings.
 * This includes operations like marking listings as sold, approving listings,
 * pausing/resuming listings, and archiving/unarchiving listings.
 *
 * All operations are transactional and properly validated.
 * Status changes trigger appropriate events for system-wide consistency.
 *
 * @author AutoTrader Team
 * @version 2.0
 * @since 2025-05-20
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CarListingStatusService {

    private final CarListingRepository carListingRepository;
    private final UserRepository userRepository;
    private final CarListingMapper carListingMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Marks a car listing as sold by its owner.
     * 
     * @param listingId The ID of the listing to mark as sold
     * @param username The username of the user attempting to mark the listing as sold
     * @return Updated car listing response
     * @throws ResourceNotFoundException if the listing or user is not found
     * @throws SecurityException if the user is not authorized to modify the listing
     * @throws IllegalStateException if the listing is already sold or archived
     */
    @Transactional
    public CarListingResponse markListingAsSold(@NonNull Long listingId, @NonNull String username) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");
        Objects.requireNonNull(username, "Username must not be null");

        User user = findUserByUsername(username);
        CarListing listing = findListingById(listingId);
        
        authorizeListingModification(listing, user, "mark as sold");
        validateListingCanBeMarkedAsSold(listing);

        listing.setSold(true);
        CarListing savedListing = carListingRepository.save(listing);
        eventPublisher.publishEvent(new ListingMarkedAsSoldEvent(this, savedListing, false));
        
        log.info("Listing {} marked as sold by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(savedListing);
    }

    /**
     * Marks a car listing as sold by an admin.
     *
     * @param listingId The ID of the listing to mark as sold
     * @return Updated car listing response with admin-specific fields
     * @throws ResourceNotFoundException if the listing is not found
     * @throws IllegalStateException if the listing is already sold or archived
     */
    @Transactional
    public CarListingResponse markListingAsSoldByAdmin(@NonNull Long listingId) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");

        CarListing listing = findListingById(listingId);
        validateListingCanBeMarkedAsSold(listing);

        listing.setSold(true);
        CarListing savedListing = carListingRepository.save(listing);
        eventPublisher.publishEvent(new ListingMarkedAsSoldEvent(this, savedListing, true));
        
        log.info("Listing {} marked as sold by admin", listingId);
        return carListingMapper.toCarListingResponseForAdmin(savedListing);
    }

    /**
     * Approves a car listing
     *
     * @param listingId The ID of the listing to approve
     * @return Updated car listing response
     * @throws ResourceNotFoundException if the listing is not found
     * @throws IllegalStateException if the listing is already approved
     */
    @Transactional
    public CarListingResponse approveListing(@NonNull Long listingId) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");

        CarListing listing = findListingById(listingId);
        validateListingCanBeApproved(listing);

        listing.setApproved(true);
        CarListing savedListing = carListingRepository.save(listing);
        eventPublisher.publishEvent(new ListingApprovedEvent(this, savedListing));
        
        log.info("Listing {} approved", listingId);
        return carListingMapper.toCarListingResponse(savedListing);
    }

    /**
     * Temporarily pauses (hides) a car listing from public view.
     *
     * @param listingId The ID of the listing to pause
     * @param username The username of the user attempting to pause the listing
     * @return Updated car listing response
     * @throws ResourceNotFoundException if the listing or user is not found
     * @throws SecurityException if the user is not authorized to modify the listing
     * @throws IllegalStateException if the listing is already paused, sold, or archived
     */
    @Transactional
    public CarListingResponse pauseListing(@NonNull Long listingId, @NonNull String username) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");
        Objects.requireNonNull(username, "Username must not be null");

        User user = findUserByUsername(username);
        CarListing listing = findListingById(listingId);
        
        authorizeListingModification(listing, user, "pause");
        validateListingCanBePaused(listing);

        listing.setIsUserActive(false);
        CarListing savedListing = carListingRepository.save(listing);
        
        log.info("Listing {} paused by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(savedListing);
    }

    /**
     * Resumes (unhides) a paused car listing, making it visible in public view again.
     *
     * @param listingId The ID of the listing to resume
     * @param username The username of the user attempting to resume the listing
     * @return Updated car listing response
     * @throws ResourceNotFoundException if the listing or user is not found
     * @throws SecurityException if the user is not authorized to modify the listing
     * @throws IllegalStateException if the listing is already active, sold, or archived
     */
    @Transactional
    public CarListingResponse resumeListing(@NonNull Long listingId, @NonNull String username) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");
        Objects.requireNonNull(username, "Username must not be null");

        User user = findUserByUsername(username);
        CarListing listing = findListingById(listingId);
        
        authorizeListingModification(listing, user, "resume");
        validateListingCanBeResumed(listing);

        listing.setIsUserActive(true);
        CarListing savedListing = carListingRepository.save(listing);
        
        log.info("Listing {} resumed by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(savedListing);
    }

    /**
     * Archives a car listing by its owner. Archived listings are hidden from public view
     * and cannot be modified without being unarchived first.
     *
     * @param listingId The ID of the listing to archive
     * @param username The username of the user attempting to archive the listing
     * @return Updated car listing response
     * @throws ResourceNotFoundException if the listing or user is not found
     * @throws SecurityException if the user is not authorized to modify the listing
     * @throws IllegalStateException if the listing is already archived
     */
    @Transactional
    public CarListingResponse archiveListing(@NonNull Long listingId, @NonNull String username) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");
        Objects.requireNonNull(username, "Username must not be null");

        User user = findUserByUsername(username);
        CarListing listing = findListingById(listingId);
        
        authorizeListingModification(listing, user, "archive");
        validateListingCanBeArchived(listing);

        listing.setArchived(true);
        CarListing savedListing = carListingRepository.save(listing);
        eventPublisher.publishEvent(new ListingArchivedEvent(this, savedListing, false));
        
        log.info("Listing {} archived by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(savedListing);
    }

    /**
     * Archives a car listing by an admin. Archived listings are hidden from public view
     * and cannot be modified without being unarchived first.
     *
     * @param listingId The ID of the listing to archive
     * @return Updated car listing response with admin-specific fields
     * @throws ResourceNotFoundException if the listing is not found
     * @throws IllegalStateException if the listing is already archived
     */
    @Transactional
    public CarListingResponse archiveListingByAdmin(@NonNull Long listingId) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");

        CarListing listing = findListingById(listingId);
        validateListingCanBeArchived(listing);

        listing.setArchived(true);
        CarListing savedListing = carListingRepository.save(listing);
        eventPublisher.publishEvent(new ListingArchivedEvent(this, savedListing, true));
        
        log.info("Listing {} archived by admin", listingId);
        return carListingMapper.toCarListingResponseForAdmin(savedListing);
    }

    /**
     * Unarchives a car listing by its owner. Makes the listing visible and modifiable again.
     *
     * @param listingId The ID of the listing to unarchive
     * @param username The username of the user attempting to unarchive the listing
     * @return Updated car listing response
     * @throws ResourceNotFoundException if the listing or user is not found
     * @throws SecurityException if the user is not authorized to modify the listing
     * @throws IllegalStateException if the listing is not archived
     */
    @Transactional
    public CarListingResponse unarchiveListing(@NonNull Long listingId, @NonNull String username) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");
        Objects.requireNonNull(username, "Username must not be null");

        User user = findUserByUsername(username);
        CarListing listing = findListingById(listingId);
        
        authorizeListingModification(listing, user, "unarchive");
        validateListingCanBeUnarchived(listing);

        listing.setArchived(false);
        CarListing savedListing = carListingRepository.save(listing);
        
        log.info("Listing {} unarchived by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(savedListing);
    }

    /**
     * Unarchives a car listing by an admin. Makes the listing visible and modifiable again.
     *
     * @param listingId The ID of the listing to unarchive
     * @return Updated car listing response with admin-specific fields
     * @throws ResourceNotFoundException if the listing is not found
     * @throws IllegalStateException if the listing is not archived
     */
    @Transactional
    public CarListingResponse unarchiveListingByAdmin(@NonNull Long listingId) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");

        CarListing listing = findListingById(listingId);
        validateListingCanBeUnarchived(listing);

        listing.setArchived(false);
        CarListing savedListing = carListingRepository.save(listing);
        
        log.info("Listing {} unarchived by admin", listingId);
        return carListingMapper.toCarListingResponseForAdmin(savedListing);
    }

    /**
     * Marks a car listing as expired. This is typically called by the system
     * when a listing reaches its expiration date.
     *
     * @param listingId The ID of the listing to expire
     * @return Updated car listing response with admin-specific fields since this is a system action
     * @throws ResourceNotFoundException if the listing is not found
     * @throws IllegalStateException if the listing is already expired, sold, or archived
     */
    @Transactional
    public CarListingResponse expireListing(@NonNull Long listingId) {
        Objects.requireNonNull(listingId, "Listing ID must not be null");

        CarListing listing = findListingById(listingId);
        validateListingCanBeExpired(listing);

        listing.setExpired(true);
        CarListing savedListing = carListingRepository.save(listing);
        eventPublisher.publishEvent(new ListingExpiredEvent(this, savedListing));
        
        log.info("Listing {} expired", listingId);
        return carListingMapper.toCarListingResponseForAdmin(savedListing);
    }

    // Helper methods

    /**
     * Finds a user by username.
     *
     * @param username The username to look up
     * @return The User entity
     * @throws ResourceNotFoundException if the user is not found
     */
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User lookup failed for username: {}", username);
                    return new ResourceNotFoundException("User", "username", username);
                });
    }

    /**
     * Finds a car listing by ID.
     *
     * @param listingId The listing ID to look up
     * @return The CarListing entity
     * @throws ResourceNotFoundException if the listing is not found
     */
    public CarListing findListingById(Long listingId) {
        return carListingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.warn("Listing lookup failed for ID: {}", listingId);
                    return new ResourceNotFoundException("CarListing", "id", listingId);
                });
    }

    /**
     * Verifies that the user has permission to modify the listing.
     *
     * @param listing The listing to check
     * @param user The user attempting the modification
     * @param action Description of the action being attempted (for logging)
     * @throws SecurityException if the user does not own the listing
     */
    public void authorizeListingModification(CarListing listing, User user, String action) {
        if (!listing.getSeller().getId().equals(user.getId())) {
            log.warn("Authorization failed: User {} (ID: {}) attempted to {} listing {} owned by user ID: {}", 
                    user.getUsername(), user.getId(), action, listing.getId(), listing.getSeller().getId());
            throw new SecurityException("User does not have permission to modify this listing.");
        }
    }

    /**
     * Validates that a listing can be marked as sold.
     *
     * @param listing The listing to validate
     * @throws IllegalStateException if the listing cannot be marked as sold
     */
    private void validateListingCanBeMarkedAsSold(CarListing listing) {
        if (listing.getSold()) {
            throw new IllegalStateException(String.format("Listing with ID %d is already marked as sold.", listing.getId()));
        }
        if (listing.getArchived()) {
            throw new IllegalStateException("Cannot mark an archived listing as sold. Please unarchive first.");
        }
    }

    /**
     * Validates that a listing can be approved.
     *
     * @param listing The listing to validate
     * @throws IllegalStateException if the listing cannot be approved
     */
    private void validateListingCanBeApproved(CarListing listing) {
        if (listing.getApproved()) {
            throw new IllegalStateException(String.format("Listing with ID %d is already approved.", listing.getId()));
        }
        if (listing.getArchived()) {
            throw new IllegalStateException("Cannot approve an archived listing. Please unarchive first.");
        }
        if (listing.getSold()) {
            throw new IllegalStateException("Cannot approve a sold listing");
        }
    }

    /**
     * Validates that a listing can be paused.
     *
     * @param listing The listing to validate
     * @throws IllegalStateException if the listing cannot be paused
     */
    private void validateListingCanBePaused(CarListing listing) {
        if (!listing.getIsUserActive()) {
            throw new IllegalStateException(String.format("Listing with ID %d is already paused.", listing.getId()));
        }
        if (listing.getSold()) {
            throw new IllegalStateException("Cannot pause a listing that has been marked as sold.");
        }
        if (listing.getArchived()) {
            throw new IllegalStateException("Cannot pause a listing that has been archived.");
        }
    }

    /**
     * Validates that a listing can be resumed.
     *
     * @param listing The listing to validate
     * @throws IllegalStateException if the listing cannot be resumed
     */
    private void validateListingCanBeResumed(CarListing listing) {
        if (listing.getIsUserActive()) {
            throw new IllegalStateException(String.format("Listing with ID %d is already active.", listing.getId()));
        }
        if (listing.getSold()) {
            throw new IllegalStateException("Cannot resume a listing that has been marked as sold.");
        }
        if (listing.getArchived()) {
            throw new IllegalStateException("Cannot resume a listing that has been archived. Please contact support or renew if applicable.");
        }
    }

    /**
     * Validates that a listing can be archived.
     *
     * @param listing The listing to validate
     * @throws IllegalStateException if the listing cannot be archived
     */
    private void validateListingCanBeArchived(CarListing listing) {
        if (listing.getArchived()) {
            throw new IllegalStateException(String.format("Listing with ID %d is already archived.", listing.getId()));
        }
    }

    /**
     * Validates that a listing can be unarchived.
     *
     * @param listing The listing to validate
     * @throws IllegalStateException if the listing cannot be unarchived
     */
    private void validateListingCanBeUnarchived(CarListing listing) {
        if (!listing.getArchived()) {
            throw new IllegalStateException(String.format("Listing with ID %d is not archived.", listing.getId()));
        }
    }

    /**
     * Validates that a listing can be expired.
     *
     * @param listing The listing to validate
     * @throws IllegalStateException if the listing cannot be expired
     */
    private void validateListingCanBeExpired(CarListing listing) {
        if (listing.getExpired()) {
            throw new IllegalStateException("Listing is already expired");
        }
        if (listing.getSold()) {
            throw new IllegalStateException("Cannot expire a sold listing");
        }
        if (listing.getArchived()) {
            throw new IllegalStateException("Cannot expire an archived listing");
        }
    }
}
