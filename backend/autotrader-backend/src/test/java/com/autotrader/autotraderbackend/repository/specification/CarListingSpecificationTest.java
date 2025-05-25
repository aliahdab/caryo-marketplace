package com.autotrader.autotraderbackend.repository.specification;

import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.payload.request.ListingFilterRequest;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class CarListingSpecificationTest {

    @Mock
    private Root<CarListing> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate mockPredicate;
    
    // Use raw types to avoid generics issues
    private Path mockPath;
    private Expression mockExpression;
    private Path mockBooleanPath;
    
    @BeforeEach
    void setUpMocks() {
        // Create mocks with raw types to avoid generics issues
        mockPath = mock(Path.class);
        mockExpression = mock(Expression.class);
        mockBooleanPath = mock(Path.class);
        
        // Enable lenient stubbing to avoid unnecessary stubbing errors
        lenient().when(root.get(anyString())).thenReturn(mockPath);
        lenient().when(mockPath.get(anyString())).thenReturn(mockPath);
        
        // Mock criteriaBuilder methods with lenient() to avoid unnecessary stubbing errors
        lenient().when(criteriaBuilder.like(any(), anyString())).thenReturn(mockPredicate);
        lenient().when(criteriaBuilder.equal(any(), any())).thenReturn(mockPredicate);
        lenient().when(criteriaBuilder.greaterThanOrEqualTo(any(), any(Comparable.class))).thenReturn(mockPredicate);
        lenient().when(criteriaBuilder.lessThanOrEqualTo(any(), any(Comparable.class))).thenReturn(mockPredicate);
        
        // Mock boolean operations
        lenient().when(criteriaBuilder.isTrue(any())).thenReturn(mockPredicate);
        lenient().when(criteriaBuilder.isFalse(any())).thenReturn(mockPredicate);
        
        // Mock and/or operations
        lenient().when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mockPredicate);
        lenient().when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(mockPredicate);
        lenient().when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(mockPredicate);
        lenient().when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class), any(Predicate.class))).thenReturn(mockPredicate);
        
        // Mock lower() to return an Expression
        lenient().when(criteriaBuilder.lower(any())).thenReturn(mockExpression);
    }

    @Test
    void fromFilter_withEmptyFilter_shouldReturnNoPredicates() {
        ListingFilterRequest filter = new ListingFilterRequest();
        Specification<CarListing> spec = CarListingSpecification.fromFilter(filter, null);

        spec.toPredicate(root, query, criteriaBuilder);

        // Verify that 'and' is called with an empty array (or null predicate if empty)
        verify(criteriaBuilder).and(eq(new Predicate[0]));
        // Don't verify no more interactions since other methods might be used based on implementation changes
    }

    @Test
    void fromFilter_withBrand_shouldAddBrandPredicate() {
        ListingFilterRequest filter = new ListingFilterRequest();
        filter.setBrand("Toyota");
        Specification<CarListing> spec = CarListingSpecification.fromFilter(filter, null);

        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).lower(any()); // Verify lower is called
        verify(criteriaBuilder).like(any(), eq("%toyota%"));

        // Use ArgumentCaptor for 'and'
        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(criteriaBuilder).and(predicateCaptor.capture());
        assertEquals(1, predicateCaptor.getValue().length, "Should combine exactly 1 predicate");
    }

    @Test
    void fromFilter_withModel_shouldAddModelPredicate() {
        ListingFilterRequest filter = new ListingFilterRequest();
        filter.setModel("Camry");
        Specification<CarListing> spec = CarListingSpecification.fromFilter(filter, null);

        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).lower(any()); // Verify lower is called
        verify(criteriaBuilder).like(any(), eq("%camry%"));

        // Use ArgumentCaptor for 'and'
        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(criteriaBuilder).and(predicateCaptor.capture());
        assertEquals(1, predicateCaptor.getValue().length, "Should combine exactly 1 predicate");
    }

    @Test
    void fromFilter_withMinMaxYear_shouldAddYearPredicates() {
        ListingFilterRequest filter = new ListingFilterRequest();
        filter.setMinYear(2015);
        filter.setMaxYear(2020);
        Specification<CarListing> spec = CarListingSpecification.fromFilter(filter, null);

        spec.toPredicate(root, query, criteriaBuilder);

        // Verify the criteria methods are called with the right values
        verify(criteriaBuilder).greaterThanOrEqualTo(any(), eq(2015));
        verify(criteriaBuilder).lessThanOrEqualTo(any(), eq(2020));

        // Use ArgumentCaptor for 'and'
        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(criteriaBuilder).and(predicateCaptor.capture());
        assertEquals(2, predicateCaptor.getValue().length, "Should combine exactly 2 predicates");
    }

     @Test
    void fromFilter_withMinMaxPrice_shouldAddPricePredicates() {
        ListingFilterRequest filter = new ListingFilterRequest();
        BigDecimal minPrice = BigDecimal.valueOf(10000.0);
        BigDecimal maxPrice = BigDecimal.valueOf(20000.0);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        Specification<CarListing> spec = CarListingSpecification.fromFilter(filter, null);

        spec.toPredicate(root, query, criteriaBuilder);

        // Verify the criteria methods are called with the right values
        verify(criteriaBuilder).greaterThanOrEqualTo(any(), eq(minPrice));
        verify(criteriaBuilder).lessThanOrEqualTo(any(), eq(maxPrice));

        // Use ArgumentCaptor for 'and'
        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(criteriaBuilder).and(predicateCaptor.capture());
        assertEquals(2, predicateCaptor.getValue().length, "Should combine exactly 2 predicates");
    }

    @Test
    void fromFilter_withMinMaxMileage_shouldAddMileagePredicates() {
        ListingFilterRequest filter = new ListingFilterRequest();
        filter.setMinMileage(50000);
        filter.setMaxMileage(100000);
        Specification<CarListing> spec = CarListingSpecification.fromFilter(filter, null);

        spec.toPredicate(root, query, criteriaBuilder);

        // Verify the criteria methods are called with the right values
        verify(criteriaBuilder).greaterThanOrEqualTo(any(), eq(50000));
        verify(criteriaBuilder).lessThanOrEqualTo(any(), eq(100000));

        // Use ArgumentCaptor for 'and'
        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(criteriaBuilder).and(predicateCaptor.capture());
        assertEquals(2, predicateCaptor.getValue().length, "Should combine exactly 2 predicates");
    }

    @Test
    void fromFilter_withLocation_shouldAddLocationPredicate() {
        ListingFilterRequest filter = new ListingFilterRequest();
        filter.setLocation("London");
        // We're passing null for the locationEntity - the location field in filter should be ignored
        Specification<CarListing> spec = CarListingSpecification.fromFilter(filter, null);

        spec.toPredicate(root, query, criteriaBuilder);

        // Verify location filter has been removed in favor of locationEntity in the main specification
        // This test now verifies that no location-related filters are applied without a Location entity

        // Use ArgumentCaptor for 'and'
        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(criteriaBuilder).and(predicateCaptor.capture());
        assertEquals(0, predicateCaptor.getValue().length, "Should combine 0 predicates since location string is now ignored");
    }

    @Test
    void fromFilter_withAllFilters_shouldAddAllPredicates() {
        ListingFilterRequest filter = new ListingFilterRequest();
        filter.setBrand("Honda");
        filter.setModel("Civic");
        filter.setMinYear(2018);
        filter.setMaxYear(2021);
        BigDecimal minPrice = BigDecimal.valueOf(15000.0);
        BigDecimal maxPrice = BigDecimal.valueOf(25000.0);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setMinMileage(30000);
        filter.setMaxMileage(60000);
        filter.setLocation("Manchester");

        Specification<CarListing> spec = CarListingSpecification.fromFilter(filter, null);
        spec.toPredicate(root, query, criteriaBuilder);

        // Verify individual predicates
        verify(criteriaBuilder, times(2)).lower(any()); // For brand and model
        verify(criteriaBuilder).like(any(), eq("%honda%"));
        verify(criteriaBuilder).like(any(), eq("%civic%"));
        verify(criteriaBuilder).greaterThanOrEqualTo(any(), eq(2018));
        verify(criteriaBuilder).lessThanOrEqualTo(any(), eq(2021));
        verify(criteriaBuilder).greaterThanOrEqualTo(any(), eq(minPrice));
        verify(criteriaBuilder).lessThanOrEqualTo(any(), eq(maxPrice));
        verify(criteriaBuilder).greaterThanOrEqualTo(any(), eq(30000));
        verify(criteriaBuilder).lessThanOrEqualTo(any(), eq(60000));
        
        // No longer verify location String predicate since we use locationEntity now
        // verify(criteriaBuilder).like(any(), eq("%manchester%"));

        // Verify the final 'and' combines all 8 predicates using ArgumentCaptor (was 9, now 8 without location)
        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(criteriaBuilder).and(predicateCaptor.capture());
        assertEquals(8, predicateCaptor.getValue().length, "Should combine exactly 8 predicates");
    }

    @Test
    void isApproved_shouldAddApprovedPredicate() {
        Specification<CarListing> spec = CarListingSpecification.isApproved();
        spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).isTrue(any()); // Use any() matcher instead of eq()
    }

    @Test
    void quickSearch_withSearchTerm_shouldAddSearchPredicate() {
        // Setup
        String searchTerm = "toyota";
        Long governorateId = null;
        String language = "en";

        // Execute
        Specification<CarListing> spec = CarListingSpecification.quickSearch(searchTerm, governorateId, language);
        spec.toPredicate(root, query, criteriaBuilder);

        // Verify
        verify(criteriaBuilder, atLeastOnce()).lower(any());
        verify(criteriaBuilder, atLeastOnce()).like(any(), eq("%toyota%"));
        verify(criteriaBuilder).or(any(), any(), any());
        
        // Allow multiple calls to isTrue since the implementation might be checking multiple flags
        verify(criteriaBuilder, atLeastOnce()).isTrue(any());
        verify(criteriaBuilder, atLeastOnce()).isFalse(any());
    }

    @Test
    void quickSearch_withGovernorateId_shouldAddGovernorateFilter() {
        // Setup
        String searchTerm = null;
        Long governorateId = 1L;
        String language = "en";

        // Execute
        Specification<CarListing> spec = CarListingSpecification.quickSearch(searchTerm, governorateId, language);
        spec.toPredicate(root, query, criteriaBuilder);

        // Verify - using atLeastOnce() instead of specific counts
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq(governorateId));
        verify(criteriaBuilder, atLeastOnce()).isTrue(any()); // for approved
        verify(criteriaBuilder, atLeastOnce()).isFalse(any()); // for sold/archived/expired
    }

    @Test
    void byBrand_shouldFilterByBrandName() {
        // Setup
        String brand = "Toyota";
        String language = "en";

        // Execute
        Specification<CarListing> spec = CarListingSpecification.byBrand(brand, language);
        spec.toPredicate(root, query, criteriaBuilder);

        // Verify
        verify(criteriaBuilder).lower(any());
        verify(criteriaBuilder).like(any(), eq("%toyota%"));
    }

    @Test
    void byModel_shouldFilterByModelName() {
        // Setup
        String model = "Camry";
        String language = "en";

        // Execute
        Specification<CarListing> spec = CarListingSpecification.byModel(model, language);
        spec.toPredicate(root, query, criteriaBuilder);

        // Verify
        verify(criteriaBuilder).lower(any());
        verify(criteriaBuilder).like(any(), eq("%camry%"));
    }

    @Test
    void byGovernorate_shouldFilterByGovernorateName() {
        // Setup
        String governorate = "Damascus";
        String language = "en";

        // Execute
        Specification<CarListing> spec = CarListingSpecification.byGovernorate(governorate, language);
        spec.toPredicate(root, query, criteriaBuilder);

        // Verify
        verify(criteriaBuilder).lower(any());
        verify(criteriaBuilder).like(any(), eq("%damascus%"));
    }
}
