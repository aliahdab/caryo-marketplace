package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.exception.ResourceNotFoundException;
import com.autotrader.autotraderbackend.exception.StorageException;
import com.autotrader.autotraderbackend.events.ListingApprovedEvent;
import com.autotrader.autotraderbackend.events.ListingArchivedEvent;
import com.autotrader.autotraderbackend.events.ListingMarkedAsSoldEvent;
import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.ListingMedia;
import com.autotrader.autotraderbackend.model.Location;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.payload.request.CreateListingRequest;
import com.autotrader.autotraderbackend.payload.request.ListingFilterRequest;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.payload.response.LocationResponse;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.LocationRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import com.autotrader.autotraderbackend.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarListingServiceTest {

    @Mock
    private CarListingRepository carListingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CarListingMapper carListingMapper;

<<<<<<< HEAD
    @Mock
    private CarListingStatusService carListingStatusService;

    @InjectMocks
=======
    @Mock // Add mock for the event publisher
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks // Ensure this injects all mocks into the service
>>>>>>> 43c7c09 (feat: Implement ListingApprovedEvent and integrate event publishing in CarListingService; add corresponding tests)
    private CarListingService carListingService;

    private User testUser;
    private CarListing savedListing;
    private CarListing listingToSave;
    private CarListingResponse expectedResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        listingToSave = new CarListing();
        listingToSave.setSeller(testUser);
        listingToSave.setApproved(false);
        listingToSave.setTitle("Honda Civic");
        listingToSave.setBrand("Honda");
        listingToSave.setModel("Civic");
        listingToSave.setModelYear(2020);
        listingToSave.setPrice(new BigDecimal("20000.00"));
        listingToSave.setMileage(10000);
        Location testLocation = new Location();
        testLocation.setId(1L);
        testLocation.setDisplayNameEn("Test Location");
        testLocation.setDisplayNameAr("موقع اختبار");
        testLocation.setSlug("test-location");
        testLocation.setCountryCode("SY");
        listingToSave.setLocation(testLocation);
        listingToSave.setDescription("Test Description");


        savedListing = new CarListing();
        savedListing.setId(1L);
        savedListing.setSeller(testUser);
        savedListing.setApproved(false);
        savedListing.setTitle("Honda Civic");
        savedListing.setBrand("Honda");
        savedListing.setModel("Civic");
        savedListing.setModelYear(2020);
        savedListing.setPrice(new BigDecimal("20000.00"));
        savedListing.setMileage(10000);
        Location testLocationSaved = new Location();
        testLocationSaved.setId(1L);
        testLocationSaved.setDisplayNameEn("Test Location");
        testLocationSaved.setDisplayNameAr("موقع اختبار");
        testLocationSaved.setSlug("test-location");
        testLocationSaved.setCountryCode("SY");
        savedListing.setLocation(testLocationSaved);
        savedListing.setDescription("Test Description");
        savedListing.setCreatedAt(LocalDateTime.now());

        expectedResponse = new CarListingResponse();
        expectedResponse.setId(savedListing.getId());
        expectedResponse.setTitle(savedListing.getTitle());
        expectedResponse.setBrand(savedListing.getBrand());
        expectedResponse.setModel(savedListing.getModel());
        expectedResponse.setModelYear(savedListing.getModelYear());
        expectedResponse.setPrice(savedListing.getPrice());
        expectedResponse.setMileage(savedListing.getMileage());
        LocationResponse locationResp = new LocationResponse();
        locationResp.setId(1L);
        locationResp.setDisplayNameEn("Test Location");
        expectedResponse.setLocationDetails(locationResp);
        expectedResponse.setDescription(savedListing.getDescription());
        expectedResponse.setCreatedAt(savedListing.getCreatedAt());
        expectedResponse.setApproved(savedListing.getApproved());
        expectedResponse.setSellerId(testUser.getId());
        expectedResponse.setSellerUsername(testUser.getUsername());
    }

    // --- Tests for createListing ---
    @Test
    void createListing_WithValidData_ShouldCreateAndReturnListing() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Honda Civic");
        request.setBrand("Honda");
        request.setModel("Civic");
        request.setModelYear(2020);
        request.setPrice(new BigDecimal("20000.00"));
        request.setMileage(10000);
        request.setLocationId(1L);
        request.setDescription("Test Description");

        Location testLocation = new Location();
        testLocation.setId(1L);
        testLocation.setDisplayNameEn("Test Location");
        testLocation.setDisplayNameAr("موقع اختبار");
        testLocation.setSlug("test-location");
        testLocation.setCountryCode("SY");
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(savedListing);
        when(carListingMapper.toCarListingResponse(savedListing)).thenReturn(expectedResponse);

        CarListingResponse response = carListingService.createListing(request, null, "testuser");

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(carListingRepository).save(any(CarListing.class));
        verify(carListingMapper).toCarListingResponse(savedListing);
    }

    @Test
    void createListing_WithNonExistentUser_ShouldThrowException() {
        CreateListingRequest request = new CreateListingRequest();
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingService.createListing(request, null, username);
        });
        assertEquals("User not found with username : 'nonexistentuser'", exception.getMessage());
        verify(carListingRepository, never()).save(any());
        verify(carListingMapper, never()).toCarListingResponse(any());
    }

    @Test
    void createListing_WhenRepositorySaveFails_ShouldThrowRuntimeException() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Test Car");
        request.setBrand("TestBrand");
        request.setModel("TestModel");
        request.setModelYear(2022);
        request.setPrice(new BigDecimal("15000"));
        request.setMileage(5000);
        request.setLocationId(1L);
        request.setDescription("TestDesc");
        String username = "testuser";
        RuntimeException dbException = new RuntimeException("Database connection failed");

        Location testLocation = new Location();
        testLocation.setId(1L);
        testLocation.setDisplayNameEn("Test Location");
        testLocation.setDisplayNameAr("موقع اختبار");
        testLocation.setSlug("test-location");
        testLocation.setCountryCode("SY");
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.save(any(CarListing.class))).thenThrow(dbException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            carListingService.createListing(request, null, username);
        });

        assertEquals("Database connection failed", thrown.getMessage());
        assertSame(dbException, thrown);

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).save(any(CarListing.class));
        verify(carListingMapper, never()).toCarListingResponse(any());
    }

    // --- Tests for getListingById ---
    @Test
    void getListingById_Success_WhenApproved() {
        Long listingId = 1L;
        savedListing.setApproved(true);
        expectedResponse.setApproved(true);

        when(carListingRepository.findByIdAndApprovedTrue(listingId)).thenReturn(Optional.of(savedListing));
        when(carListingMapper.toCarListingResponse(savedListing)).thenReturn(expectedResponse);

        CarListingResponse response = carListingService.getListingById(listingId);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(carListingRepository).findByIdAndApprovedTrue(listingId);
        verify(carListingMapper).toCarListingResponse(savedListing);
    }

    @Test
    void getListingById_NotFound_ThrowsResourceNotFoundException() {
        Long nonExistentId = 999L;
        when(carListingRepository.findByIdAndApprovedTrue(nonExistentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingService.getListingById(nonExistentId);
        });
        assertEquals("CarListing not found with id : '999'", exception.getMessage());
        verify(carListingRepository).findByIdAndApprovedTrue(nonExistentId);
        verify(carListingMapper, never()).toCarListingResponse(any());
    }

    @Test
    void getListingById_ExistsButNotApproved_ThrowsResourceNotFoundException() {
        Long listingId = 1L;
        savedListing.setApproved(false);

        when(carListingRepository.findByIdAndApprovedTrue(listingId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingService.getListingById(listingId);
        });

        assertEquals("CarListing not found with id : '1'", exception.getMessage());

        verify(carListingRepository).findByIdAndApprovedTrue(listingId);
        verify(carListingMapper, never()).toCarListingResponse(any());
    }


<<<<<<< HEAD
=======
    // --- Tests for approveListing ---
    @Test
    void approveListing_Success() {
        // Arrange
        Long listingId = 1L;
        savedListing.setApproved(false); // Ensure it starts as not approved
        CarListing approvedListing = new CarListing(); // Create a separate instance for the state after save
        // Copy properties from savedListing
        approvedListing.setId(savedListing.getId());
        approvedListing.setTitle(savedListing.getTitle());
        // ... copy other properties ...
        approvedListing.setBrand(savedListing.getBrand());
        approvedListing.setModel(savedListing.getModel());
        approvedListing.setModelYear(savedListing.getModelYear());
        approvedListing.setPrice(savedListing.getPrice());
        approvedListing.setMileage(savedListing.getMileage());
        approvedListing.setLocation(savedListing.getLocation()); // Use location
        approvedListing.setDescription(savedListing.getDescription());
        approvedListing.setSeller(savedListing.getSeller());
        approvedListing.setApproved(true); // Set approved to true
        approvedListing.setCreatedAt(savedListing.getCreatedAt());

        CarListingResponse approvedResponse = new CarListingResponse(); // Expected response after approval
        // ... populate approvedResponse based on approvedListing ...
        approvedResponse.setId(approvedListing.getId());
        approvedResponse.setTitle(approvedListing.getTitle());
        approvedResponse.setBrand(approvedListing.getBrand());
        approvedResponse.setModel(approvedListing.getModel());
        approvedResponse.setModelYear(approvedListing.getModelYear());
        approvedResponse.setPrice(approvedListing.getPrice());
        approvedResponse.setMileage(approvedListing.getMileage());
        // Create and set LocationResponse for LocationDetails
        if (approvedListing.getLocation() != null) {
            LocationResponse locationResp = new LocationResponse();
            locationResp.setId(approvedListing.getLocation().getId());
            locationResp.setDisplayNameEn(approvedListing.getLocation().getDisplayNameEn());
            // Set other fields of locationResp if necessary
            approvedResponse.setLocationDetails(locationResp);
        }
        approvedResponse.setDescription(approvedListing.getDescription());
        approvedResponse.setCreatedAt(approvedListing.getCreatedAt());
        approvedResponse.setApproved(true);
        if (approvedListing.getSeller() != null) {
            approvedResponse.setSellerId(approvedListing.getSeller().getId());
            approvedResponse.setSellerUsername(approvedListing.getSeller().getUsername());
        }

        // Setup argument captor for the event
        ArgumentCaptor<ListingApprovedEvent> eventCaptor = ArgumentCaptor.forClass(ListingApprovedEvent.class);

        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(carListingRepository.save(any(CarListing.class))).thenReturn(approvedListing); // Return the approved state
        // Mock the mapper call for the approved state
        when(carListingMapper.toCarListingResponse(approvedListing)).thenReturn(approvedResponse);

        // Act
        CarListingResponse response = carListingService.approveListing(listingId);

        // Assert
        assertNotNull(response);
        assertTrue(response.getApproved());
        assertEquals(approvedResponse, response);
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository).save(argThat(listing -> listing.getId().equals(listingId) && Boolean.TRUE.equals(listing.getApproved())));
        verify(carListingMapper).toCarListingResponse(approvedListing);

        // Verify event publishing
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ListingApprovedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals(approvedListing, capturedEvent.getListing());
        assertEquals(carListingService, capturedEvent.getSource());
    }

    @Test
    void approveListing_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistentId = 999L;
        when(carListingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingService.approveListing(nonExistentId);
        });
        assertEquals("CarListing not found with id : '999'", exception.getMessage());
        verify(carListingRepository).findById(nonExistentId);
        verify(carListingRepository, never()).save(any());
        verify(carListingMapper, never()).toCarListingResponse(any());
        // Verify no events were published
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void approveListing_AlreadyApproved_ThrowsIllegalStateException() {
        // Arrange
        Long listingId = 1L;
        savedListing.setApproved(true); // Start as already approved
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            carListingService.approveListing(listingId);
        });
        assertEquals("Listing with ID 1 is already approved.", exception.getMessage());
        verify(carListingRepository).findById(listingId);
        verify(carListingRepository, never()).save(any());
        verify(carListingMapper, never()).toCarListingResponse(any());
        // Verify no events were published
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void approveListing_WhenRepositorySaveFails_ShouldThrowRuntimeException() {
        // Arrange
        Long listingId = 1L;
        savedListing.setApproved(false); // Ensure it starts as not approved
        RuntimeException dbException = new RuntimeException("DB save failed");

        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        // Mock repository save to throw an exception
        when(carListingRepository.save(any(CarListing.class))).thenThrow(dbException);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            carListingService.approveListing(listingId);
        });

        // Assert that the original exception message is thrown
        assertEquals("DB save failed", thrown.getMessage()); // <-- Updated assertion
        assertSame(dbException, thrown); // Verify it's the exact exception instance

        verify(carListingRepository).findById(listingId);
        // Verify save was attempted with the correct state
        verify(carListingRepository).save(argThat(listing -> listing.getId().equals(listingId) && Boolean.TRUE.equals(listing.getApproved())));
        verify(carListingMapper, never()).toCarListingResponse(any()); // Mapper should not be called
    }

>>>>>>> 43c7c09 (feat: Implement ListingApprovedEvent and integrate event publishing in CarListingService; add corresponding tests)
    // --- Tests for getAllApprovedListings & getFilteredListings ---
    @Test
    void getAllApprovedListings_ShouldReturnPageOfApprovedListings() {
        Pageable pageable = PageRequest.of(0, 10);
        CarListing approvedListing1 = new CarListing();
        approvedListing1.setId(1L);
        approvedListing1.setApproved(true);
        approvedListing1.setSold(false);
        approvedListing1.setArchived(false);
        CarListing approvedListing2 = new CarListing();
        approvedListing2.setId(2L);
        approvedListing2.setApproved(true);
        approvedListing2.setSold(false);
        approvedListing2.setArchived(false);
        List<CarListing> listings = Arrays.asList(approvedListing1, approvedListing2);
        Page<CarListing> listingPage = new PageImpl<>(listings, pageable, listings.size());

        CarListingResponse response1 = new CarListingResponse();
        response1.setId(1L);
        response1.setApproved(true);
        CarListingResponse response2 = new CarListingResponse();
        response2.setId(2L);
        response2.setApproved(true);

        when(carListingRepository.findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<CarListing>>any(), eq(pageable))).thenReturn(listingPage);
        when(carListingMapper.toCarListingResponse(approvedListing1)).thenReturn(response1);
        when(carListingMapper.toCarListingResponse(approvedListing2)).thenReturn(response2);

        Page<CarListingResponse> responsePage = carListingService.getAllApprovedListings(pageable);

        assertNotNull(responsePage);
        assertEquals(2, responsePage.getTotalElements());
        assertEquals(2, responsePage.getContent().size());
        assertEquals(response1, responsePage.getContent().get(0));
        assertEquals(response2, responsePage.getContent().get(1));
        verify(carListingRepository).findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<CarListing>>any(), eq(pageable));
        verify(carListingMapper, times(2)).toCarListingResponse(any(CarListing.class));
    }

     @Test
    void getAllApprovedListings_WhenNoneFound_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CarListing> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(carListingRepository.findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<CarListing>>any(), eq(pageable))).thenReturn(emptyPage);

        Page<CarListingResponse> responsePage = carListingService.getAllApprovedListings(pageable);

        assertNotNull(responsePage);
        assertTrue(responsePage.isEmpty());
        assertEquals(0, responsePage.getTotalElements());
        verify(carListingRepository).findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<CarListing>>any(), eq(pageable));
        verify(carListingMapper, never()).toCarListingResponse(any());
    }

    @Test
    void getFilteredListings_ShouldReturnFilteredAndApprovedListings() {
        Pageable pageable = PageRequest.of(0, 10);
        ListingFilterRequest filter = new ListingFilterRequest();
        filter.setBrand("Honda");

        CarListing filteredListing = new CarListing();
        filteredListing.setId(1L);
        filteredListing.setBrand("Honda");
        filteredListing.setApproved(true);
        List<CarListing> listings = Collections.singletonList(filteredListing);
        Page<CarListing> listingPage = new PageImpl<>(listings, pageable, 1);

        CarListingResponse filteredResponse = new CarListingResponse();
        filteredResponse.setId(1L);
        filteredResponse.setBrand("Honda");
        filteredResponse.setApproved(true);

        when(carListingRepository.findAll(ArgumentMatchers.<Specification<CarListing>>any(), eq(pageable))).thenReturn(listingPage);
        when(carListingMapper.toCarListingResponse(filteredListing)).thenReturn(filteredResponse);

        Page<CarListingResponse> responsePage = carListingService.getFilteredListings(filter, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(1, responsePage.getContent().size());
        assertEquals(filteredResponse, responsePage.getContent().get(0));
        verify(carListingRepository).findAll(ArgumentMatchers.<Specification<CarListing>>any(), eq(pageable));
        verify(carListingMapper).toCarListingResponse(filteredListing);
    }

    @Test
    void getFilteredListings_WhenNoneMatch_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        ListingFilterRequest filter = new ListingFilterRequest();
        filter.setBrand("NonExistent");
        Page<CarListing> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(carListingRepository.findAll(ArgumentMatchers.<Specification<CarListing>>any(), eq(pageable))).thenReturn(emptyPage);

        Page<CarListingResponse> responsePage = carListingService.getFilteredListings(filter, pageable);

        assertNotNull(responsePage);
        assertTrue(responsePage.isEmpty());
        verify(carListingRepository).findAll(ArgumentMatchers.<Specification<CarListing>>any(), eq(pageable));
        verify(carListingMapper, never()).toCarListingResponse(any());
    }

    // --- Tests for uploadListingImage ---
    @Test
    void uploadListingImage_Success() throws IOException {
        Long listingId = savedListing.getId();
        String username = testUser.getUsername();
        MockMultipartFile file = new MockMultipartFile(
                "file", "hello.jpg", "image/jpeg", "Hello, World!".getBytes()
        );
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(storageService.store(eq(file), keyCaptor.capture())).thenAnswer(invocation -> keyCaptor.getValue());
        when(carListingRepository.save(any(CarListing.class))).thenAnswer(invocation -> {
             CarListing listingToSave = invocation.getArgument(0);
             assertNotNull(listingToSave.getMedia());
             assertFalse(listingToSave.getMedia().isEmpty());
             assertEquals(keyCaptor.getValue(), listingToSave.getMedia().get(0).getFileKey());
             return listingToSave;
        });

        String returnedKey = carListingService.uploadListingImage(listingId, file, username);

        assertNotNull(returnedKey);
        assertEquals(returnedKey, keyCaptor.getValue());

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(storageService).store(eq(file), eq(returnedKey));
        verify(carListingRepository).save(argThat(l -> {
            if (l.getId().equals(listingId) && !l.getMedia().isEmpty()) {
                ListingMedia media = l.getMedia().get(0);
                return returnedKey.equals(media.getFileKey());
            }
            return false;
        }));
    }

    @Test
    void uploadListingImage_ListingNotFound_ThrowsResourceNotFoundException() {
        Long nonExistentId = 999L;
        String username = testUser.getUsername();
        MockMultipartFile file = new MockMultipartFile("file", "hello.jpg", "image/jpeg", "content".getBytes());
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser)); // Mock user lookup
        when(carListingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingService.uploadListingImage(nonExistentId, file, username);
        });
        assertEquals("CarListing not found with id : '999'", exception.getMessage());

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(nonExistentId);
        verify(storageService, never()).store(any(MultipartFile.class), anyString());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void uploadListingImage_UnauthorizedUser_ThrowsSecurityException() {
        Long listingId = savedListing.getId();
        String wrongUsername = "wronguser";
        User wrongUser = new User();
        wrongUser.setId(99L);
        wrongUser.setUsername(wrongUsername);

        MockMultipartFile file = new MockMultipartFile("file", "hello.jpg", "image/jpeg", "content".getBytes());
        when(userRepository.findByUsername(wrongUsername)).thenReturn(Optional.of(wrongUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            carListingService.uploadListingImage(listingId, file, wrongUsername);
        });
        assertEquals("User does not have permission to modify this listing.", exception.getMessage());

        verify(userRepository).findByUsername(wrongUsername);
        verify(carListingRepository).findById(listingId);
        verify(storageService, never()).store(any(MultipartFile.class), anyString());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void uploadListingImage_EmptyFile_ThrowsStorageException() {
        Long listingId = savedListing.getId();
        String username = testUser.getUsername();
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]); // Empty file

        StorageException exception = assertThrows(StorageException.class, () -> {
            carListingService.uploadListingImage(listingId, emptyFile, username);
        });
        assertEquals("File provided for upload is null or empty.", exception.getMessage());

        verify(carListingRepository, never()).findById(anyLong());
        verify(storageService, never()).store(any(MultipartFile.class), anyString());
        verify(carListingRepository, never()).save(any());
    }


    @Test
    void uploadListingImage_StorageFailure_ThrowsRuntimeException() throws IOException {
        Long listingId = savedListing.getId();
        String username = testUser.getUsername();
        MockMultipartFile file = new MockMultipartFile("file", "hello.jpg", "image/jpeg", "content".getBytes());
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        doThrow(new StorageException("Disk full")).when(storageService).store(eq(file), keyCaptor.capture());

        StorageException exception = assertThrows(StorageException.class, () -> {
             carListingService.uploadListingImage(listingId, file, username);
        });
        assertEquals("Failed to store image file.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof StorageException);
        assertEquals("Disk full", exception.getCause().getMessage());


        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(storageService).store(eq(file), keyCaptor.capture());
        verify(carListingRepository, never()).save(any());
    }

    @Test
    void uploadListingImage_WhenRepositorySaveFailsAfterStore_ShouldThrowRuntimeException() throws IOException {
        Long listingId = savedListing.getId();
        String username = testUser.getUsername();
        MockMultipartFile file = new MockMultipartFile("file", "hello.jpg", "image/jpeg", "content".getBytes());
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        RuntimeException dbException = new RuntimeException("DB save failed");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(storageService.store(eq(file), keyCaptor.capture())).thenAnswer(inv -> keyCaptor.getValue());
        when(carListingRepository.save(any(CarListing.class))).thenThrow(dbException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            carListingService.uploadListingImage(listingId, file, username);
        });

        assertEquals("Failed to update listing after image upload.", thrown.getMessage());
        assertSame(dbException, thrown.getCause());

        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findById(listingId);
        verify(storageService).store(eq(file), eq(keyCaptor.getValue()));
        verify(carListingRepository).save(any(CarListing.class));
    }

    @Test
    void uploadListingImage_WithNullOriginalFilename_ShouldGenerateSafeKey() throws IOException {
        Long listingId = savedListing.getId();
        String username = testUser.getUsername();
        MockMultipartFile file = new MockMultipartFile("file", null, "image/png", "content".getBytes());
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findById(listingId)).thenReturn(Optional.of(savedListing));
        when(storageService.store(eq(file), keyCaptor.capture())).thenAnswer(inv -> keyCaptor.getValue());
        when(carListingRepository.save(any(CarListing.class))).thenAnswer(inv -> inv.getArgument(0));

        String returnedKey = carListingService.uploadListingImage(listingId, file, username);

        assertNotNull(returnedKey);

        verify(storageService).store(eq(file), keyCaptor.capture());
        String capturedKey = keyCaptor.getValue();

        assertEquals(returnedKey, capturedKey);
        assertTrue(capturedKey.startsWith("listings/" + listingId + "/"));
        assertTrue(capturedKey.matches("listings/" + listingId + "/\\d+_"),
                   "Generated key '" + capturedKey + "' did not match expected pattern.");

        verify(carListingRepository).save(argThat(l -> {
            if (!l.getMedia().isEmpty()) {
                ListingMedia media = l.getMedia().get(0);
                return capturedKey.equals(media.getFileKey());
            }
            return false;
        }));
    }

    // --- Tests for getMyListings ---
    @Test
    void getMyListings_ShouldReturnUserListings() {
        String username = testUser.getUsername();
        CarListing listing1 = new CarListing();
        listing1.setId(1L);
        listing1.setSeller(testUser);
        CarListing listing2 = new CarListing();
        listing2.setId(2L);
        listing2.setSeller(testUser);
        List<CarListing> userListings = Arrays.asList(listing1, listing2);

        CarListingResponse response1 = new CarListingResponse();
        response1.setId(1L);
        CarListingResponse response2 = new CarListingResponse();
        response2.setId(2L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findBySeller(testUser)).thenReturn(userListings);
        when(carListingMapper.toCarListingResponse(listing1)).thenReturn(response1);
        when(carListingMapper.toCarListingResponse(listing2)).thenReturn(response2);

        List<CarListingResponse> responses = carListingService.getMyListings(username);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.contains(response1));
        assertTrue(responses.contains(response2));
        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findBySeller(testUser);
        verify(carListingMapper, times(2)).toCarListingResponse(any(CarListing.class));
    }

    @Test
    void getMyListings_WhenUserNotFound_ShouldThrowException() {
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carListingService.getMyListings(username);
        });
        assertEquals("User not found with username : 'nonexistentuser'", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(carListingRepository, never()).findBySeller(any());
        verify(carListingMapper, never()).toCarListingResponse(any());
    }

    @Test
    void getMyListings_WhenUserHasNoListings_ShouldReturnEmptyList() {
        String username = testUser.getUsername();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(carListingRepository.findBySeller(testUser)).thenReturn(Collections.emptyList());

        List<CarListingResponse> responses = carListingService.getMyListings(username);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(userRepository).findByUsername(username);
        verify(carListingRepository).findBySeller(testUser);
        verify(carListingMapper, never()).toCarListingResponse(any());
    }
}
