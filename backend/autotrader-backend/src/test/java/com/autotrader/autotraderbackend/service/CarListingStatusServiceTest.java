package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.events.ListingApprovedEvent;
import com.autotrader.autotraderbackend.events.ListingArchivedEvent;
import com.autotrader.autotraderbackend.events.ListingMarkedAsSoldEvent;
import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    }

    @Test
    void markListingAsSold_Success() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(testListing);
        when(carListingMapper.toCarListingResponse(any(CarListing.class))).thenReturn(testListingResponse);

        carListingStatusService.markListingAsSold(testListing.getId(), testUser.getUsername());

        verify(carListingRepository).save(testListing);
        verify(eventPublisher).publishEvent(any(ListingMarkedAsSoldEvent.class));
        assertThat(testListing.getSold()).isTrue();
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
        when(carListingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.markListingAsSold(999L, testUser.getUsername()));
        assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.archiveListing(999L, testUser.getUsername()));
        assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.unarchiveListing(999L, testUser.getUsername()));
        assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.pauseListing(999L, testUser.getUsername()));
        assertThrows(ResourceNotFoundException.class,
                () -> carListingStatusService.resumeListing(999L, testUser.getUsername()));
    }

    @Test
    void unauthorized_WrongUser() {
        User wrongUser = new User();
        wrongUser.setId(2L);
        wrongUser.setUsername("wronguser");

        when(userRepository.findByUsername(wrongUser.getUsername())).thenReturn(Optional.of(wrongUser));
        when(carListingRepository.findById(testListing.getId())).thenReturn(Optional.of(testListing));

        assertThrows(SecurityException.class,
                () -> carListingStatusService.markListingAsSold(testListing.getId(), wrongUser.getUsername()));
        assertThrows(SecurityException.class,
                () -> carListingStatusService.archiveListing(testListing.getId(), wrongUser.getUsername()));
        assertThrows(SecurityException.class,
                () -> carListingStatusService.unarchiveListing(testListing.getId(), wrongUser.getUsername()));
        assertThrows(SecurityException.class,
                () -> carListingStatusService.pauseListing(testListing.getId(), wrongUser.getUsername()));
        assertThrows(SecurityException.class,
                () -> carListingStatusService.resumeListing(testListing.getId(), wrongUser.getUsername()));
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
}
