package com.autotrader.autotraderbackend.repository.specification;

import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.Governorate;
import com.autotrader.autotraderbackend.model.Location;
import com.autotrader.autotraderbackend.payload.request.ListingFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CarListingSpecification {

    public static Specification<CarListing> fromFilter(ListingFilterRequest filter, Location locationEntity) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by approved status unless explicitly overridden (e.g., for admin views)
            // predicates.add(criteriaBuilder.isTrue(root.get("approved")));
            // Note: We apply the approved filter in the service layer for clarity

            if (StringUtils.hasText(filter.getBrand())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), "%" + filter.getBrand().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getModel())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("model")), "%" + filter.getModel().toLowerCase() + "%"));
            }
            if (filter.getMinYear() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("modelYear"), filter.getMinYear()));
            }
            if (filter.getMaxYear() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("modelYear"), filter.getMaxYear()));
            }
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }
            if (filter.getMinMileage() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("mileage"), filter.getMinMileage()));
            }
            if (filter.getMaxMileage() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("mileage"), filter.getMaxMileage()));
            }

            // Add filter for Location entity if provided
            if (locationEntity != null) {
                predicates.add(criteriaBuilder.equal(root.get("location"), locationEntity));
            }

            // Add filter for governorate if provided
            if (filter.getGovernorateId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("governorate").get("id"), filter.getGovernorateId()));
            }

            // Add filter for isSold status if provided
            if (filter.getIsSold() != null) {
                predicates.add(criteriaBuilder.equal(root.get("sold"), filter.getIsSold()));
            }

            // Add filter for isArchived status if provided
            if (filter.getIsArchived() != null) {
                predicates.add(criteriaBuilder.equal(root.get("archived"), filter.getIsArchived()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Quick search specification using denormalized fields for optimal performance
     */
    public static Specification<CarListing> quickSearch(String searchTerm, Long governorateId, String language) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always include active listings only
            predicates.add(criteriaBuilder.isTrue(root.get("approved")));
            predicates.add(criteriaBuilder.isFalse(root.get("sold")));
            predicates.add(criteriaBuilder.isFalse(root.get("archived")));
            predicates.add(criteriaBuilder.isFalse(root.get("expired")));
            predicates.add(criteriaBuilder.isTrue(root.get("isUserActive")));
            
            // Add governorate filter if provided
            if (governorateId != null) {
                predicates.add(criteriaBuilder.equal(root.get("governorate").get("id"), governorateId));
            }
            
            // Add search term filter if provided
            if (StringUtils.hasText(searchTerm)) {
                String search = "%" + searchTerm.toLowerCase() + "%";
                
                // Use language-specific denormalized fields for better performance
                if ("ar".equals(language)) {
                    predicates.add(
                        criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("brandNameAr")), search),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("modelNameAr")), search),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), search)
                        )
                    );
                } else {
                    // Default to English
                    predicates.add(
                        criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("brandNameEn")), search),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("modelNameEn")), search),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), search)
                        )
                    );
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    // Search by brand using denormalized fields
    public static Specification<CarListing> byBrand(String brand, String language) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(brand)) {
                return criteriaBuilder.conjunction();
            }
            
            String search = "%" + brand.toLowerCase() + "%";
            
            if ("ar".equals(language)) {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("brandNameAr")), search);
            } else {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("brandNameEn")), search);
            }
        };
    }
    
    // Search by model using denormalized fields
    public static Specification<CarListing> byModel(String model, String language) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(model)) {
                return criteriaBuilder.conjunction();
            }
            
            String search = "%" + model.toLowerCase() + "%";
            
            if ("ar".equals(language)) {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("modelNameAr")), search);
            } else {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("modelNameEn")), search);
            }
        };
    }
    
    // Search by governorate using denormalized fields
    public static Specification<CarListing> byGovernorate(String governorate, String language) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(governorate)) {
                return criteriaBuilder.conjunction();
            }
            
            String search = "%" + governorate.toLowerCase() + "%";
            
            if ("ar".equals(language)) {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("governorateNameAr")), search);
            } else {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("governorateNameEn")), search);
            }
        };
    }

    public static Specification<CarListing> isApproved() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("approved"));
    }

    public static Specification<CarListing> isNotSold() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("sold"));
    }

    public static Specification<CarListing> isNotArchived() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("archived"));
    }

    public static Specification<CarListing> isUserActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isUserActive"));
    }
}
