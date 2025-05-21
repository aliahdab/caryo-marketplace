package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.exception.StorageException;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.ListingMedia;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * Service responsible for managing media files related to car listings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CarListingMediaService {

    private final StorageService storageService;
    private final CarListingRepository carListingRepository;
    private final CarListingServiceHelper serviceHelper;

    @Transactional
    public String uploadListingImage(Long listingId, MultipartFile file, String username) {
        log.info("Uploading image for listing ID: {} by user: {}", listingId, username);

        User user = serviceHelper.findUserByUsername(username);
        validateFile(file, listingId);

        CarListing listing = serviceHelper.findListingById(listingId);
        serviceHelper.authorizeListingModification(listing, user, "upload image for");

        String imageKey = generateImageKey(listingId, file.getOriginalFilename());

        try {
            storageService.store(file, imageKey);
            ListingMedia media = buildListingMedia(file, imageKey, listing, true, 0);
            listing.addMedia(media);
            carListingRepository.save(listing);

            log.info("Uploaded image key '{}' for listing ID: {}", imageKey, listingId);
            return imageKey;
        } catch (Exception e) {
            log.error("Error uploading image for listing ID {}: {}", listingId, e.getMessage(), e);
            throw new StorageException("Image upload failed.", e);
        }
    }

    @Transactional
    public CarListing addImageToListing(CarListing listing, MultipartFile image) {
        validateFile(image, listing.getId());

        String imageKey = generateImageKey(listing.getId(), image.getOriginalFilename());
        storageService.store(image, imageKey);

        ListingMedia media = buildListingMedia(image, imageKey, listing, true, 0);
        listing.addMedia(media);
        return carListingRepository.save(listing);
    }

    public void deleteAllMediaForListing(CarListing listing) {
        if (Objects.nonNull(listing.getMedia()) && !listing.getMedia().isEmpty()) {
            listing.getMedia().forEach(media -> {
                try {
                    storageService.delete(media.getFileKey());
                    log.info("Deleted media key: {} for listing ID: {}", media.getFileKey(), listing.getId());
                } catch (StorageException e) {
                    log.error("Failed to delete media key: {} for listing ID: {}", media.getFileKey(), listing.getId(), e);
                }
            });
        }
    }

    private void validateFile(MultipartFile file, Long listingId) {
        if (Objects.isNull(file) || file.isEmpty()) {
            log.warn("Null or empty file for listing ID: {}", listingId);
            throw new StorageException("Provided file is null or empty.");
        }
    }

    private String generateImageKey(Long listingId, String originalFilename) {
        String safeFilename = Objects.nonNull(originalFilename)
                ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "image";
        return String.format("listings/%d/%d_%s", listingId, System.currentTimeMillis(), safeFilename);
    }

    private ListingMedia buildListingMedia(MultipartFile file, String fileKey, CarListing listing, boolean isPrimary, int sortOrder) {
        ListingMedia media = new ListingMedia();
        media.setCarListing(listing);
        media.setFileKey(fileKey);
        media.setFileName(file.getOriginalFilename());
        media.setContentType(file.getContentType());
        media.setSize(file.getSize());
        media.setSortOrder(sortOrder);
        media.setIsPrimary(isPrimary);
        media.setMediaType("image");
        return media;
    }
}

