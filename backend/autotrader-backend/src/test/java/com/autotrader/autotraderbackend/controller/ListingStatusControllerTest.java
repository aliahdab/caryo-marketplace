package com.autotrader.autotraderbackend.controller;

import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.service.CarListingStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListingStatusControllerTest {

    @Mock
    private CarListingStatusService carListingStatusService;

    @InjectMocks
    private CarListingController carListingController;

    private UserDetails mockUserDetails;
    private Long validListingId;
    private CarListingResponse mockResponse;

    @BeforeEach
    void setUp() {
        validListingId = 1L;
        mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn("testuser");
        
        mockResponse = new CarListingResponse();
        mockResponse.setId(validListingId);
        mockResponse.setTitle("Test Car");
        mockResponse.setIsSold(false);
        mockResponse.setIsArchived(false);
    }

    @Test
    void markListingAsSold_Success() {
        // Arrange
        CarListingResponse soldResponse = new CarListingResponse();
        soldResponse.setId(validListingId);
        soldResponse.setTitle("Test Car");
        soldResponse.setIsSold(true);
        soldResponse.setIsArchived(false);
        
        when(carListingStatusService.markListingAsSold(eq(validListingId), anyString()))
            .thenReturn(soldResponse);
        
        // Act
        ResponseEntity<?> response = carListingController.markListingAsSold(validListingId, mockUserDetails);
        
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody() instanceof CarListingResponse, "Response body should be a CarListingResponse");
        
        CarListingResponse returnedResponse = (CarListingResponse) response.getBody();
        assertNotNull(returnedResponse, "CarListingResponse should not be null");
        assertEquals(validListingId, returnedResponse.getId());
        assertTrue(returnedResponse.getIsSold());
        
        verify(carListingStatusService).markListingAsSold(eq(validListingId), eq("testuser"));
    }
    
    @Test
    void markListingAsSold_NotFound() {
        // Arrange
        Long nonExistentId = 999L;
        when(carListingStatusService.markListingAsSold(eq(nonExistentId), anyString()))
            .thenThrow(new ResourceNotFoundException("CarListing", "id", nonExistentId));
        
        // Act
        ResponseEntity<?> response = carListingController.markListingAsSold(nonExistentId, mockUserDetails);
        
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody() instanceof Map, "Response body should be a Map");
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertNotNull(errorResponse, "Error response should not be null");
        assertTrue(errorResponse.containsKey("message"));
        
        verify(carListingStatusService).markListingAsSold(eq(nonExistentId), eq("testuser"));
    }
    
    @Test
    void markListingAsSold_Forbidden() {
        // Arrange
        String errorMessage = "User does not have permission to modify this listing.";
        when(carListingStatusService.markListingAsSold(eq(validListingId), anyString()))
            .thenThrow(new SecurityException(errorMessage));
        
        // Act
        ResponseEntity<?> response = carListingController.markListingAsSold(validListingId, mockUserDetails);
        
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody() instanceof Map, "Response body should be a Map");
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertNotNull(errorResponse, "Error response should not be null");
        assertTrue(errorResponse.containsKey("message"));
        assertEquals(errorMessage, errorResponse.get("message"));
    }
    
    @Test
    void markListingAsSold_Conflict() {
        // Arrange
        String errorMessage = "Cannot mark an archived listing as sold. Please unarchive first.";
        when(carListingStatusService.markListingAsSold(eq(validListingId), anyString()))
            .thenThrow(new IllegalStateException(errorMessage));
        
        // Act
        ResponseEntity<?> response = carListingController.markListingAsSold(validListingId, mockUserDetails);
        
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody() instanceof Map, "Response body should be a Map");
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertNotNull(errorResponse, "Error response should not be null");
        assertTrue(errorResponse.containsKey("message"));
        assertEquals(errorMessage, errorResponse.get("message"));
    }
    
    @Test
    void archiveListing_Success() {
        // Arrange
        CarListingResponse archivedResponse = new CarListingResponse();
        archivedResponse.setId(validListingId);
        archivedResponse.setTitle("Test Car");
        archivedResponse.setIsSold(false);
        archivedResponse.setIsArchived(true);
        
        when(carListingStatusService.archiveListing(eq(validListingId), anyString()))
            .thenReturn(archivedResponse);
        
        // Act
        ResponseEntity<?> response = carListingController.archiveListing(validListingId, mockUserDetails);
        
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody() instanceof CarListingResponse, "Response body should be a CarListingResponse");
        
        CarListingResponse returnedResponse = (CarListingResponse) response.getBody();
        assertNotNull(returnedResponse, "CarListingResponse should not be null");
        assertEquals(validListingId, returnedResponse.getId());
        assertTrue(returnedResponse.getIsArchived());
        
        verify(carListingStatusService).archiveListing(eq(validListingId), eq("testuser"));
    }
    
    @Test
    void archiveListing_NotFound() {
        // Arrange
        Long nonExistentId = 999L;
        when(carListingStatusService.archiveListing(eq(nonExistentId), anyString()))
            .thenThrow(new ResourceNotFoundException("CarListing", "id", nonExistentId));
        
        // Act
        ResponseEntity<?> response = carListingController.archiveListing(nonExistentId, mockUserDetails);
        
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody() instanceof Map, "Response body should be a Map");
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertNotNull(errorResponse, "Error response should not be null");
        assertTrue(errorResponse.containsKey("message"));
    }
    
    @Test
    void archiveListing_Forbidden() {
        // Arrange
        String errorMessage = "User does not have permission to modify this listing.";
        when(carListingStatusService.archiveListing(eq(validListingId), anyString()))
            .thenThrow(new SecurityException(errorMessage));
        
        // Act
        ResponseEntity<?> response = carListingController.archiveListing(validListingId, mockUserDetails);
        
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody() instanceof Map, "Response body should be a Map");
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertNotNull(errorResponse, "Error response should not be null");
        assertTrue(errorResponse.containsKey("message"));
        assertEquals(errorMessage, errorResponse.get("message"));
    }
    
    @Test
    void unarchiveListing_Success() {
        // Arrange
        CarListingResponse unarchivedResponse = new CarListingResponse();
        unarchivedResponse.setId(validListingId);
        unarchivedResponse.setTitle("Test Car");
        unarchivedResponse.setIsSold(false);
        unarchivedResponse.setIsArchived(false);
        
        when(carListingStatusService.unarchiveListing(eq(validListingId), anyString()))
            .thenReturn(unarchivedResponse);
        
        // Act
        ResponseEntity<?> response = carListingController.unarchiveListing(validListingId, mockUserDetails);
        
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody() instanceof CarListingResponse, "Response body should be a CarListingResponse");
        
        CarListingResponse returnedResponse = (CarListingResponse) response.getBody();
        assertNotNull(returnedResponse, "CarListingResponse should not be null");
        assertEquals(validListingId, returnedResponse.getId());
        assertFalse(returnedResponse.getIsArchived());
        
        verify(carListingStatusService).unarchiveListing(eq(validListingId), eq("testuser"));
    }
    
    @Test
    void unarchiveListing_Conflict() {
        // Arrange
        String errorMessage = "Listing with ID 1 is not currently archived.";
        when(carListingStatusService.unarchiveListing(eq(validListingId), anyString()))
            .thenThrow(new IllegalStateException(errorMessage));
        
        // Act
        ResponseEntity<?> response = carListingController.unarchiveListing(validListingId, mockUserDetails);
        
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody() instanceof Map, "Response body should be a Map");
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertNotNull(errorResponse, "Error response should not be null");
        assertTrue(errorResponse.containsKey("message"));
        assertEquals(errorMessage, errorResponse.get("message"));
    }
}
