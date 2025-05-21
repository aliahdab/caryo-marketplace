package com.autotrader.autotraderbackend.listeners;

import com.autotrader.autotraderbackend.events.ListingApprovedEvent;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Listener for listing approval events.
 * Handles notification and other business logic when a listing is approved.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ListingApprovedListener {

    private final ListingEventUtils eventUtils;
    
    // TODO: Inject email service when ready
    // private final EmailService emailService;
    
    /**
     * Handle the listing approved event.
     * This will log the event and trigger any notification processes.
     * 
     * @param event The listing approved event (must not be null)
     */
    @EventListener
    @Async
    public void handleListingApproved(@NonNull ListingApprovedEvent event) {
        Objects.requireNonNull(event, "ListingApprovedEvent cannot be null");
        CarListing listing = event.getListing();
        User seller = listing.getSeller();
        
        log.info("Listing approved event received for {}", 
                eventUtils.getListingInfo(listing));

        // Detailed log about the car using Optional for null safety
        log.debug("Approved Listing Details: ID: {}, Seller: {}, Make: {}, Model: {}, Year: {}, Price: {}",
                Objects.toString(listing.getId(), "unknown"),
                Optional.ofNullable(seller).map(User::getUsername).orElse("N/A"),
                Objects.toString(listing.getBrand(), "N/A"),
                Objects.toString(listing.getModel(), "N/A"),
                Optional.ofNullable(listing.getModelYear()).map(String::valueOf).orElse("N/A"),
                Optional.ofNullable(listing.getPrice()).map(String::valueOf).orElse("N/A")
        );

        // TODO: Send email notification to the seller
        // if (seller != null && seller.getEmail() != null) {
        //     emailService.sendListingApprovedEmail(seller.getEmail(), listing);
        // }

        // TODO: Update analytics or reporting
    }
}
