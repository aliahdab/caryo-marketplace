package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.events.ListingArchivedEvent;
import com.autotrader.autotraderbackend.events.ListingMarkedAsSoldEvent;
import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.exception.StorageException;
import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.ListingMedia;
import com.autotrader.autotraderbackend.model.Location;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.payload.request.CreateListingRequest;
import com.autotrader.autotraderbackend.payload.request.UpdateListingRequest;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service responsible for create, update, and delete operations on car listings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CarListingCommandService {
    private final CarListingRepository carListingRepository;
    private final LocationRepository locationRepository;
    private final CarListingMapper carListingMapper;
    private final CarListingServiceHelper serviceHelper;
    private final CarListingMediaService mediaService;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Create a new car listing.
     */
    @Transactional
    public CarListingResponse createListing(CreateListingRequest request, MultipartFile image, String username) {
        log.info("Attempting to create new listing for user: {}", username);
        User user = serviceHelper.findUserByUsername(username);

        CarListing carListing = buildCarListingFromRequest(request, user);
        // isSold and isArchived are set within buildCarListingFromRequest

        CarListing savedListing = carListingRepository.save(carListing);

        // Handle image upload if provided
        if (image != null && !image.isEmpty()) {
            try {
                mediaService.addImageToListing(savedListing, image);
                log.info("Successfully uploaded image for new listing ID: {}", savedListing.getId());
            } catch (Exception e) {
                // If image upload/update fails, log it but proceed with listing creation response
                log.error("Error handling image for listing ID {}: {}", savedListing.getId(), e.getMessage(), e);
            }
        }

        log.info("Successfully created new listing with ID: {} for user: {}", savedListing.getId(), username);
        return carListingMapper.toCarListingResponse(savedListing);
    }

    /**
     * Update an existing car listing.
     *
     * @param id         The ID of the car listing to update
     * @param request    Updated listing details
     * @param username   The username of the user making the request
     * @return The updated CarListingResponse
     * @throws ResourceNotFoundException If the listing does not exist
     * @throws SecurityException If the user does not own the listing
     */
    @Transactional
    public CarListingResponse updateListing(Long id, UpdateListingRequest request, String username) {
        log.info("Attempting to update listing with ID: {} by user: {}", id, username);
        
        CarListing existingListing = carListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarListing", "id", id));
        
        // Check if the user owns this listing
        if (!existingListing.getSeller().getUsername().equals(username)) {
            log.warn("User {} attempted to update listing {} owned by {}", 
                    username, id, existingListing.getSeller().getUsername());
            throw new SecurityException("You are not authorized to update this listing");
        }
        
        // Store original status for event publishing
        boolean originalIsSold = existingListing.getSold();
        boolean originalIsArchived = existingListing.getArchived();

        // Update only non-null fields
        if (request.getTitle() != null) {
            existingListing.setTitle(request.getTitle());
        }
        if (request.getBrand() != null) {
            existingListing.setBrand(request.getBrand());
        }
        if (request.getModel() != null) {
            existingListing.setModel(request.getModel());
        }
        if (request.getModelYear() != null) {
            existingListing.setModelYear(request.getModelYear());
        }
        if (request.getPrice() != null) {
            existingListing.setPrice(request.getPrice());
        }
        if (request.getMileage() != null) {
            existingListing.setMileage(request.getMileage());
        }
        
        // Handle location updates - only use locationId
        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> {
                    log.warn("Location lookup failed for ID: {}", request.getLocationId());
                    return new ResourceNotFoundException("Location", "id", request.getLocationId());
                });
            existingListing.setLocation(location);
        }
        
        if (request.getDescription() != null) {
            existingListing.setDescription(request.getDescription());
        }
        if (request.getTransmission() != null) {
            existingListing.setTransmission(request.getTransmission());
        }

        // Update isSold and isArchived if provided in the request
        if (request.getIsSold() != null) {
            existingListing.setSold(request.getIsSold());
        }
        if (request.getIsArchived() != null) {
            existingListing.setArchived(request.getIsArchived());
        }
        
        CarListing updatedListing = carListingRepository.save(existingListing);
        log.info("Successfully updated listing ID: {} by user: {}", id, username);

        // Publish events if status changed
        if (updatedListing.getSold() && !originalIsSold) {
            // Determine if admin action based on who is making the call, for now, assume false if called via this method by a user
            boolean isAdminAction = false; // This might need to be determined by user roles or a specific parameter
            eventPublisher.publishEvent(new ListingMarkedAsSoldEvent(this, updatedListing, isAdminAction)); 
            log.info("Published ListingMarkedAsSoldEvent for listing ID: {} (isAdminAction: {})", updatedListing.getId(), isAdminAction);
        }
        if (updatedListing.getArchived() && !originalIsArchived) {
            // Determine if admin action
            boolean isAdminAction = false; // This might need to be determined by user roles or a specific parameter
            eventPublisher.publishEvent(new ListingArchivedEvent(this, updatedListing, isAdminAction)); 
            log.info("Published ListingArchivedEvent for listing ID: {} (isAdminAction: {})", updatedListing.getId(), isAdminAction);
        }
        
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    /**
     * Delete a car listing.
     *
     * @param id         The ID of the car listing to delete
     * @param username   The username of the user making the request
     * @throws ResourceNotFoundException If the listing does not exist
     * @throws SecurityException If the user does not own the listing
     */
    @Transactional
    public void deleteListing(Long id, String username) {
        log.info("Attempting to delete listing with ID: {} by user: {}", id, username);
        
        CarListing existingListing = carListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarListing", "id", id));
        
        // Check if the user owns this listing
        if (!existingListing.getSeller().getUsername().equals(username)) {
            log.warn("User {} attempted to delete listing {} owned by {}", 
                    username, id, existingListing.getSeller().getUsername());
            throw new SecurityException("You are not authorized to delete this listing");
        }
        
        // Delete all media files associated with the listing
        mediaService.deleteAllMediaForListing(existingListing);
        
        // Delete the listing
        carListingRepository.delete(existingListing);
        log.info("Successfully deleted listing with ID: {}", id);
    }
    
    /**
     * Admin-only method to delete any car listing.
     *
     * @param id The ID of the car listing to delete
     * @throws ResourceNotFoundException If the listing does not exist
     */
    @Transactional
    public void deleteListingAsAdmin(Long id) {
        log.info("Admin attempting to delete listing with ID: {}", id);
        
        CarListing existingListing = carListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CarListing", "id", id));
        
        // Delete all media files associated with the listing
        mediaService.deleteAllMediaForListing(existingListing);
        
        // Delete the listing
        carListingRepository.delete(existingListing);
        log.info("Admin successfully deleted listing with ID: {}", id);
    }
    
    /**
     * Creates a CarListing entity from a CreateListingRequest.
     * 
     * @param request The request containing listing data
     * @param user The user who will be the seller of the listing
     * @return A new CarListing entity (not yet persisted)
     */
    private CarListing buildCarListingFromRequest(CreateListingRequest request, User user) {
        CarListing carListing = new CarListing();
        carListing.setTitle(request.getTitle());
        carListing.setBrand(request.getBrand());
        carListing.setModel(request.getModel());
        carListing.setModelYear(request.getModelYear());
        carListing.setPrice(request.getPrice());
        carListing.setMileage(request.getMileage());
        carListing.setDescription(request.getDescription());
        
        // Handle location - only use locationId
        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> {
                    log.warn("Location lookup failed for ID: {}", request.getLocationId());
                    return new ResourceNotFoundException("Location", "id", request.getLocationId());
                });
            carListing.setLocation(location);
        }
        // If request.getLocationId() is null, carListing.location will remain null.
        
        carListing.setSeller(user);
        carListing.setApproved(false); // Default to not approved
        // Set isSold and isArchived from request, defaulting to false if null
        carListing.setSold(request.getIsSold() != null ? request.getIsSold() : false);
        carListing.setArchived(request.getIsArchived() != null ? request.getIsArchived() : false);
        return carListing;
    }
}
