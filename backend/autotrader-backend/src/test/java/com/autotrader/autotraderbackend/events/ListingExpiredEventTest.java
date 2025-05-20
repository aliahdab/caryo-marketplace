package com.autotrader.autotraderbackend.events;

import com.autotrader.autotraderbackend.model.CarListing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ListingExpiredEventTest {

    @Mock
    private CarListing mockListing;

    @Test
    void constructor_ShouldSetSourceAndListing() {
        // Arrange
        Object source = new Object();

        // Act
        ListingExpiredEvent event = new ListingExpiredEvent(source, mockListing);

        // Assert
        assertNotNull(event);
        assertEquals(source, event.getSource());
        assertEquals(mockListing, event.getListing());
    }

    @Test
    void getListing_ShouldReturnListing() {
        // Arrange
        ListingExpiredEvent event = new ListingExpiredEvent(new Object(), mockListing);

        // Act
        CarListing listing = event.getListing();

        // Assert
        assertEquals(mockListing, listing);
    }
}
