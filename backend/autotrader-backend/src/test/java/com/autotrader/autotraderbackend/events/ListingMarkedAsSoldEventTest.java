package com.autotrader.autotraderbackend.events;

import com.autotrader.autotraderbackend.model.CarListing;
<<<<<<< HEAD
import com.autotrader.autotraderbackend.model.User;
import org.junit.jupiter.api.BeforeEach;
=======
>>>>>>> 299f8c8 (feat: Add event classes for listing expiration, marking as sold, and renewal initiation with corresponding tests)
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
<<<<<<< HEAD
import static org.mockito.Mockito.when;
=======
>>>>>>> 299f8c8 (feat: Add event classes for listing expiration, marking as sold, and renewal initiation with corresponding tests)

@ExtendWith(MockitoExtension.class)
class ListingMarkedAsSoldEventTest {

<<<<<<< HEAD
    @Mock private CarListing mockListing;
    @Mock private User mockSeller;
    
    private Object source;
    private static final Long LISTING_ID = 1L;
    private static final String SELLER_USERNAME = "testSeller";

    @BeforeEach
    void setUp() {
        source = new Object();
    }

    @Test
    void constructor_ShouldSetProperties_WhenUserAction() {
        // Act
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(source, mockListing, false);

        // Assert
        assertAll(
            () -> assertNotNull(event),
            () -> assertEquals(source, event.getSource()),
            () -> assertEquals(mockListing, event.getListing()),
            () -> assertFalse(event.isAdminAction())
        );
    }

    @Test
    void constructor_ShouldSetProperties_WhenAdminAction() {
        // Act
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(source, mockListing, true);

        // Assert
        assertAll(
            () -> assertNotNull(event),
            () -> assertEquals(source, event.getSource()),
            () -> assertEquals(mockListing, event.getListing()),
            () -> assertTrue(event.isAdminAction())
        );
    }

    @Test
    void constructor_ShouldThrowIllegalArgumentException_WhenListingIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> new ListingMarkedAsSoldEvent(source, null, false));
        assertEquals("CarListing cannot be null", exception.getMessage());
    }

    @Test
    void constructor_ShouldThrowIllegalArgumentException_WhenSourceIsNull() {
        // Act & Assert
        // Note: Spring ApplicationEvent itself throws this for a null source.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new ListingMarkedAsSoldEvent(null, mockListing, false));
        assertEquals("null source", exception.getMessage());
    }

    @Test
    void toString_ShouldReturnFormattedString_WithAllValues() {
        // Arrange
        when(mockListing.getId()).thenReturn(LISTING_ID);
        when(mockListing.getSeller()).thenReturn(mockSeller);
        when(mockSeller.getUsername()).thenReturn(SELLER_USERNAME);

        // Act
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(source, mockListing, false);

        // Assert
        assertEquals(
            String.format("ListingMarkedAsSoldEvent[listingId=%d, isAdminAction=%s, seller=%s]",
                LISTING_ID, false, SELLER_USERNAME),
            event.toString()
        );
    }

    @Test
    void toString_ShouldHandleNullSeller() {
        // Arrange
        when(mockListing.getId()).thenReturn(LISTING_ID);
        when(mockListing.getSeller()).thenReturn(null);

        // Act
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(source, mockListing, true);

        // Assert
        assertEquals(
            String.format("ListingMarkedAsSoldEvent[listingId=%d, isAdminAction=%s, seller=%s]",
                LISTING_ID, true, "unknown"),
            event.toString()
        );
    }

    @Test
    void toString_ShouldHandleNullListingId() {
        // Arrange
        when(mockListing.getId()).thenReturn(null);
        when(mockListing.getSeller()).thenReturn(mockSeller);
        when(mockSeller.getUsername()).thenReturn(SELLER_USERNAME);

        // Act
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(source, mockListing, false);

        // Assert
        assertEquals(
            String.format("ListingMarkedAsSoldEvent[listingId=%s, isAdminAction=%s, seller=%s]",
                "null", false, SELLER_USERNAME),
            event.toString()
        );
    }

    @Test
    void toString_ShouldHandleNullListingIdAndNullSeller() {
        // Arrange
        when(mockListing.getId()).thenReturn(null);
        when(mockListing.getSeller()).thenReturn(null);

        // Act
        ListingMarkedAsSoldEvent event = new ListingMarkedAsSoldEvent(source, mockListing, true);

        // Assert
        assertEquals(
            String.format("ListingMarkedAsSoldEvent[listingId=%s, isAdminAction=%s, seller=%s]",
                "null", true, "unknown"),
            event.toString()
        );
=======
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
>>>>>>> 299f8c8 (feat: Add event classes for listing expiration, marking as sold, and renewal initiation with corresponding tests)
    }
}
