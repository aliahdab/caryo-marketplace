package com.autotrader.autotraderbackend.integration;

import com.autotrader.autotraderbackend.events.ListingArchivedEvent;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import com.autotrader.autotraderbackend.service.CarListingStatusService;
import com.autotrader.autotraderbackend.service.CarListingService;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingArchivedEventIntegrationTest extends BaseListingEventIntegrationTest {

    @Override
    @BeforeEach
    public void setUpBase() {
        super.setUpBase();
        
        // Additional setup specific to ArchivedEvent tests
        mockListing.setArchived(false);
        
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenAnswer(i -> {
            CarListing listing = i.getArgument(0);
            CarListingResponse response = new CarListingResponse();
            response.setId(listing.getId());
            response.setIsArchived(listing.getArchived());
            return response;
        });
    }

    @Test
    public void testListingArchivedEventPublished() {
        // Arrange
        doNothing().when(eventPublisher).publishEvent(any(ListingArchivedEvent.class));

        // Act
        carListingStatusService.archiveListing(mockListing.getId(), "testuser");

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ListingArchivedEvent.class));
    }
}
