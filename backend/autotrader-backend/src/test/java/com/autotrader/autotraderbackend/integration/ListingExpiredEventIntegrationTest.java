package com.autotrader.autotraderbackend.integration;

import com.autotrader.autotraderbackend.events.ListingExpiredEvent;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ListingExpiredEventIntegrationTest extends BaseListingEventIntegrationTest {


        // Setup test listing
        mockListing = new CarListing();
        mockListing.setId(1L);
        mockListing.setTitle("Test Listing");
        mockListing.setSeller(mockUser);
        mockListing.setArchived(false);
        // Setup repository mocks
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(carListingRepository.findById(mockListing.getId())).thenReturn(Optional.of(mockListing));
        when(carListingRepository.save(any(CarListing.class))).thenAnswer(i -> i.getArgument(0));
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenAnswer(i -> {
            CarListing listing = i.getArgument(0);
            CarListingResponse response = new CarListingResponse();
            response.setId(listing.getId());
            response.setIsArchived(listing.getArchived());
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
