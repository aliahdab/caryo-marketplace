package com.autotrader.autotraderbackend.repository;

import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarListingRepository extends JpaRepository<CarListing, Long>, JpaSpecificationExecutor<CarListing> {
    
    // Find all approved listings with pagination
    Page<CarListing> findByApprovedTrue(Pageable pageable);
    
    // Find by id and approved
    Optional<CarListing> findByIdAndApprovedTrue(Long id);
    
    // Find by various criteria with pagination
    Page<CarListing> findByBrandNameEnAndApprovedTrue(String brandNameEn, Pageable pageable);
    
    Page<CarListing> findByModelAndApprovedTrue(String model, Pageable pageable);
    
    Page<CarListing> findByModelYearAndApprovedTrue(Integer modelYear, Pageable pageable);
    
    Page<CarListing> findByPriceBetweenAndApprovedTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Find listings by seller
    List<CarListing> findBySeller(User seller);
    
    // Find listings pending approval
    Page<CarListing> findByApprovedFalse(Pageable pageable);
}
