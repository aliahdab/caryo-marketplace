package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import com.autotrader.autotraderbackend.payload.request.CreateListingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

/**
 * Helper class providing utility methods for CarListing-related operations,
 * such as user lookup, listing lookup, and authorization validation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CarListingServiceHelper {

    private final CarListingRepository carListingRepository;
    private final UserRepository userRepository;

    /**
     * Finds a User entity by username.
     *
     * @param username the username of the user
     * @return the found User
     * @throws ResourceNotFoundException if no user is found
     */
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for username: {}", username);
                    return new ResourceNotFoundException("User", "username", username);
                });
    }

    /**
     * Finds a CarListing by its ID.
     *
     * @param listingId the ID of the listing
     * @return the found CarListing
     * @throws ResourceNotFoundException if no listing is found
     */
    public CarListing findListingById(Long listingId) {
        return carListingRepository.findById(listingId)
                .orElseThrow(() -> {
                    log.warn("CarListing not found for ID: {}", listingId);
                    return new ResourceNotFoundException("CarListing", "id", listingId);
                });
    }

    /**
     * Ensures that the given user is authorized to modify the specified listing.
     *
     * @param listing the listing to be modified
     * @param user the user attempting to perform the modification
     * @param action a description of the action (used in logs)
     * @throws SecurityException if the user is not authorized
     */
    public void authorizeListingModification(CarListing listing, User user, String action) {
        if (Objects.isNull(listing.getSeller()) || !Objects.equals(listing.getSeller().getId(), user.getId())) {
            String sellerUsername = Objects.nonNull(listing.getSeller()) ? listing.getSeller().getUsername() : "unknown";
            Long sellerId = Objects.nonNull(listing.getSeller()) ? listing.getSeller().getId() : null;

            log.warn("Unauthorized {} attempt: user '{}' (ID: {}) tried to access listing ID {} owned by '{}' (ID: {})",
                    action, user.getUsername(), user.getId(), listing.getId(), sellerUsername, sellerId);

            throw new SecurityException("User does not have permission to modify this listing.");
        }
    }

    /**
     * Retrieves a listing by ID and authorizes the given user for a specific action.
     *
     * @param listingId the ID of the listing
     * @param username the username of the user attempting access
     * @param action a description of the action being performed
     * @return the authorized CarListing
     * @throws ResourceNotFoundException if the user or listing is not found
     * @throws SecurityException if the user is not authorized
     */
    public CarListing findListingByIdAndAuthorize(Long listingId, String username, String action) {
        User user = findUserByUsername(username);
        CarListing listing = findListingById(listingId);
        authorizeListingModification(listing, user, action);
        return listing;
    }

    /**
     * Validates a multipart file for upload.
     *
     * @param file      The file to validate.
     * @param listingId The ID of the listing for logging purposes.
     * @throws IllegalArgumentException if the file is invalid.
     */
    public void validateFile(MultipartFile file, Long listingId) {
        if (file == null || file.isEmpty()) {
            log.warn("Upload attempt with empty or null file for listing ID: {}.", listingId);
            throw new IllegalArgumentException("File cannot be empty or null.");
        }
        // Add more validation as needed (e.g., file type, size)
        // For example, to check for image content type:
        // if (!file.getContentType().startsWith("image/")) {
        //     log.warn("Invalid file type '{}' for listing ID: {}. Must be an image.", file.getContentType(), listingId);
        //     throw new IllegalArgumentException("Invalid file type. Please upload an image.");
        // }
        log.debug("File validation successful for listing ID: {}", listingId);
    }

    /**
     * Generates a unique key for storing an image in the storage service.
     *
     * @param listingId    The ID of the car listing.
     * @param originalFilename The original filename of the image.
     * @return A unique string key for the image.
     */
    public String generateImageKey(Long listingId, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String key = String.format("listing-%d/image-%s%s", listingId, UUID.randomUUID().toString(), extension);
        log.debug("Generated image key: '{}' for listing ID: {}", key, listingId);
        return key;
    }

    /**
     * Builds a CarListing entity from a CreateListingRequest.
     *
     * @param request The request object containing listing details.
     * @param seller  The User entity who is selling the car.
     * @return A new CarListing entity.
     */
    public CarListing buildCarListingFromRequest(CreateListingRequest request, User seller) {
        CarListing carListing = new CarListing();
        carListing.setSeller(seller);
        carListing.setTitle(request.getTitle());
        carListing.setBrand(request.getBrand());
        carListing.setModel(request.getModel());
        carListing.setModelYear(request.getModelYear());
        carListing.setPrice(request.getPrice());
        carListing.setMileage(request.getMileage());
        carListing.setDescription(request.getDescription());
        // carListing.setTransmission(request.getTransmission()); // Removed as getTransmission() does not exist on CreateListingRequest
        // Assuming Location needs to be fetched or created based on request.getLocationId()
        // This part needs to be handled by the calling service (e.g., CarListingCommandService)
        // as it involves repository access for Location.
        // carListing.setLocation(location); 

        // Default values for new listings
        carListing.setApproved(false); // Listings require approval
        carListing.setSold(request.getIsSold() != null ? request.getIsSold() : false); // Set from request or default to false
        carListing.setArchived(request.getIsArchived() != null ? request.getIsArchived() : false); // Set from request or default to false
        carListing.setIsUserActive(true); // User actively lists it
        // carListing.setViewCount(0); // Removed as setViewCount() does not exist on CarListing

        log.debug("Built CarListing entity for user: {}", seller.getUsername());
        return carListing;
    }
}
