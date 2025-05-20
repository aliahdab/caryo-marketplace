package com.autotrader.autotraderbackend.events;

import com.autotrader.autotraderbackend.model.CarListing;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when a car listing is marked as sold.
 * This can happen either by the seller or by an admin.
 */
@Getter
public class ListingMarkedAsSoldEvent extends ApplicationEvent {
    private final CarListing listing;
    private final boolean isAdminAction;

    public ListingMarkedAsSoldEvent(Object source, CarListing listing, boolean isAdminAction) {
        super(source);
        this.listing = listing;
        this.isAdminAction = isAdminAction;
    }
}