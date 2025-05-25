package com.autotrader.autotraderbackend.controller;

import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.payload.response.QuickSearchResponse;
import com.autotrader.autotraderbackend.service.CarListingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private CarListingService carListingService;

    @InjectMocks
    private SearchController searchController;

    @Test
    void quickSearch_shouldReturnQuickSearchResponse() {
        // Arrange
        String term = "toyota";
        Long governorateId = 1L;
        String language = "en";
        Pageable pageable = PageRequest.of(0, 20);

        List<CarListingResponse> listings = createMockListingResponses();
        Page<CarListingResponse> listingsPage = new PageImpl<>(listings, pageable, listings.size());

        when(carListingService.quickSearch(term, governorateId, language, pageable))
                .thenReturn(listingsPage);

        // Act
        ResponseEntity<QuickSearchResponse> response = searchController.quickSearch(term, governorateId, language, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        QuickSearchResponse quickSearchResponse = response.getBody();
        assertNotNull(quickSearchResponse);
        assertEquals(term, quickSearchResponse.getSearchTerm());
        assertEquals(governorateId, quickSearchResponse.getGovernorateId());
        assertEquals(language, quickSearchResponse.getLanguage());
        assertEquals(listings.size(), quickSearchResponse.getListings().size());
        assertEquals(listingsPage.getTotalElements(), quickSearchResponse.getTotalElements());
        assertEquals(listingsPage.getTotalPages(), quickSearchResponse.getTotalPages());
        assertEquals(listingsPage.getNumber(), quickSearchResponse.getCurrentPage());
        assertEquals(listingsPage.getSize(), quickSearchResponse.getPageSize());
        assertEquals(listingsPage.hasNext(), quickSearchResponse.isHasNext());
        assertEquals(listingsPage.hasPrevious(), quickSearchResponse.isHasPrevious());

        verify(carListingService).quickSearch(term, governorateId, language, pageable);
    }

    @Test
    void searchByBrand_shouldReturnCarListingResponsePage() {
        // Arrange
        String brand = "toyota";
        String language = "en";
        Pageable pageable = PageRequest.of(0, 20);

        List<CarListingResponse> listings = createMockListingResponses();
        Page<CarListingResponse> listingsPage = new PageImpl<>(listings, pageable, listings.size());

        when(carListingService.searchByBrand(brand, language, pageable))
                .thenReturn(listingsPage);

        // Act
        ResponseEntity<Page<CarListingResponse>> response = searchController.searchByBrand(brand, language, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<CarListingResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(listings.size(), responseBody.getContent().size());
        assertEquals(listingsPage.getTotalElements(), responseBody.getTotalElements());

        verify(carListingService).searchByBrand(brand, language, pageable);
    }

    @Test
    void searchByModel_shouldReturnCarListingResponsePage() {
        // Arrange
        String model = "camry";
        String language = "en";
        Pageable pageable = PageRequest.of(0, 20);

        List<CarListingResponse> listings = createMockListingResponses();
        Page<CarListingResponse> listingsPage = new PageImpl<>(listings, pageable, listings.size());

        when(carListingService.searchByModel(model, language, pageable))
                .thenReturn(listingsPage);

        // Act
        ResponseEntity<Page<CarListingResponse>> response = searchController.searchByModel(model, language, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<CarListingResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(listings.size(), responseBody.getContent().size());
        assertEquals(listingsPage.getTotalElements(), responseBody.getTotalElements());

        verify(carListingService).searchByModel(model, language, pageable);
    }

    @Test
    void searchByGovernorate_shouldReturnCarListingResponsePage() {
        // Arrange
        String governorate = "damascus";
        String language = "en";
        Pageable pageable = PageRequest.of(0, 20);

        List<CarListingResponse> listings = createMockListingResponses();
        Page<CarListingResponse> listingsPage = new PageImpl<>(listings, pageable, listings.size());

        when(carListingService.searchByGovernorate(governorate, language, pageable))
                .thenReturn(listingsPage);

        // Act
        ResponseEntity<Page<CarListingResponse>> response = searchController.searchByGovernorate(governorate, language, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<CarListingResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(listings.size(), responseBody.getContent().size());
        assertEquals(listingsPage.getTotalElements(), responseBody.getTotalElements());

        verify(carListingService).searchByGovernorate(governorate, language, pageable);
    }

    private List<CarListingResponse> createMockListingResponses() {
        // Create two mock listings 
        CarListingResponse listing1 = new CarListingResponse();
        listing1.setId(1L);
        listing1.setTitle("Toyota Camry 2020");
        listing1.setBrand("Toyota");
        listing1.setModel("Camry");
        listing1.setPrice(BigDecimal.valueOf(25000));
        listing1.setModelYear(2020);
        listing1.setCreatedAt(LocalDateTime.now());
        
        CarListingResponse listing2 = new CarListingResponse();
        listing2.setId(2L);
        listing2.setTitle("Toyota Corolla 2019");
        listing2.setBrand("Toyota");
        listing2.setModel("Corolla");
        listing2.setPrice(BigDecimal.valueOf(22000));
        listing2.setModelYear(2019);
        listing2.setCreatedAt(LocalDateTime.now());
        
        return Arrays.asList(listing1, listing2);
    }
}
