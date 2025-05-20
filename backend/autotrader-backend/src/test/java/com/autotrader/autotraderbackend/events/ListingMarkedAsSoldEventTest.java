package com.autotrader.autotraderbackend.events;

import com.autotrader.autotraderbackend.model.CarListing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ListingMarkedAsSoldEventTest {

    @Mock
    private CarListing mockListing;

    @Test
    void constructor_ShouldSetSourceListingAndAdminFlag() {
        // Arrange
        Object source = new Object();
        boolean isAdminAction = true;

        // Act
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(source, mockListing, isAdminAction);

        // Assert
        assertNotNull(event);
        assertEquals(source, event.getSource());
        assertEquals(mockListing, event.getListing());
        assertTrue(event.isAdminAction());
    }

    @Test
    void getListing_ShouldReturnListing() {
        // Arrange
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(new Object(), mockListing, false);

        // Act
        CarListing listing = event.getListing();

        // Assert
        assertEquals(mockListing, listing);
    }

    @Test
    void isAdminAction_ShouldReturnFalseForUserAction() {
        // Arrange
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(new Object(), mockListing, false);

        // Act & Assert
        assertFalse(event.isAdminAction());
    }

    @Test
    void isAdminAction_ShouldReturnTrueForAdminAction() {
        // Arrange
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(new Object(), mockListing, true);

        // Act & Assert
        assertTrue(event.isAdminAction());
    }
}
