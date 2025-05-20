package com.autotrader.autotraderbackend.controller;

import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.payload.response.ApiResponse;
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
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingStatusControllerTest {

    @Mock
    private CarListingStatusService carListingStatusService;

    @Mock
    private Principal principal;

    @InjectMocks
    private ListingStatusController listingStatusController;

    private CarListingResponse mockResponse;
    private Long listingId;

    @BeforeEach
    void setUp() {
        listingId = 1L;
        when(principal.getName()).thenReturn("testUser");

        mockResponse = new CarListingResponse();
        mockResponse.setId(listingId);
        mockResponse.setTitle("Test Car");
    }

    @Test
    void markListingAsSold_Success() {
        // Arrange
        when(carListingStatusService.markListingAsSold(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Listing marked as sold successfully", response.getBody().getMessage());
        assertEquals(mockResponse, response.getBody().getData());

        verify(carListingStatusService).markListingAsSold(listingId, "testUser");
    }

    @Test
    void markListingAsSold_NotFound() {
        // Arrange
        when(carListingStatusService.markListingAsSold(listingId, "testUser"))
                .thenThrow(new ResourceNotFoundException("Listing", "id", listingId));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("not found"));
    }

    @Test
    void markListingAsSoldByAdmin_Success() {
        // Arrange
        when(carListingStatusService.markListingAsSoldByAdmin(listingId))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSoldByAdmin(listingId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Listing marked as sold by admin successfully", response.getBody().getMessage());
        assertEquals(mockResponse, response.getBody().getData());
    }

    @Test
    void markListingAsSoldByAdmin_NotFound() {
        // Arrange
        when(carListingStatusService.markListingAsSoldByAdmin(listingId))
                .thenThrow(new ResourceNotFoundException("Listing", "id", listingId));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSoldByAdmin(listingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("not found"));
    }

    @Test
    void approveListing_Success() {
        // Arrange
        when(carListingStatusService.approveListing(listingId))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.approveListing(listingId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Listing approved successfully", response.getBody().getMessage());
        assertEquals(mockResponse, response.getBody().getData());
    }

    @Test
    void pauseListing_Success() {
        // Arrange
        when(carListingStatusService.pauseListing(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.pauseListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Listing paused successfully", response.getBody().getMessage());
        assertEquals(mockResponse, response.getBody().getData());
    }

    @Test
    void resumeListing_Success() {
        // Arrange
        when(carListingStatusService.resumeListing(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.resumeListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Listing resumed successfully", response.getBody().getMessage());
        assertEquals(mockResponse, response.getBody().getData());
    }

    @Test
    void archiveListing_Success() {
        // Arrange
        when(carListingStatusService.archiveListing(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.archiveListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Listing archived successfully", response.getBody().getMessage());
        assertEquals(mockResponse, response.getBody().getData());
    }

    @Test
    void unarchiveListing_Success() {
        // Arrange
        when(carListingStatusService.unarchiveListing(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.unarchiveListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Listing unarchived successfully", response.getBody().getMessage());
        assertEquals(mockResponse, response.getBody().getData());
    }

    // Error scenarios
    
    @Test
    void anyOperation_AccessDenied() {
        // Arrange
        when(carListingStatusService.markListingAsSold(anyLong(), anyString()))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("permission"));
    }

    @Test
    void anyOperation_IllegalState() {
        // Arrange
        String errorMessage = "Invalid state transition";
        when(carListingStatusService.markListingAsSold(anyLong(), anyString()))
                .thenThrow(new IllegalStateException(errorMessage));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    @Test
    void anyOperation_UnexpectedError() {
        // Arrange
        when(carListingStatusService.markListingAsSold(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
    }
}
