// SortableCarListingField enum moved to its own file (SortableCarListingField.java)
package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.exception.StorageException;
import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.ListingMedia;
import com.autotrader.autotraderbackend.model.Location;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.payload.request.CreateListingRequest;
import com.autotrader.autotraderbackend.payload.request.ListingFilterRequest;
import com.autotrader.autotraderbackend.payload.request.UpdateListingRequest;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.LocationRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import com.autotrader.autotraderbackend.repository.specification.CarListingSpecification;
import com.autotrader.autotraderbackend.service.storage.StorageService;
import com.autotrader.autotraderbackend.events.ListingApprovedEvent; // Re-added import
import com.autotrader.autotraderbackend.events.ListingArchivedEvent;
import com.autotrader.autotraderbackend.events.ListingMarkedAsSoldEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarListingService {

    private final CarListingRepository carListingRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StorageService storageService;
    private final CarListingMapper carListingMapper;
    private final ApplicationEventPublisher eventPublisher; 

    /**
     * Pauses a car listing (sets isUserActive to false).
     *
     * @param listingId The ID of the car listing to pause.
     * @param username  The username of the user making the request.
     * @return The updated CarListingResponse.
     */
    @Transactional
    public CarListingResponse pauseListing(Long listingId, String username) {
        log.info("User {} attempting to pause listing ID {}", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "pause");

        if (!listing.getApproved()) {
            log.warn("User {} attempted to pause unapproved listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot pause a listing that is not yet approved.");
        }
        if (listing.getSold()) {
            log.warn("User {} attempted to pause sold listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot pause a listing that has been marked as sold.");
        }
        if (listing.getArchived()) {
            log.warn("User {} attempted to pause archived listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot pause a listing that has been archived.");
        }
        if (!listing.getIsUserActive()) {
            log.warn("Listing ID {} is already paused by user {}. Throwing IllegalStateException.", listingId, username);
            // throw new IllegalStateException("Listing with ID " + listingId + " is already paused."); // Old behavior was to return
            throw new IllegalStateException("Listing with ID " + listingId + " is already paused.");
        }

        listing.setIsUserActive(false);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Successfully paused listing ID {} by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    /**
     * Resumes a car listing (sets isUserActive to true).
     *
     * @param listingId The ID of the car listing to resume.
     * @param username  The username of the user making the request.
     * @return The updated CarListingResponse.
     */
    @Transactional
    public CarListingResponse resumeListing(Long listingId, String username) {
        log.info("User {} attempting to resume listing ID {}", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "resume");

        // Add similar checks as in pauseListing if needed, e.g., cannot resume if sold/archived by admin
        // For now, we assume if a user can access it to resume, and it\'s theirs, it\'s permissible unless other flags prevent it.
        if (listing.getSold()) {
            log.warn("User {} attempted to resume sold listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot resume a listing that has been marked as sold.");
        }
        if (listing.getArchived()) {
            // This check might be more nuanced. If a listing auto-expired and was archived,
            // resuming might mean it needs to go through a renewal process.
            // For now, a simple block if archived.
            log.warn("User {} attempted to resume archived listing ID {}", username, listingId);
            throw new IllegalStateException("Cannot resume a listing that has been archived. Please contact support or renew if applicable.");
        }
        if (listing.getIsUserActive()) {
            log.warn("Listing ID {} is already active for user {}. Throwing IllegalStateException.", listingId, username);
            // throw new IllegalStateException("Listing with ID " + listingId + " is already active."); // Old behavior was to return
            throw new IllegalStateException("Listing with ID " + listingId + " is already active.");
        }

        listing.setIsUserActive(true);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Successfully resumed listing ID {} by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    /**
     * Create a new car listing.
     */
    @Transactional
    public CarListingResponse createListing(CreateListingRequest request, MultipartFile image, String username) {
        log.info("Attempting to create new listing for user: {}", username);
        User user = findUserByUsername(username);

        CarListing carListing = buildCarListingFromRequest(request, user);
        // isSold and isArchived are set within buildCarListingFromRequest

        CarListing savedListing = carListingRepository.save(carListing);

        // Handle image upload if provided
        if (image != null && !image.isEmpty()) {
            try {
                String imageKey = generateImageKey(savedListing.getId(), image.getOriginalFilename());
                storageService.store(image, imageKey);
                
                // Create and add ListingMedia for this image
                ListingMedia media = new ListingMedia();
                media.setCarListing(savedListing);
                media.setFileKey(imageKey);
                media.setFileName(image.getOriginalFilename());
                media.setContentType(image.getContentType());
                media.setSize(image.getSize());
                media.setSortOrder(0);
                media.setIsPrimary(true);
                media.setMediaType("image");
                savedListing.addMedia(media);
                
                savedListing = carListingRepository.save(savedListing); // Save again to update with media
                log.info("Successfully uploaded image for new listing ID: {}", savedListing.getId());
            } catch (StorageException e) {
                // If image upload/update fails, log it but proceed with listing creation response
                log.error("Failed to upload image or update listing with image key for listing ID {}: {}", savedListing.getId(), e.getMessage(), e);
            } catch (Exception e) {
                // Catch unexpected errors during image handling
                log.error("Unexpected error during image handling for listing ID {}: {}", savedListing.getId(), e.getMessage(), e);
            }
        }

        log.info("Successfully created new listing with ID: {} for user: {}", savedListing.getId(), username);
        return carListingMapper.toCarListingResponse(savedListing);
    }

    /**
     * Upload an image for a car listing.
     */
    @Transactional
    public String uploadListingImage(Long listingId, MultipartFile file, String username) {
        log.info("Attempting to upload image for listing ID: {} by user: {}", listingId, username);
        User user = findUserByUsername(username);

        validateFile(file, listingId);

        CarListing listing = findListingById(listingId);

        authorizeListingModification(listing, user, "upload image for");

        String imageKey = generateImageKey(listingId, file.getOriginalFilename());

        try {
            storageService.store(file, imageKey);
            
            // Create a new ListingMedia entity and link it to the car listing
            ListingMedia media = new ListingMedia();
            media.setCarListing(listing);
            media.setFileKey(imageKey);
            media.setFileName(file.getOriginalFilename());
            media.setContentType(file.getContentType());
            media.setSize(file.getSize());
            media.setSortOrder(0);
            media.setIsPrimary(true);
            media.setMediaType("image");
            
            // Add the media to the listing using helper method
            listing.addMedia(media);
            
            carListingRepository.save(listing); // Save the updated listing
            log.info("Successfully uploaded image with key '{}' and updated listing ID: {}", imageKey, listingId);
            return imageKey;
        } catch (StorageException e) {
            log.error("Storage service failed to store image for listing ID {}: {}", listingId, e.getMessage(), e);
            throw new StorageException("Failed to store image file.", e);
        } catch (Exception e) {
            log.error("Unexpected error saving listing {} after image upload: {}", listingId, e.getMessage(), e);
            throw new RuntimeException("Failed to update listing after image upload.", e);
        }
    }

    /**
     * Get car listing details by ID. Only returns approved listings.
     */
    @Transactional(readOnly = true)
    public CarListingResponse getListingById(Long id) {
        log.debug("Fetching approved listing details for ID: {}", id);
        // Use findByIdAndApprovedTrue to ensure only approved listings are returned publicly
        CarListing carListing = carListingRepository.findByIdAndApprovedTrue(id)
                .orElseThrow(() -> {
                    log.warn("Approved CarListing lookup failed for ID: {}", id);
                    return new ResourceNotFoundException("CarListing", "id", id);
                });
        return carListingMapper.toCarListingResponse(carListing);
    }

    /**
     * Get all approved listings with pagination.
     * By default, this excludes listings that are sold or archived.
     */
    @Transactional(readOnly = true)
    public Page<CarListingResponse> getAllApprovedListings(Pageable pageable) {
        log.debug("Fetching approved, not sold, and not archived listings page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Specification<CarListing> spec = Specification.where(CarListingSpecification.isApproved())
                                                     .and(CarListingSpecification.isNotSold())
                                                     .and(CarListingSpecification.isNotArchived())
                                                     .and(CarListingSpecification.isUserActive()); // Added isUserActive
                                                     
        Page<CarListing> listingPage = carListingRepository.findAll(spec, pageable);
        log.info("Found {} approved, not sold, not archived listings on page {}", listingPage.getNumberOfElements(), pageable.getPageNumber());
        return listingPage.map(carListingMapper::toCarListingResponse);
    }

    /**
     * Get filtered and approved listings based on criteria.
     * If isSold is not specified in filterRequest, defaults to false (not sold).
     * If isArchived is not specified in filterRequest, defaults to false (not archived).
     */
    @Transactional(readOnly = true)
    public Page<CarListingResponse> getFilteredListings(ListingFilterRequest filterRequest, Pageable pageable) {
        log.debug("Fetching filtered listings with filter: {}, page: {}, size: {}",
                  filterRequest, pageable.getPageNumber(), pageable.getPageSize());

        // --- SORT FIELD VALIDATION ---
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                String property = order.getProperty();
                // If the property is a compound (e.g. "price,desc"), split and take the field
                String[] sortParts = property.split(",");
                String requestedField = sortParts[0];
                if (!SortableCarListingField.isAllowed(requestedField)) {
                    log.warn("Attempt to sort by non-whitelisted field: '{}'. Ignoring sort for this field.", requestedField);
                    throw new IllegalArgumentException("Sorting by field '" + requestedField + "' is not allowed.");
                }
            });
        }

        Specification<CarListing> spec;
        Location locationToFilterBy = null;
        boolean locationFilterAttempted = false;
        String locationFilterType = "none"; // For logging

        if (filterRequest.getLocationId() != null) {
            locationFilterAttempted = true;
            locationFilterType = "ID: " + filterRequest.getLocationId();
            Optional<Location> locationOpt = locationRepository.findById(filterRequest.getLocationId());
            if (locationOpt.isPresent()) {
                locationToFilterBy = locationOpt.get();
                log.info("Location found by ID: {}. Applying filter.", filterRequest.getLocationId());
            } else {
                log.warn("Location ID {} provided in filter but not found. No listings will match this location criterion.", filterRequest.getLocationId());
                // locationToFilterBy remains null, spec will be set to disjunction
            }
        } else if (StringUtils.hasText(filterRequest.getLocation())) {
            locationFilterAttempted = true;
            locationFilterType = "slug: '" + filterRequest.getLocation() + "'";
            Optional<Location> locationOpt = locationRepository.findBySlug(filterRequest.getLocation());
            if (locationOpt.isPresent()) {
                locationToFilterBy = locationOpt.get();
                log.info("Location found by slug: '{}'. Applying filter.", filterRequest.getLocation());
            } else {
                log.warn("Location slug '{}' provided in filter but not found. No listings will match this location criterion.", filterRequest.getLocation());
                // locationToFilterBy remains null, spec will be set to disjunction
            }
        }

        if (locationFilterAttempted && locationToFilterBy == null) {
            // A location filter was specified (ID or slug) but the location was not found.
            // We should return an empty page result directly rather than using JPA filtering
            log.info("Location filter ({}) resulted in no valid location. Returning empty page result.", locationFilterType);
            
            // Return empty page immediately
            Page<CarListing> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            log.info("Empty page returned for invalid location filter");
            return emptyPage.map(carListingMapper::toCarListingResponse);
        } else {
            // Either no location filter was specified, or a valid location was found.
            // Pass locationToFilterBy (which is null if no filter applied, or a valid Location object if found)
            // CarListingSpecification.fromFilter handles a null locationToFilterBy gracefully (no location predicate added).
            spec = CarListingSpecification.fromFilter(filterRequest, locationToFilterBy);
            if (locationToFilterBy != null) {
                log.info("Applying location filter for {}.", locationFilterType);
            } else if (!locationFilterAttempted) {
                log.info("No location ID or slug provided in filter. Proceeding without specific location entity filter.");
            }
        }

        // Always combine with the 'approved' status filter
        spec = spec.and(CarListingSpecification.isApproved());
        // Also filter by user active status
        spec = spec.and(CarListingSpecification.isUserActive());

        // Apply isSold and isArchived filters
        // If not specified in the request, default to showing NOT sold and NOT archived listings.
        if (filterRequest.getIsSold() == null) {
            spec = spec.and(CarListingSpecification.isNotSold());
            log.debug("Defaulting filter to isSold=false as it was not specified.");
        }
        // If isSold IS specified, the CarListingSpecification.fromFilter will have already added it.

        if (filterRequest.getIsArchived() == null) {
            spec = spec.and(CarListingSpecification.isNotArchived());
            log.debug("Defaulting filter to isArchived=false as it was not specified.");
        }
        // If isArchived IS specified, the CarListingSpecification.fromFilter will have already added it.


        Page<CarListing> listingPage = carListingRepository.findAll(spec, pageable);
        log.info("Found {} filtered listings matching criteria on page {} (Location filter used: {})",
                 listingPage.getNumberOfElements(), pageable.getPageNumber(), locationFilterType);
        return listingPage.map(carListingMapper::toCarListingResponse);
    }

    /**
     * Get all listings (approved or not) for the specified user.
     * This method does NOT automatically filter by isSold or isArchived,
     * allowing users to see all their listings regardless of state.
     */
    @Transactional(readOnly = true)
    public List<CarListingResponse> getMyListings(String username) {
        log.debug("Fetching all listings for user: {}", username);
        User user = findUserByUsername(username);
        List<CarListing> listings = carListingRepository.findBySeller(user);
        log.info("Found {} listings for user: {}", listings.size(), username);
        return listings.stream()
                .map(carListingMapper::toCarListingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approve a car listing.
     */
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

        // Publish ListingApprovedEvent
        eventPublisher.publishEvent(new ListingApprovedEvent(this, approvedListing)); 
        log.info("Published ListingApprovedEvent for listing ID: {}", approvedListing.getId());

        return carListingMapper.toCarListingResponse(approvedListing);
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
        } else {
            // If locationId is not provided in the request, it implies no change to the location.
            // If the intention is to remove the location, the request should explicitly indicate this,
            // for example, by sending a specific value like -1 or a dedicated flag.
            // Based on current structure, not providing locationId means "keep current location".
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
        
        // If listing has media, delete all media files from storage
        if (existingListing.getMedia() != null && !existingListing.getMedia().isEmpty()) {
            for (ListingMedia media : existingListing.getMedia()) {
                try {
                    storageService.delete(media.getFileKey());
                    log.info("Deleted media with key: {} for listing ID: {}", media.getFileKey(), id);
                } catch (StorageException e) {
                    // Log but continue with listing deletion
                    log.error("Failed to delete media with key: {} for listing ID: {}", media.getFileKey(), id, e);
                }
            }
        }
        
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
        
        // If listing has media, delete all media files from storage
        if (existingListing.getMedia() != null && !existingListing.getMedia().isEmpty()) {
            for (ListingMedia media : existingListing.getMedia()) {
                try {
                    storageService.delete(media.getFileKey());
                    log.info("Admin deleted media with key: {} for listing ID: {}", media.getFileKey(), id);
                } catch (StorageException e) {
                    // Log but continue with listing deletion
                    log.error("Admin failed to delete media with key: {} for listing ID: {}", media.getFileKey(), id, e);
                }
            }
        }
        
        // Delete the listing
        carListingRepository.delete(existingListing);
        log.info("Admin successfully deleted listing with ID: {}", id);
    }

    /**
     * Marks a car listing as sold.
     *
     * @param listingId The ID of the car listing to mark as sold.
     * @param username  The username of the user making the request.
     * @return The updated CarListingResponse.
     * @throws ResourceNotFoundException If the listing does not exist.
     * @throws SecurityException         If the user does not own the listing.
     * @throws IllegalStateException     If the listing is already sold or archived.
     */
    @Transactional
    public CarListingResponse markListingAsSold(Long listingId, String username) {
        log.info("User {} attempting to mark listing ID {} as sold", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "mark as sold");

        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Attempt to mark archived listing ID {} as sold by user {}", listingId, username);
            throw new IllegalStateException("Cannot mark an archived listing as sold. Please unarchive first.");
        }
        if (Boolean.TRUE.equals(listing.getSold())) {
            log.warn("Listing ID {} is already marked as sold. No action taken by user {}.", listingId, username);
            // Optionally, could return current state or throw specific exception
            // For now, let's treat it as a successful no-op if already sold and not archived.
            return carListingMapper.toCarListingResponse(listing);
        }

        listing.setSold(true);
        CarListing updatedListing = carListingRepository.save(listing);
        
        // Publish event
        eventPublisher.publishEvent(new ListingMarkedAsSoldEvent(this, updatedListing, false));
        log.info("Successfully marked listing ID {} as sold by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    /**
     * Marks a car listing as sold (admin-only).
     *
     * @param listingId The ID of the car listing to mark as sold.
     * @return The updated CarListingResponse.
     * @throws ResourceNotFoundException If the listing does not exist.
     * @throws IllegalStateException     If the listing is archived (can't mark archived listings as sold).
     */
    @Transactional
    public CarListingResponse markListingAsSoldByAdmin(Long listingId) {
        log.info("Admin attempting to mark listing ID {} as sold", listingId);
        CarListing listing = carListingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.warn("Admin mark as sold failed: Listing not found with ID: {}", listingId);
                    return new ResourceNotFoundException("Car Listing", "id", listingId.toString());
                });

        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Admin attempt to mark archived listing ID {} as sold", listingId);
            throw new IllegalStateException("Cannot mark an archived listing as sold. Please unarchive first.");
        }

        CarListingResponse response;
        if (Boolean.TRUE.equals(listing.getSold())) {
            log.warn("Listing ID {} is already marked as sold. No action taken by admin.", listingId);
            // Return current state as successful no-op
            response = carListingMapper.toCarListingResponseForAdmin(listing);
        } else {
            listing.setSold(true);
            CarListing updatedListing = carListingRepository.save(listing);
            log.info("Admin successfully marked listing ID {} as sold", listingId);
            response = carListingMapper.toCarListingResponseForAdmin(updatedListing);
        }
        // Defensive: never return null
        if (response == null) {
            log.error("carListingMapper.toCarListingResponseForAdmin returned null for listing ID {}. Returning minimal response.", listingId);
            response = new CarListingResponse();
            response.setId(listing.getId());
            response.setIsSold(listing.getSold());
            response.setIsArchived(listing.getArchived());
        }
        return response;
    }

    /**
     * Archives a car listing.
     *
     * @param listingId The ID of the car listing to archive.
     * @param username  The username of the user making the request.
     * @return The updated CarListingResponse.
     * @throws ResourceNotFoundException If the listing does not exist.
     * @throws SecurityException         If the user does not own the listing.
     * @throws IllegalStateException     If the listing is already archived.
     */
    @Transactional
    public CarListingResponse archiveListing(Long listingId, String username) {
        log.info("User {} attempting to archive listing ID {}", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "archive");

        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Listing ID {} is already archived. No action taken by user {}.", listingId, username);
            return carListingMapper.toCarListingResponse(listing); // Idempotent
        }

        listing.setArchived(true);
        // Optionally, consider if archiving should also mark it as "not sold" if it was sold.
        // For now, archiving is independent of the sold status.
        CarListing updatedListing = carListingRepository.save(listing);
        
        // Publish the event for successful archival
        eventPublisher.publishEvent(new ListingArchivedEvent(this, updatedListing, false));
        log.info("Published ListingArchivedEvent for listing ID: {}", updatedListing.getId());
        
        log.info("Successfully archived listing ID {} by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    /**
     * Archives a car listing (admin-only).
     *
     * @param listingId The ID of the car listing to archive.
     * @return The updated CarListingResponse.
     * @throws ResourceNotFoundException If the listing does not exist.
     * @throws IllegalStateException     If the listing is already archived.
     */
    @Transactional
    public CarListingResponse archiveListingByAdmin(Long listingId) {
        log.info("Admin attempting to archive listing ID {}", listingId);
        CarListing listing = carListingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.warn("Admin archive failed: Listing not found with ID: {}", listingId);
                    return new ResourceNotFoundException("Car Listing", "id", listingId.toString());
                });

        if (Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Listing ID {} is already archived. No action taken by admin.", listingId);
            return carListingMapper.toCarListingResponse(listing); // Idempotent
        }

        listing.setArchived(true);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Admin successfully archived listing ID {}", listingId);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    /**
     * Unarchives a car listing.
     *
     * @param listingId The ID of the car listing to unarchive.
     * @param username  The username of the user making the request.
     * @return The updated CarListingResponse.
     * @throws ResourceNotFoundException If the listing does not exist.
     * @throws SecurityException         If the user does not own the listing.
     * @throws IllegalStateException     If the listing is not currently archived.
     */
    @Transactional
    public CarListingResponse unarchiveListing(Long listingId, String username) {
        log.info("User {} attempting to unarchive listing ID {}", username, listingId);
        CarListing listing = findListingByIdAndAuthorize(listingId, username, "unarchive");

        if (!Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Listing ID {} is not archived. No action taken for unarchive by user {}.", listingId, username);
            throw new IllegalStateException("Listing with ID " + listingId + " is not currently archived.");
        }

        listing.setArchived(false);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Successfully unarchived listing ID {} by user {}", listingId, username);
        return carListingMapper.toCarListingResponse(updatedListing);
    }

    /**
     * Unarchives a car listing (admin-only).
     *
     * @param listingId The ID of the car listing to unarchive.
     * @return The updated CarListingResponse.
     * @throws ResourceNotFoundException If the listing does not exist.
     * @throws IllegalStateException     If the listing is not currently archived.
     */
    @Transactional
    public CarListingResponse unarchiveListingByAdmin(Long listingId) {
        log.info("Admin attempting to unarchive listing ID {}", listingId);
        CarListing listing = carListingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.warn("Admin unarchive failed: Listing not found with ID: {}", listingId);
                    return new ResourceNotFoundException("Car Listing", "id", listingId.toString());
                });

        if (!Boolean.TRUE.equals(listing.getArchived())) {
            log.warn("Listing ID {} is not archived. No action taken for unarchive by admin.", listingId);
            throw new IllegalStateException("Listing with ID " + listingId + " is not currently archived.");
        }

        listing.setArchived(false);
        CarListing updatedListing = carListingRepository.save(listing);
        log.info("Admin successfully unarchived listing ID {}", listingId);
        return carListingMapper.toCarListingResponse(updatedListing);
    }
    
    // --- Helper Methods ---

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User lookup failed for username: {}", username);
                    return new ResourceNotFoundException("User", "username", username);
                });
    }

    private CarListing findListingById(Long listingId) {
        return carListingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.warn("CarListing lookup failed for ID: {}", listingId);
                    return new ResourceNotFoundException("CarListing", "id", listingId);
                });
    }

    private void validateFile(MultipartFile file, Long listingId) {
        if (file == null || file.isEmpty()) {
            log.warn("Attempt to upload null or empty file for listing ID: {}", listingId);
            throw new StorageException("File provided for upload is null or empty.");
        }
        // Add other validations if needed (e.g., file type, size)
    }

    private void authorizeListingModification(CarListing listing, User user, String action) {
        if (listing.getSeller() == null || !listing.getSeller().getId().equals(user.getId())) {
            log.warn("Authorization failed: User '{}' (ID: {}) attempted to {} listing ID {} owned by '{}' (ID: {})",
                     user.getUsername(), user.getId(), action, listing.getId(),
                     listing.getSeller() != null ? listing.getSeller().getUsername() : "unknown",
                     listing.getSeller() != null ? listing.getSeller().getId() : "unknown");
            throw new SecurityException("User does not have permission to modify this listing.");
        }
    }

    private CarListing findListingByIdAndAuthorize(Long listingId, String username, String action) {
        User user = findUserByUsername(username);
        CarListing listing = findListingById(listingId);
        authorizeListingModification(listing, user, action);
        return listing;
    }

    private String generateImageKey(Long listingId, String originalFilename) {
        // Clean the original filename to prevent path traversal or invalid characters
        String safeFilename = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "image";
        return String.format("listings/%d/%d_%s", listingId, System.currentTimeMillis(), safeFilename);
    }

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