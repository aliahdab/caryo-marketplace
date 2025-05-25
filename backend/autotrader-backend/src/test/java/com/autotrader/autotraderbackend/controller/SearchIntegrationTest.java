package com.autotrader.autotraderbackend.controller;

import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.Governorate;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.payload.response.LocationResponse;
import com.autotrader.autotraderbackend.payload.response.QuickSearchResponse;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.GovernorateRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import com.autotrader.autotraderbackend.service.CarListingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the SearchController.
 * These tests validate the search functionality by testing all search endpoints
 * with various search criteria.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) // Changed from AFTER_CLASS
public class SearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarListingService carListingService;

    @MockBean
    private CarListingRepository carListingRepository;

    @MockBean
    private GovernorateRepository governorateRepository;

    @MockBean
    private UserRepository userRepository;

    // Test data
    private User testUser;
    private Governorate damascusGovernorate;
    private Governorate aleppoGovernorate;
    private List<CarListingResponse> toyotaListings;
    private List<CarListingResponse> hondaListings;
    private List<CarListingResponse> damascusListings;
    private List<CarListingResponse> aleppoListings;

    /**
     * Set up test data and mock responses for each test.
     */
    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = createTestUser();

        // Set up governorates
        setupGovernoratesTestData();
        
        // Create test listings responses
        setupTestListings();

        // Configure mock service responses
        configureServiceMocks();
    }

    /**
     * Creates a test user with basic properties
     */
    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        return user;
    }

    /**
     * Sets up governorate test data
     */
    private void setupGovernoratesTestData() {
        damascusGovernorate = new Governorate();
        damascusGovernorate.setId(1L);
        damascusGovernorate.setDisplayNameEn("Damascus");
        damascusGovernorate.setDisplayNameAr("دمشق");
        damascusGovernorate.setSlug("damascus");
        damascusGovernorate.setCountryCode("SY");

        aleppoGovernorate = new Governorate();
        aleppoGovernorate.setId(2L);
        aleppoGovernorate.setDisplayNameEn("Aleppo");
        aleppoGovernorate.setDisplayNameAr("حلب");
        aleppoGovernorate.setSlug("aleppo");
        aleppoGovernorate.setCountryCode("SY");
    }

    /**
     * Sets up test car listings
     */
    private void setupTestListings() {
        toyotaListings = createCarListingResponses("Toyota", "Camry", damascusGovernorate, 2);
        hondaListings = createCarListingResponses("Honda", "Accord", aleppoGovernorate, 2);
        
        damascusListings = new ArrayList<>(toyotaListings);
        damascusListings.add(createCarListingResponse("BMW", "X5", damascusGovernorate));
        
        aleppoListings = new ArrayList<>(hondaListings);
    }

    /**
     * Configures the mock service responses for all test scenarios
     */
    private void configureServiceMocks() {
        // Toyota search mock
        when(carListingService.quickSearch(eq("toyota"), isNull(), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(toyotaListings));
        
        // Arabic search mock        
        when(carListingService.quickSearch(eq("تويوتا"), isNull(), eq("ar"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(toyotaListings));
        
        // Damascus governorate search mock        
        when(carListingService.quickSearch(isNull(), eq(damascusGovernorate.getId()), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(damascusListings));
        
        // Honda brand search mock        
        when(carListingService.searchByBrand(eq("honda"), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(hondaListings));
        
        // Camry model search mock        
        when(carListingService.searchByModel(eq("camry"), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(toyotaListings.get(0))));
        
        // Aleppo governorate search mock        
        when(carListingService.searchByGovernorate(eq("aleppo"), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(aleppoListings));
    }

    /**
     * Tests the quick search endpoint with a brand term filter.
     * Verifies that results match the specified brand.
     */
    @Test
    @DisplayName("Quick search should return listings matching the brand term")
    @WithMockUser(username = "testuser")
    void quickSearch_withBrandTerm_shouldReturnMatchingListings() throws Exception {
        // Perform the request
        MvcResult result = mockMvc.perform(get("/api/search/quick")
                        .param("term", "toyota")
                        .param("language", "en")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Parse the response
        String content = result.getResponse().getContentAsString();
        QuickSearchResponse response = objectMapper.readValue(content, QuickSearchResponse.class);

        // Verify the response
        assertNotNull(response, "Response should not be null");
        assertEquals("toyota", response.getSearchTerm(), "Search term should match the requested term");
        assertEquals(2, response.getListings().size(), "Should return exactly 2 Toyota listings");
        assertTrue(response.getListings().stream()
                .allMatch(listing -> listing.getBrand().toLowerCase().contains("toyota")),
                "All returned listings should be Toyota brand");
    }

    /**
     * Tests the quick search endpoint with a governorate ID filter.
     * Verifies that results match the specified governorate.
     */
    @Test
    @DisplayName("Quick search should return listings in the specified governorate")
    @WithMockUser(username = "testuser")
    void quickSearch_withGovernorateId_shouldReturnListingsInGovernorate() throws Exception {
        // Perform the request
        MvcResult result = mockMvc.perform(get("/api/search/quick")
                        .param("governorateId", damascusGovernorate.getId().toString())
                        .param("language", "en")
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Parse the response
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        QuickSearchResponse response = objectMapper.readValue(content, QuickSearchResponse.class);

        // Verify the response
        assertNotNull(response, "Response should not be null");
        assertEquals(damascusGovernorate.getId(), response.getGovernorateId(), 
                "Response governorate ID should match the requested ID");
        assertEquals(3, response.getListings().size(), 
                "Should find 3 listings in Damascus");

        // Detailed check for each listing
        for (CarListingResponse listing : response.getListings()) {
            assertNotNull(listing.getLocationDetails(), 
                    "LocationDetails should not be null for listing with title: " + listing.getTitle());
            assertEquals("Damascus", listing.getLocationDetails().getDisplayNameEn(), 
                    "Listing should be in Damascus, title: " + listing.getTitle());
        }
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchByBrand_shouldReturnMatchingListings() throws Exception {
        // Perform the request
        MvcResult result = mockMvc.perform(get("/api/search/by-brand")
                        .param("brand", "honda")
                        .param("language", "en")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Parse the response
        String content = result.getResponse().getContentAsString();
        Page<CarListingResponse> response = objectMapper.readValue(content, 
                new TypeReference<RestResponsePage<CarListingResponse>>() {});

        // Verify the response
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertTrue(response.getContent().stream()
                .allMatch(listing -> listing.getBrand().toLowerCase().contains("honda")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchByModel_shouldReturnMatchingListings() throws Exception {
        // Perform the request
        MvcResult result = mockMvc.perform(get("/api/search/by-model")
                        .param("model", "camry")
                        .param("language", "en")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Parse the response
        String content = result.getResponse().getContentAsString();
        Page<CarListingResponse> response = objectMapper.readValue(content, 
                new TypeReference<RestResponsePage<CarListingResponse>>() {});

        // Verify the response
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Camry", response.getContent().get(0).getModel());
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchByGovernorate_shouldReturnMatchingListings() throws Exception {
        // Perform the request
        MvcResult result = mockMvc.perform(get("/api/search/by-governorate")
                        .param("governorate", "aleppo")
                        .param("language", "en")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Parse the response
        String content = result.getResponse().getContentAsString();
        Page<CarListingResponse> response = objectMapper.readValue(content, 
                new TypeReference<RestResponsePage<CarListingResponse>>() {});

        // Verify the response
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        // Check LocationDetails
        assertTrue(response.getContent().stream()
                .allMatch(listing -> 
                    listing.getLocationDetails() != null && 
                    "Aleppo".equals(listing.getLocationDetails().getDisplayNameEn())));
    }

    @Test
    @WithMockUser(username = "testuser")
    void arabicSearch_shouldWorkWithArabicTerms() throws Exception {
        // Perform the request for Arabic search
        MvcResult result = mockMvc.perform(get("/api/search/quick")
                        .param("term", "تويوتا")
                        .param("language", "ar")
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Parse the response, ensuring UTF-8
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        QuickSearchResponse response = objectMapper.readValue(content, QuickSearchResponse.class);

        // Verify the response
        assertNotNull(response);
        assertEquals("تويوتا", response.getSearchTerm());
        assertTrue(response.getListings().size() > 0, "Should find listings for Arabic term 'تويوتا'");
    }

    private List<CarListingResponse> createCarListingResponses(String brand, String model, Governorate governorate, int count) {
        List<CarListingResponse> listings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            listings.add(createCarListingResponse(brand, model, governorate));
        }
        return listings;
    }

    private CarListingResponse createCarListingResponse(String brand, String model, Governorate governorate) {
        CarListingResponse response = new CarListingResponse();
        response.setId((long) (Math.random() * 1000));
        response.setTitle("TEST_SEARCH " + brand + " " + model);
        response.setBrand(brand);
        response.setModel(model);
        response.setPrice(BigDecimal.valueOf(25000));
        response.setModelYear(2022);
        response.setMileage(10000);
        response.setDescription("Test car for search functionality");
        response.setApproved(true);
        response.setCreatedAt(LocalDateTime.now());
        
        // Set location details as LocationResponse
        LocationResponse locationResponse = new LocationResponse();
        locationResponse.setId(governorate.getId());
        locationResponse.setDisplayNameEn(governorate.getDisplayNameEn());
        locationResponse.setDisplayNameAr(governorate.getDisplayNameAr());
        locationResponse.setSlug(governorate.getSlug());
        locationResponse.setCountryCode(governorate.getCountryCode());
        locationResponse.setActive(true);
        response.setLocationDetails(locationResponse);
        
        return response;
    }

    // Helper class for Jackson to deserialize Page objects
    public static class RestResponsePage<T> extends org.springframework.data.domain.PageImpl<T> {
        private static final long serialVersionUID = 1L;

        @com.fasterxml.jackson.annotation.JsonCreator(mode = com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES)
        public RestResponsePage(@com.fasterxml.jackson.annotation.JsonProperty("content") List<T> content,
                                @com.fasterxml.jackson.annotation.JsonProperty("number") int number,
                                @com.fasterxml.jackson.annotation.JsonProperty("size") int size,
                                @com.fasterxml.jackson.annotation.JsonProperty("totalElements") Long totalElements,
                                @com.fasterxml.jackson.annotation.JsonProperty("pageable") JsonNode pageableNode, // Consume it, not strictly needed if reconstructing
                                @com.fasterxml.jackson.annotation.JsonProperty("last") boolean last,
                                @com.fasterxml.jackson.annotation.JsonProperty("totalPages") int totalPages,
                                @com.fasterxml.jackson.annotation.JsonProperty("sort") JsonNode sortNode, // Consume it, not strictly needed if reconstructing sort
                                @com.fasterxml.jackson.annotation.JsonProperty("first") boolean first,
                                @com.fasterxml.jackson.annotation.JsonProperty("numberOfElements") int numberOfElements) {
            super(content, org.springframework.data.domain.PageRequest.of(number, size), (totalElements == null) ? 0 : totalElements);
        }

        public RestResponsePage(List<T> content, org.springframework.data.domain.Pageable pageable, long total) {
            super(content, pageable, total);
        }

        public RestResponsePage(List<T> content) {
            super(content);
        }
        
        public RestResponsePage() {
            super(new java.util.ArrayList<>());
        }
    }
}
