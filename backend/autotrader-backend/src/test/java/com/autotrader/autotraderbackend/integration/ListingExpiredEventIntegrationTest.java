package com.autotrader.autotraderbackend.integration;

import com.autotrader.autotraderbackend.events.ListingExpiredEvent;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.model.CarListing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingExpiredEventIntegrationTest extends BaseListingEventIntegrationTest {

    @Override
    @BeforeEach
    public void setUpBase() {
        super.setUpBase();
        
        // Additional setup specific to ExpiredEvent tests
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenAnswer(i -> {
            CarListing listing = i.getArgument(0);
            CarListingResponse response = new CarListingResponse();
            response.setId(listing.getId());
            response.setIsExpired(true); // Set the expired flag for testing
            return response;
        });
    }

    @Test
    public void testListingExpiredEventPublished() {
        // Arrange
        doNothing().when(eventPublisher).publishEvent(any(ListingExpiredEvent.class));

        // Act
        carListingStatusService.expireListing(mockListing.getId());

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ListingExpiredEvent.class));
    }
}
