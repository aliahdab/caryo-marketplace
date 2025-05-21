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
        mockResponse = new CarListingResponse();
        mockResponse.setId(listingId);
        mockResponse.setTitle("Test Car");
    }

    @Test
    void markListingAsSold_Success() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.markListingAsSold(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("Listing marked as sold successfully", body.getMessage());
        assertEquals(mockResponse, body.getData());

        verify(carListingStatusService).markListingAsSold(listingId, "testUser");
    }

    @Test
    void markListingAsSold_NotFound() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.markListingAsSold(listingId, "testUser"))
                .thenThrow(new ResourceNotFoundException("Listing", "id", listingId));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("not found"));
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
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("Listing marked as sold by admin successfully", body.getMessage());
        assertEquals(mockResponse, body.getData());
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
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("not found"));
    }

    @Test
    void markListingAsSoldByAdmin_IllegalState() {
        // Arrange
        when(carListingStatusService.markListingAsSoldByAdmin(listingId))
                .thenThrow(new IllegalStateException("Listing cannot be marked as sold in its current state"));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSoldByAdmin(listingId);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("current state"));
    }

    @Test
    void markListingAsSoldByAdmin_UnexpectedError() {
        // Arrange
        when(carListingStatusService.markListingAsSoldByAdmin(listingId))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSoldByAdmin(listingId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("unexpected error"));
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
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("Listing approved successfully", body.getMessage());
        assertEquals(mockResponse, body.getData());
    }

    @Test
    void approveListing_IllegalState() {
        // Arrange
        when(carListingStatusService.approveListing(listingId))
                .thenThrow(new IllegalStateException("Listing is already approved"));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.approveListing(listingId);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("already approved"));
    }

    @Test
    void pauseListing_Success() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.pauseListing(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.pauseListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("Listing paused successfully", body.getMessage());
        assertEquals(mockResponse, body.getData());
    }

    @Test
    void pauseListing_IllegalState() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.pauseListing(listingId, "testUser"))
                .thenThrow(new IllegalStateException("Listing is already paused"));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.pauseListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("already paused"));
    }

    @Test
    void resumeListing_Success() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.resumeListing(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.resumeListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("Listing resumed successfully", body.getMessage());
        assertEquals(mockResponse, body.getData());
    }

    @Test
    void resumeListing_IllegalState() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.resumeListing(listingId, "testUser"))
                .thenThrow(new IllegalStateException("Listing is not paused"));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.resumeListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("not paused"));
    }

    @Test
    void archiveListing_Success() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.archiveListing(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.archiveListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("Listing archived successfully", body.getMessage());
        assertEquals(mockResponse, body.getData());
    }

    @Test
    void unarchiveListing_Success() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.unarchiveListing(listingId, "testUser"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.unarchiveListing(listingId, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("Listing unarchived successfully", body.getMessage());
        assertEquals(mockResponse, body.getData());
    }

    // Error scenarios
    
    @Test
    void markListingAsSold_AccessDenied() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.markListingAsSold(listingId, "testUser"))
                .thenThrow(new AccessDeniedException("User does not have permission"));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("permission"));
    }

    @Test
    void markListingAsSold_ResourceNotFound() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.markListingAsSold(listingId, "testUser"))
                .thenThrow(new ResourceNotFoundException("CarListing", "id", listingId));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("CarListing not found with id : '1'"));
    }

    @Test
    void markListingAsSold_UnexpectedError() {
        // Arrange
        when(principal.getName()).thenReturn("testUser");
        when(carListingStatusService.markListingAsSold(listingId, "testUser"))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<ApiResponse<CarListingResponse>> response = 
                listingStatusController.markListingAsSold(listingId, principal);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse<CarListingResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("unexpected error"));
    }
}
