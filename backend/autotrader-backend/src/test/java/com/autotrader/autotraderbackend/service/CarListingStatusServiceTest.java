package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.events.ListingApprovedEvent;
import com.autotrader.autotraderbackend.events.ListingArchivedEvent;
import com.autotrader.autotraderbackend.events.ListingMarkedAsSoldEvent;
import com.autotrader.autotraderbackend.events.ListingExpiredEvent;
import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

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

    @InjectMocks
    private CarListingStatusService carListingStatusService;

    private User testUser;
    private CarListing testListing;
    private CarListingResponse testListingResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testListing = new CarListing();
        testListing.setId(1L);
        testListing.setSeller(testUser);
        testListing.setApproved(true);
        testListing.setSold(false);
        testListing.setArchived(false);
        testListing.setIsUserActive(true);

        testListingResponse = new CarListingResponse();
        testListingResponse.setId(1L);
        testListingResponse.setIsSold(false);
        testListingResponse.setIsArchived(false);

        // Validate setup
        Objects.requireNonNull(testUser.getId(), "Test user ID cannot be null");
        Objects.requireNonNull(testUser.getUsername(), "Test username cannot be null");
        Objects.requireNonNull(testListing.getId(), "Test listing ID cannot be null");
        Objects.requireNonNull(testListing.getSeller(), "Test listing seller cannot be null");
    }

    @Test
    void markListingAsSold_Success() {
        // Arrange
        testListingResponse.setIsSold(true);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        // Act
        CarListingResponse response = carListingStatusService.markListingAsSold(testListing.getId(), testUser.getUsername());

        // Assert
        assertThat(response)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.getId()).isEqualTo(testListing.getId());
                assertThat(r.getIsSold()).isTrue();
            });

        verify(carListingRepository).save(argThat(listing -> 
            Objects.nonNull(listing) &&
            listing.getId().equals(testListing.getId()) &&
            listing.getSold() &&
            !listing.getArchived()
        ));
        verify(eventPublisher).publishEvent(any(ListingMarkedAsSoldEvent.class));
        verify(carListingMapper).toCarListingResponse(testListing);
    }

    @Test
    void markListingAsSold_AlreadySold() {
        testListing.setSold(true);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.markListingAsSold(testListing.getId(), testUser.getUsername());

        verify(carListingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void archiveListing_Success() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.archiveListing(testListing.getId(), testUser.getUsername());

        verify(carListingRepository).save(testListing);
        verify(eventPublisher).publishEvent(any(ListingArchivedEvent.class));
        assertThat(testListing.getArchived()).isTrue();
    }

    @Test
    void unarchiveListing_Success() {
        testListing.setArchived(true);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.unarchiveListing(testListing.getId(), testUser.getUsername());

        verify(carListingRepository).save(testListing);
        assertThat(testListing.getArchived()).isFalse();
    }

    @Test
    void pauseListing_Success() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.pauseListing(testListing.getId(), testUser.getUsername());

        verify(carListingRepository).save(testListing);
        assertThat(testListing.getIsUserActive()).isFalse();
    }

    @Test
    void resumeListing_Success() {
        testListing.setIsUserActive(false);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.resumeListing(testListing.getId(), testUser.getUsername());

        verify(carListingRepository).save(testListing);
        assertThat(testListing.getIsUserActive()).isTrue();
    }

    @Test
    void approveListing_Success() {
        testListing.setApproved(false);
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.approveListing(testListing.getId());

        verify(carListingRepository).save(testListing);
        verify(eventPublisher).publishEvent(any(ListingApprovedEvent.class));
        assertThat(testListing.getApproved()).isTrue();
    }

    @Test
    void markListingAsSoldByAdmin_Success() {
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponseForAdmin(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.markListingAsSoldByAdmin(testListing.getId());

        verify(carListingRepository).save(testListing);
        assertThat(testListing.getSold()).isTrue();
    }

    @Test
    void archiveListingByAdmin_Success() {
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.archiveListingByAdmin(testListing.getId());

        verify(carListingRepository).save(testListing);
        assertThat(testListing.getArchived()).isTrue();
    }

    @Test
    void unarchiveListingByAdmin_Success() {
        testListing.setArchived(true);
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.unarchiveListingByAdmin(testListing.getId());

        verify(carListingRepository).save(testListing);
        assertThat(testListing.getArchived()).isFalse();
    }

    @Test
    void listing_NotFound() {
        // Arrange
        Long nonExistentId = 999L;
        String username = testUser.getUsername();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException soldException = assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.markListingAsSold(nonExistentId, username));
        assertEquals("CarListing not found with id : '999'", soldException.getMessage());

        ResourceNotFoundException archiveException = assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.archiveListing(nonExistentId, username));
        assertEquals("CarListing not found with id : '999'", archiveException.getMessage());

        ResourceNotFoundException unarchiveException = assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.unarchiveListing(nonExistentId, username));
        assertEquals("CarListing not found with id : '999'", unarchiveException.getMessage());

        ResourceNotFoundException pauseException = assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.pauseListing(nonExistentId, username));
        assertEquals("CarListing not found with id : '999'", pauseException.getMessage());

        ResourceNotFoundException resumeException = assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.resumeListing(nonExistentId, username));
        assertEquals("CarListing not found with id : '999'", resumeException.getMessage());

        // Verify
        verify(userRepository, times(5)).findByUsername(username);
        verify(carListingRepository, times(5)).findById(nonExistentId);
        verify(carListingRepository, never()).save(any(CarListing.class));
    }

    @Test
    void unauthorized_WrongUser() {
        // Arrange
        User wrongUser = new User();
        wrongUser.setId(2L);
        wrongUser.setUsername("wronguser");

        when(userRepository.findByUsername(wrongUser.getUsername())).thenReturn(Optional.of(wrongUser));
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));

        // Act & Assert
        SecurityException soldException = assertThrows(SecurityException.class,
                () -> carListingStatusService.markListingAsSold(testListing.getId(), wrongUser.getUsername()));
        assertEquals("User is not authorized to modify this listing", soldException.getMessage());

        SecurityException archiveException = assertThrows(SecurityException.class,
                () -> carListingStatusService.archiveListing(testListing.getId(), wrongUser.getUsername()));
        assertEquals("User is not authorized to modify this listing", archiveException.getMessage());

        SecurityException unarchiveException = assertThrows(SecurityException.class,
                () -> carListingStatusService.unarchiveListing(testListing.getId(), wrongUser.getUsername()));
        assertEquals("User is not authorized to modify this listing", unarchiveException.getMessage());

        SecurityException pauseException = assertThrows(SecurityException.class,
                () -> carListingStatusService.pauseListing(testListing.getId(), wrongUser.getUsername()));
        assertEquals("User is not authorized to modify this listing", pauseException.getMessage());

        SecurityException resumeException = assertThrows(SecurityException.class,
                () -> carListingStatusService.resumeListing(testListing.getId(), wrongUser.getUsername()));
        assertEquals("User is not authorized to modify this listing", resumeException.getMessage());

        // Verify
        verify(userRepository, times(5)).findByUsername(wrongUser.getUsername());
        verify(carListingRepository, times(5)).findById(testListing.getId());
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    // --- Tests for pauseListing ---

    @Test
    void pauseListing_Success_ByOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "testuser";
        testListing.setSeller(testUser);
        testListing.setApproved(true);
        testListing.setSold(false);
        testListing.setArchived(false);
        testListing.setIsUserActive(true);

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
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(pausedListing);
        when(carListingMapper.toCarListingResponse(pausedListing)).thenReturn(pausedResponse);

        // Act
        CarListingResponse response = carListingStatusService.pauseListing(listingId, username);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsUserActive()).isFalse();
        verify(carListingRepository).save(argThat(listing -> !listing.getIsUserActive()));
        verify(carListingMapper).toCarListingResponse(pausedListing);
    }

    @Test
    void pauseListing_ThrowsSecurityException_IfNotOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "otheruser"; // Not the owner
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername(username);

        testListing.setSeller(testUser); // Owner is testUser
        testListing.setApproved(true);
        testListing.setSold(false);
        testListing.setArchived(false);
        testListing.setIsUserActive(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(testListing));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            carListingStatusService.pauseListing(listingId, username);
        });
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void pauseListing_ThrowsIllegalStateException_IfAlreadyPaused() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        testListing.setApproved(true);
        testListing.setSold(false);
        testListing.setArchived(false);
        testListing.setIsUserActive(false); // Already paused

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(testListing));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.pauseListing(listingId, username);
        });

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void pauseListing_ThrowsIllegalStateException_IfArchived() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        testListing.setApproved(true);
        testListing.setSold(false);
        testListing.setArchived(true); // Archived
        testListing.setIsUserActive(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(testListing));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.pauseListing(listingId, username);
        });

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void pauseListing_ThrowsIllegalStateException_IfNotApproved() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        testListing.setApproved(false); // Not approved
        testListing.setSold(false);
        testListing.setArchived(false);
        testListing.setIsUserActive(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(testListing));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.pauseListing(listingId, username);
        });

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void pauseListing_ThrowsIllegalStateException_IfSold() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        testListing.setApproved(true);
        testListing.setSold(true); // Sold
        testListing.setArchived(false);
        testListing.setIsUserActive(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(testListing));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.pauseListing(listingId, username);
        });

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    // --- Tests for resumeListing ---

    @Test
    void resumeListing_Success_ByOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "testuser";
        testListing.setSeller(testUser);
        testListing.setApproved(true);
        testListing.setSold(false);
        testListing.setArchived(false);
        testListing.setIsUserActive(false); // Paused

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
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(resumedListing);
        when(carListingMapper.toCarListingResponse(resumedListing)).thenReturn(resumedResponse);

        // Act
        CarListingResponse response = carListingStatusService.resumeListing(listingId, username);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsUserActive()).isTrue();
        verify(carListingRepository).save(argThat(listing -> listing.getIsUserActive()));
        verify(carListingMapper).toCarListingResponse(resumedListing);
    }

    @Test
    void resumeListing_ThrowsSecurityException_IfNotOwner() {
        // Arrange
        Long listingId = 1L;
        String username = "otheruser"; // Not the owner
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername(username);

        testListing.setSeller(testUser); // Owner is testUser
        testListing.setApproved(true);
        testListing.setIsUserActive(false); // Paused

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(testListing));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            carListingStatusService.resumeListing(listingId, username);
        });
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void resumeListing_ThrowsIllegalStateException_IfAlreadyActive() {
        // Arrange
        Long listingId = 1L;
        String username = testUser.getUsername();
        testListing.setApproved(true);
        testListing.setSold(false);
        testListing.setArchived(false);
        testListing.setIsUserActive(true); // Already active

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(testListing));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            carListingStatusService.resumeListing(listingId, username);
        });

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void resumeListing_ThrowsResourceNotFoundException_IfListingNotFound() {
        // Arrange
        Long listingId = 999L; // Non-existent
        String username = "testuser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            carListingStatusService.resumeListing(listingId, username);
        });
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void admin_Operations_NotFound() {
        // Arrange
        Long nonExistentId = 999L;
        Objects.requireNonNull(nonExistentId, "Test ID cannot be null");
        when(carListingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        String expectedErrorMessage = String.format("CarListing not found with id : '%d'", nonExistentId);

        assertAll(
            () -> {
                ResourceNotFoundException soldException = assertThrows(ResourceNotFoundException.class,
                        () -> carListingStatusService.markListingAsSoldByAdmin(nonExistentId));
                assertThat(soldException)
                    .isNotNull()
                    .extracting(ResourceNotFoundException::getMessage)
                    .isEqualTo(expectedErrorMessage);
            },
            () -> {
                ResourceNotFoundException approveException = assertThrows(ResourceNotFoundException.class,
                        () -> carListingStatusService.approveListing(nonExistentId));
                assertThat(approveException)
                    .isNotNull()
                    .extracting(ResourceNotFoundException::getMessage)
                    .isEqualTo(expectedErrorMessage);
            },
            () -> {
                ResourceNotFoundException archiveException = assertThrows(ResourceNotFoundException.class,
                        () -> carListingStatusService.archiveListingByAdmin(nonExistentId));
                assertThat(archiveException)
                    .isNotNull()
                    .extracting(ResourceNotFoundException::getMessage)
                    .isEqualTo(expectedErrorMessage);
            },
            () -> {
                ResourceNotFoundException unarchiveException = assertThrows(ResourceNotFoundException.class,
                        () -> carListingStatusService.unarchiveListingByAdmin(nonExistentId));
                assertThat(unarchiveException)
                    .isNotNull()
                    .extracting(ResourceNotFoundException::getMessage)
                    .isEqualTo(expectedErrorMessage);
            }
        );

        // Verify
        verify(carListingRepository, times(4)).findById(nonExistentId);
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponseForAdmin(any(CarListing.class));
    }

    @Test
    void admin_Operations_IllegalState() {
        // Arrange - Setup listing in various invalid states
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));

        // Case 1: Already approved
        testListing.setApproved(true);
        IllegalStateException approveException = assertThrows(IllegalStateException.class,
                () -> carListingStatusService.approveListing(testListing.getId()));
        assertEquals("Listing is already approved", approveException.getMessage());

        // Case 2: Already archived
        testListing.setArchived(true);
        IllegalStateException archiveException = assertThrows(IllegalStateException.class,
                () -> carListingStatusService.archiveListingByAdmin(testListing.getId()));
        assertEquals("Listing is already archived", archiveException.getMessage());

        // Case 3: Not archived (can't unarchive)
        testListing.setArchived(false);
        IllegalStateException unarchiveException = assertThrows(IllegalStateException.class,
                () -> carListingStatusService.unarchiveListingByAdmin(testListing.getId()));
        assertEquals("Listing is not archived", unarchiveException.getMessage());

        // Case 4: Already sold
        testListing.setSold(true);
        IllegalStateException soldException = assertThrows(IllegalStateException.class,
                () -> carListingStatusService.markListingAsSoldByAdmin(testListing.getId()));
        assertEquals("Listing is already marked as sold", soldException.getMessage());

        // Verify
        verify(carListingRepository, times(4)).findById(testListing.getId());
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponseForAdmin(any(CarListing.class));
    }

    // --- Tests for expireListing ---

    @Test
    void expireListing_Success() {
        // Arrange
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        // Act
        CarListingResponse response = carListingStatusService.expireListing(testListing.getId());

        // Assert
        assertThat(response)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.getId()).isEqualTo(testListing.getId());
                assertThat(r.getIsExpired()).isTrue();
            });

        verify(carListingRepository).save(argThat(listing -> 
            Objects.nonNull(listing) &&
            listing.getId().equals(testListing.getId()) &&
            listing.getExpired() &&
            !listing.getIsUserActive()
        ));
        verify(carListingMapper).toCarListingResponse(testListing);
        verify(eventPublisher).publishEvent(any(ListingExpiredEvent.class));
    }

    @Test
    void expireListing_ThrowsResourceNotFoundException_IfListingNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        when(carListingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.expireListing(nonExistentId));
        assertEquals("CarListing not found with id : '999'", exception.getMessage());

        verify(carListingRepository).findById(nonExistentId);
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void expireListing_ThrowsIllegalStateException_IfAlreadyExpired() {
        // Arrange
        testListing.setExpired(true);
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> carListingStatusService.expireListing(testListing.getId()));
        assertEquals("Listing is already expired", exception.getMessage());

        verify(carListingRepository).findById(testListing.getId());
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void expireListing_ThrowsIllegalStateException_IfArchived() {
        // Arrange
        testListing.setArchived(true);
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> carListingStatusService.expireListing(testListing.getId()));
        assertEquals("Cannot expire an archived listing", exception.getMessage());

        verify(carListingRepository).findById(testListing.getId());
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void expireListing_ThrowsIllegalStateException_IfSold() {
        // Arrange
        testListing.setSold(true);
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> carListingStatusService.expireListing(testListing.getId()));
        assertEquals("Cannot expire a sold listing", exception.getMessage());

        verify(carListingRepository).findById(testListing.getId());
        verify(carListingRepository, never()).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any(CarListing.class));
    }
}
