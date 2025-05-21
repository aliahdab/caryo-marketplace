package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.events.ListingApprovedEvent;
import com.autotrader.autotraderbackend.events.ListingArchivedEvent;
import com.autotrader.autotraderbackend.events.ListingMarkedAsSoldEvent;
import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarListingStatusServiceTest {

    @Mock
    private CarListingRepository carListingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarListingMapper carListingMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CarListingStatusService carListingStatusService;
    private User testUser;
    private User otherUser;
    private CarListing savedListing;

    @BeforeEach
    void setUp() {
        carListingStatusService = new CarListingStatusService(
                carListingRepository,
                userRepository,
                carListingMapper,
                eventPublisher
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        savedListing = new CarListing();
        savedListing.setId(1L);
        savedListing.setSeller(testUser);
    }

    // --- Tests for approveListing ---

    @Test
    void approveListing_Success() {
        // Arrange
        Long id = 1L;
        savedListing.setApproved(false);
        
        CarListingResponse expectedResponse = new CarListingResponse();
        expectedResponse.setId(id);
        expectedResponse.setApproved(true);

        when(carListingRepository.findById(id)).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(savedListing);
        when(carListingMapper.toCarListingResponse(savedListing)).thenReturn(expectedResponse);

        // Act
        CarListingResponse response = carListingStatusService.approveListing(id);

        // Assert
        assertNotNull(response);
        assertTrue(response.getApproved());
        verify(carListingRepository).findById(id);
        verify(carListingRepository).save(argThat(listing -> Boolean.TRUE.equals(listing.getApproved())));
        verify(eventPublisher).publishEvent(any(ListingApprovedEvent.class));
        verify(carListingMapper).toCarListingResponse(savedListing);
    }

    @Test
    void approveListing_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistentId = 999L;
        when(carListingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingStatusService.approveListing(nonExistentId);
        });
        assertEquals("CarListing not found with id : '999'", exception.getMessage());
        verify(carListingRepository).findById(nonExistentId);
        verify(carListingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void approveListing_AlreadyApproved_ThrowsIllegalStateException() {
        // Arrange
        Long id = 1L;
        savedListing.setApproved(true); // Already approved
        
        when(carListingRepository.findById(id)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.approveListing(id);
        });
        assertEquals("Listing with ID " + id + " is already approved.", exception.getMessage());
        verify(carListingRepository).findById(id);
        verify(carListingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // --- Tests for pauseListing ---

    @Test
    void pauseListing_Success_ByOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "testuser";
        savedListing.setApproved(true);
        savedListing.setSold(false);
        savedListing.setArchived(false);
        savedListing.setIsUserActive(true);

        CarListing pausedListing = new CarListing();
        pausedListing.setId(listingId);
        pausedListing.setSeller(testUser);
        pausedListing.setApproved(true);
        pausedListing.setSold(false);
        pausedListing.setArchived(false);
        pausedListing.setIsUserActive(false); // Paused

        CarListingResponse pausedResponse = new CarListingResponse();
        pausedResponse.setId(listingId);
        pausedResponse.setIsUserActive(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(pausedListing);
        when(carListingMapper.toCarListingResponse(pausedListing)).thenReturn(pausedResponse);

        // Act
        CarListingResponse response = carListingStatusService.pauseListing(listingId, username);

        // Assert
        assertNotNull(response);
        assertFalse(response.getIsUserActive());
        verify(carListingRepository).save(argThat(listing -> !listing.getIsUserActive()));
        verify(carListingMapper).toCarListingResponse(pausedListing);
    }

    @Test
    void pauseListing_ThrowsSecurityException_IfNotOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "otheruser"; // Not the owner
        savedListing.setSeller(testUser); // Owner is testUser
        savedListing.setApproved(true);
        savedListing.setSold(false);
        savedListing.setArchived(false);
        savedListing.setIsUserActive(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            carListingStatusService.pauseListing(listingId, username);
        });
        assertEquals("User does not have permission to modify this listing.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void pauseListing_ThrowsIllegalStateException_IfAlreadyPaused() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setApproved(true);
        savedListing.setSold(false);
        savedListing.setArchived(false);
        savedListing.setIsUserActive(false); // Already paused

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.pauseListing(listingId, username);
        });
        assertEquals("Listing with ID " + listingId + " is already paused.", exception.getMessage());

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void pauseListing_ThrowsIllegalStateException_IfListingSold() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSeller(testUser);
        savedListing.setApproved(true);
        savedListing.setSold(true); // Sold
        savedListing.setArchived(false);
        savedListing.setIsUserActive(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.pauseListing(listingId, username);
        });
        assertEquals("Cannot pause a listing that has been marked as sold.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void pauseListing_ThrowsIllegalStateException_IfListingArchived() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSeller(testUser);
        savedListing.setApproved(true);
        savedListing.setSold(false);
        savedListing.setArchived(true); // Archived
        savedListing.setIsUserActive(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.pauseListing(listingId, username);
        });
        assertEquals("Cannot pause a listing that has been archived.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    // --- Tests for resumeListing ---

    @Test
    void resumeListing_Success_ByOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "testuser";
        savedListing.setSeller(testUser);
        savedListing.setApproved(true);
        savedListing.setSold(false);
        savedListing.setArchived(false);
        savedListing.setIsUserActive(false); // Paused

        CarListing resumedListing = new CarListing();
        resumedListing.setId(listingId);
        resumedListing.setSeller(testUser);
        resumedListing.setApproved(true);
        resumedListing.setSold(false);
        resumedListing.setArchived(false);
        resumedListing.setIsUserActive(true); // Resumed

        CarListingResponse resumedResponse = new CarListingResponse();
        resumedResponse.setId(listingId);
        resumedResponse.setIsUserActive(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(resumedListing);
        when(carListingMapper.toCarListingResponse(resumedListing)).thenReturn(resumedResponse);

        // Act
        CarListingResponse response = carListingStatusService.resumeListing(listingId, username);

        // Assert
        assertNotNull(response);
        assertTrue(response.getIsUserActive());
        verify(carListingRepository).save(argThat(listing -> listing.getIsUserActive()));
        verify(carListingMapper).toCarListingResponse(resumedListing);
    }

    @Test
    void resumeListing_ThrowsSecurityException_IfNotOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "otheruser";
        savedListing.setSeller(testUser); // Owner is testUser
        savedListing.setApproved(true);
        savedListing.setSold(false);
        savedListing.setArchived(false);
        savedListing.setIsUserActive(false); // Paused

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            carListingStatusService.resumeListing(listingId, username);
        });
        assertEquals("User does not have permission to modify this listing.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void resumeListing_ThrowsIllegalStateException_IfAlreadyActive() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSeller(testUser);
        savedListing.setApproved(true);
        savedListing.setSold(false);
        savedListing.setArchived(false);
        savedListing.setIsUserActive(true); // Already active

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.resumeListing(listingId, username);
        });
        assertEquals("Listing with ID " + listingId + " is already active.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void resumeListing_ThrowsIllegalStateException_IfListingSold() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSeller(testUser);
        savedListing.setApproved(true);
        savedListing.setSold(true); // Sold
        savedListing.setArchived(false);
        savedListing.setIsUserActive(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.resumeListing(listingId, username);
        });
        assertEquals("Cannot resume a listing that has been marked as sold.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void resumeListing_ThrowsIllegalStateException_IfListingArchived() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSeller(testUser);
        savedListing.setApproved(true);
        savedListing.setSold(false);
        savedListing.setArchived(true); // Archived
        savedListing.setIsUserActive(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.resumeListing(listingId, username);
        });
        assertEquals("Cannot resume a listing that has been archived. Please contact support or renew if applicable.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void resumeListing_ThrowsResourceNotFoundException_IfListingNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        String username = testUser.getUsername();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingStatusService.resumeListing(nonExistentId, username);
        });
        assertEquals("CarListing not found with id : '999'", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    // --- Tests for markListingAsSold ---

    @Test
    void markListingAsSold_Success() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSold(false); // Ensure it's not already sold
        savedListing.setArchived(false); // Ensure it's not archived

        CarListingResponse expectedResponse = new CarListingResponse();
        expectedResponse.setId(listingId);
        expectedResponse.setIsSold(true);
        expectedResponse.setIsArchived(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any(CarListing.class))).thenAnswer(invocation -> {
            CarListing listingToSave = invocation.getArgument(0);
            assertTrue(listingToSave.getSold());
            return listingToSave;
        });
        when(carListingMapper.toCarListingResponse(savedListing)).thenReturn(expectedResponse);

        // Act
        CarListingResponse response = carListingStatusService.markListingAsSold(listingId, username);

        // Assert
        assertNotNull(response);
        assertTrue(response.getIsSold());
        assertEquals(expectedResponse, response);
        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository).save(argThat(l -> Boolean.TRUE.equals(l.getSold())));
        verify(carListingMapper).toCarListingResponse(savedListing);
        verify(eventPublisher).publishEvent(any(ListingMarkedAsSoldEvent.class));
    }

    @Test
    void markListingAsSold_ThrowsSecurityException_IfNotOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "otheruser";
        savedListing.setSeller(testUser); // Owner is testUser
        savedListing.setSold(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            carListingStatusService.markListingAsSold(listingId, username);
        });
        assertEquals("User does not have permission to modify this listing.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void markListingAsSold_ThrowsIllegalStateException_IfAlreadySold() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSeller(testUser);
        savedListing.setSold(true); // Already sold

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.markListingAsSold(listingId, username);
        });
        assertEquals("Listing with ID " + listingId + " is already marked as sold.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void markListingAsSold_ThrowsIllegalStateException_IfArchived() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSeller(testUser);
        savedListing.setSold(false);
        savedListing.setArchived(true); // Archived

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.markListingAsSold(listingId, username);
        });
        assertEquals("Cannot mark an archived listing as sold. Please unarchive first.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    // --- Tests for markListingAsSoldByAdmin ---

    @Test
    void markListingAsSoldByAdmin_Success() {
        // Setup
        savedListing.setSold(false); // Ensure not already sold
        savedListing.setArchived(false); // Not archived

        when(carListingRepository.findById(savedListing.getId())).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any())).thenAnswer(invocation -> {
            CarListing savedListing = invocation.getArgument(0);
            assertTrue(savedListing.getSold()); // Verify it's now marked sold
            return savedListing;
        });
        
        // Mock the mapper response
        CarListingResponse mockResponse = new CarListingResponse();
        mockResponse.setId(savedListing.getId());
        mockResponse.setIsSold(true);
        when(carListingMapper.toCarListingResponseForAdmin(any(CarListing.class))).thenReturn(mockResponse);

        // Execute
        CarListingResponse response = carListingStatusService.markListingAsSoldByAdmin(savedListing.getId());

        // Verify
        assertNotNull(response);
        assertTrue(response.getIsSold());
        verify(carListingRepository).findById(savedListing.getId());
        verify(carListingRepository).save(any(CarListing.class));
        verify(carListingMapper).toCarListingResponseForAdmin(any(CarListing.class));
        verify(eventPublisher).publishEvent(any(ListingMarkedAsSoldEvent.class));
    }

    @Test
    void markListingAsSoldByAdmin_ThrowsIllegalStateException_IfAlreadySold() {
        // Arrange
        Long listingId = 1L;
        savedListing.setSold(true); // Already sold

        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.markListingAsSoldByAdmin(listingId);
        });
        assertEquals("Listing with ID " + listingId + " is already marked as sold.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void markListingAsSoldByAdmin_ThrowsIllegalStateException_IfArchived() {
        // Arrange
        Long listingId = 1L;
        savedListing.setSold(false);
        savedListing.setArchived(true); // Archived

        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.markListingAsSoldByAdmin(listingId);
        });
        assertEquals("Cannot mark an archived listing as sold. Please unarchive first.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    // --- Tests for archiveListing ---

    @Test
    void archiveListing_Success() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setArchived(false); // Not archived
        
        CarListingResponse expectedResponse = new CarListingResponse();
        expectedResponse.setId(listingId);
        expectedResponse.setIsArchived(true);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any(CarListing.class))).thenAnswer(invocation -> {
            CarListing listingToSave = invocation.getArgument(0);
            assertTrue(listingToSave.getArchived());
            return listingToSave;
        });
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(expectedResponse);
        
        // Act
        CarListingResponse response = carListingStatusService.archiveListing(listingId, username);
        
        // Assert
        assertNotNull(response);
        assertTrue(response.getIsArchived());
        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository).save(argThat(l -> Boolean.TRUE.equals(l.getArchived())));
        verify(carListingMapper).toCarListingResponse(any(CarListing.class));
        verify(eventPublisher).publishEvent(any(ListingArchivedEvent.class));
    }

    @Test
    void archiveListing_ThrowsSecurityException_IfNotOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "otheruser";
        savedListing.setSeller(testUser); // Owner is testUser
        savedListing.setArchived(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            carListingStatusService.archiveListing(listingId, username);
        });
        assertEquals("User does not have permission to modify this listing.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void archiveListing_ThrowsIllegalStateException_IfAlreadyArchived() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSeller(testUser);
        savedListing.setArchived(true); // Already archived

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.archiveListing(listingId, username);
        });
        assertEquals("Listing with ID " + listingId + " is already archived.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void archiveListingByAdmin_ThrowsIllegalStateException_IfAlreadyArchived() {
        // Arrange
        Long listingId = 1L;
        savedListing.setArchived(true); // Already archived

        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.archiveListingByAdmin(listingId);
        });
        assertEquals("Listing with ID " + listingId + " is already archived.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    // --- Tests for unarchiveListing ---

    @Test
    void unarchiveListing_Success() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setArchived(true); // Currently archived
        
        CarListingResponse expectedResponse = new CarListingResponse();
        expectedResponse.setId(listingId);
        expectedResponse.setIsArchived(false);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any(CarListing.class))).thenAnswer(invocation -> {
            CarListing listingToSave = invocation.getArgument(0);
            assertFalse(listingToSave.getArchived());
            return listingToSave;
        });
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(expectedResponse);
        
        // Act
        CarListingResponse response = carListingStatusService.unarchiveListing(listingId, username);
        
        // Assert
        assertNotNull(response);
        assertFalse(response.getIsArchived());
        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository).save(argThat(l -> !l.getArchived()));
        verify(carListingMapper).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void unarchiveListing_ThrowsSecurityException_IfNotOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "otheruser";
        savedListing.setSeller(testUser); // Owner is testUser
        savedListing.setArchived(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            carListingStatusService.unarchiveListing(listingId, username);
        });
        assertEquals("User does not have permission to modify this listing.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void unarchiveListing_ThrowsIllegalStateException_IfNotArchived() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSeller(testUser);
        savedListing.setArchived(false); // Not archived

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.unarchiveListing(listingId, username);
        });
        assertEquals("Listing with ID " + listingId + " is not archived.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void unarchiveListingByAdmin_ThrowsIllegalStateException_IfNotArchived() {
        // Arrange
        Long listingId = 1L;
        savedListing.setArchived(false); // Not archived

        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.unarchiveListingByAdmin(listingId);
        });
        assertEquals("Listing with ID " + listingId + " is not archived.", exception.getMessage());
        verify(carListingRepository, never()).save(any());
    }

    // More helper and authorization tests

    @Test
    void findUserByUsername_WithValidUsername_ReturnsUser() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        User foundUser = carListingStatusService.findUserByUsername(username);

        // Assert
        assertEquals(testUser, foundUser);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findUserByUsername_WithInvalidUsername_ThrowsResourceNotFoundException() {
        // Arrange
        String invalidUsername = "nonexistent";
        when(userRepository.findByUsername(invalidUsername)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingStatusService.findUserByUsername(invalidUsername);
        });
        assertEquals("User not found with username : 'nonexistent'", exception.getMessage());
        verify(userRepository).findByUsername(invalidUsername);
    }

    @Test
    void findListingById_WithValidId_ReturnsListing() {
        // Arrange
        Long validId = 1L;
        when(carListingRepository.findById(validId)).thenReturn(Optional.of(savedListing));

        // Act
        CarListing foundListing = carListingStatusService.findListingById(validId);

        // Assert
        assertEquals(savedListing, foundListing);
        verify(carListingRepository).findById(validId);
    }

    @Test
    void findListingById_WithInvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(carListingRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingStatusService.findListingById(invalidId);
        });
        assertEquals("CarListing not found with id : '999'", exception.getMessage());
        verify(carListingRepository).findById(invalidId);
    }

    @Test
    void authorizeListingModification_WhenUserIsOwner_DoesNotThrowException() {
        // Arrange
        CarListing listing = new CarListing();
        listing.setId(1L);
        listing.setSeller(testUser);

        User ownerUser = testUser;

        // Act & Assert
        assertDoesNotThrow(() -> {
            carListingStatusService.authorizeListingModification(listing, ownerUser, "test");
        });
    }

    @Test
    void authorizeListingModification_WhenUserIsNotOwner_ThrowsSecurityException() {
        // Arrange
        CarListing listing = new CarListing();
        listing.setId(1L);
        listing.setSeller(testUser);

        User nonOwnerUser = otherUser;

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            carListingStatusService.authorizeListingModification(listing, nonOwnerUser, "test");
        });
        assertEquals("User does not have permission to modify this listing.", exception.getMessage());
    }
<<<<<<< HEAD

    @Test
    void markListingAsSold_ShouldPublishCorrectEvent() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        savedListing.setSold(false);
        savedListing.setArchived(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any())).thenReturn(savedListing);

        // Act
        carListingStatusService.markListingAsSold(listingId, username);

        // Assert
        verify(eventPublisher).publishEvent(argThat(event -> {
            if (!(event instanceof ListingMarkedAsSoldEvent soldEvent)) {
                return false;
            }
            return soldEvent.getListing().equals(savedListing) &&
                   !soldEvent.isAdminAction() &&
                   soldEvent.getSource() == carListingStatusService;
        }));
    }

    @Test
    void markListingAsSoldByAdmin_ShouldPublishCorrectEvent() {
        // Arrange
        Long listingId = 1L;
        savedListing.setSold(false);
        savedListing.setArchived(false);

        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any())).thenReturn(savedListing);

        // Act
        carListingStatusService.markListingAsSoldByAdmin(listingId);

        // Assert
        verify(eventPublisher).publishEvent(argThat(event -> {
            if (!(event instanceof ListingMarkedAsSoldEvent soldEvent)) {
                return false;
            }
            return soldEvent.getListing().equals(savedListing) &&
                   soldEvent.isAdminAction() &&
                   soldEvent.getSource() == carListingStatusService;
        }));
    }
=======
>>>>>>> a7fbdc3 (Add unit tests for CarListingStatusService methods to ensure proper functionality and error handling)
}
