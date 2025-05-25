package com.autotrader.autotraderbackend.repository;

import com.autotrader.autotraderbackend.model.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GovernorateRepository extends JpaRepository<Governorate, Long> {
    
    Optional<Governorate> findBySlug(String slug);
    
    List<Governorate> findByCountryCode(String countryCode);
    
    @Query("SELECT g FROM Governorate g WHERE g.isActive = true ORDER BY g.displayNameEn")
    List<Governorate> findAllActiveOrderByName();
    
    @Query("SELECT g FROM Governorate g WHERE g.isActive = true AND g.countryCode = ?1 ORDER BY g.displayNameEn")
    List<Governorate> findAllActiveByCountryCodeOrderByName(String countryCode);
    
    @Query("SELECT g FROM Governorate g WHERE LOWER(g.displayNameEn) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(g.displayNameAr) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Governorate> findByNameContaining(String name);
}
