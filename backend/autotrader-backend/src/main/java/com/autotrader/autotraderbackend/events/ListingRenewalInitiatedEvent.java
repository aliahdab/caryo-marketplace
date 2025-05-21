package com.autotrader.autotraderbackend.events;

import com.autotrader.autotraderbackend.model.CarListing;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.Objects;

/**
 * Event that is published when a car listing renewal is initiated.
 * This happens when a seller starts the process of renewing an existing listing.
 * Validates that the renewal duration is within acceptable bounds (1-365 days).
 */
@Getter
public class ListingRenewalInitiatedEvent extends ApplicationEvent {
    private static final int MAX_DURATION_DAYS = 365;
    private final CarListing listing;
    private final int durationDays;

    public ListingRenewalInitiatedEvent(Object source, CarListing listing, int durationDays) {
        super(source);
        if (Objects.isNull(source)) {
            throw new IllegalArgumentException("null source");
        }
        if (Objects.isNull(listing)) {
            throw new IllegalArgumentException("CarListing cannot be null");
        }
        if (durationDays <= 0 || durationDays > MAX_DURATION_DAYS) {
            throw new IllegalArgumentException(
                String.format("Duration must be between 1 and %d days", MAX_DURATION_DAYS));
        }
        this.listing = listing;
        this.durationDays = durationDays;
    }

    @Override
    public String toString() {
        return String.format("ListingRenewalInitiatedEvent[listingId=%s, durationDays=%d]",
            Objects.toString(listing.getId(), "null"),
            durationDays);
    }
}