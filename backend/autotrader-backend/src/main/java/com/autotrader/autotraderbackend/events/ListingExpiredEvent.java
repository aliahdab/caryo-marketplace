package com.autotrader.autotraderbackend.events;

import com.autotrader.autotraderbackend.model.CarListing;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when a car listing expires.
 * This happens when listing duration has elapsed based on the ad package duration.
 */
@Getter
public class ListingExpiredEvent extends ApplicationEvent {
    private final CarListing listing;

    public ListingExpiredEvent(Object source, CarListing listing) {
        super(source);
        this.listing = listing;
    }
}
