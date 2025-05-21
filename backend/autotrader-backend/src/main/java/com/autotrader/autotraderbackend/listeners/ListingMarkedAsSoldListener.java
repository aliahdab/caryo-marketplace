package com.autotrader.autotraderbackend.listeners;

import com.autotrader.autotraderbackend.events.ListingMarkedAsSoldEvent;
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
 * Listener for when a listing is marked as sold.
 * Handles notification and other business logic when a listing is sold.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ListingMarkedAsSoldListener {

    private final ListingEventUtils eventUtils;
    
    /**
     * Handle the listing marked as sold event.
     * This will log the event and trigger any notification processes.
     * 
     * @param event The listing marked as sold event (must not be null)
     */
    @EventListener
    @Async
    public void handleListingMarkedAsSold(@NonNull ListingMarkedAsSoldEvent event) {
        Objects.requireNonNull(event, "ListingMarkedAsSoldEvent cannot be null");
        
        CarListing listing = event.getListing();
        User seller = listing.getSeller();
        boolean isAdminAction = event.isAdminAction();

        log.info("Listing marked as sold event received for {} by {}", 
                eventUtils.getListingInfo(listing),
                isAdminAction ? "admin" : "seller");

        // Detailed log about the car using Optional for null safety
        log.debug("Sold Listing Details: ID: {}, Seller: {}, Make: {}, Model: {}, Year: {}, Price: {}",
                Objects.toString(listing.getId(), "unknown"),
                Optional.ofNullable(seller).map(User::getUsername).orElse("N/A"),
                Objects.toString(listing.getBrand(), "N/A"),
                Objects.toString(listing.getModel(), "N/A"),
                Optional.ofNullable(listing.getModelYear()).map(String::valueOf).orElse("N/A"),
                Optional.ofNullable(listing.getPrice()).map(String::valueOf).orElse("N/A")
        );

        // TODO: Send confirmation email to seller
        // if (seller != null && seller.getEmail() != null) {
        //     emailService.sendListingSoldEmail(seller.getEmail(), listing);
        // }
        
        // TODO: Send feedback request to seller
    }
}
