package com.autotrader.autotraderbackend.service;

import com.autotrader.autotraderbackend.model.Governorate;
import com.autotrader.autotraderbackend.repository.GovernorateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GovernorateService {

    private final GovernorateRepository governorateRepository;

    /**
     * Get all active governorates.
     */
    @Transactional(readOnly = true)
    public List<Governorate> getAllActiveGovernorates() {
        return governorateRepository.findAllActiveOrderByName();
    }

    /**
     * Get all active governorates by country code.
     */
    @Transactional(readOnly = true)
    public List<Governorate> getGovernoratesByCountryCode(String countryCode) {
        return governorateRepository.findAllActiveByCountryCodeOrderByName(countryCode);
    }

    /**
     * Get governorate by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Governorate> getGovernorateById(Long id) {
        return governorateRepository.findById(id);
    }

    /**
     * Get governorate by slug.
     */
    @Transactional(readOnly = true)
    public Optional<Governorate> getGovernorateBySlug(String slug) {
        return governorateRepository.findBySlug(slug);
    }

    /**
     * Search governorates by name.
     */
    @Transactional(readOnly = true)
    public List<Governorate> searchGovernoratesByName(String searchTerm) {
        return governorateRepository.findByNameContaining(searchTerm);
    }

    /**
     * Create a new governorate.
     */
    @Transactional
    public Governorate createGovernorate(Governorate governorate) {
        log.info("Creating new governorate with slug: {}", governorate.getSlug());
        return governorateRepository.save(governorate);
    }

    /**
     * Update an existing governorate.
     */
    @Transactional
    public Optional<Governorate> updateGovernorate(Long id, Governorate governorateDetails) {
        log.info("Updating governorate with ID: {}", id);
        return governorateRepository.findById(id)
                .map(governorate -> {
                    governorate.setDisplayNameEn(governorateDetails.getDisplayNameEn());
                    governorate.setDisplayNameAr(governorateDetails.getDisplayNameAr());
                    governorate.setSlug(governorateDetails.getSlug());
                    governorate.setCountryCode(governorateDetails.getCountryCode());
                    governorate.setRegion(governorateDetails.getRegion());
                    governorate.setLatitude(governorateDetails.getLatitude());
                    governorate.setLongitude(governorateDetails.getLongitude());
                    governorate.setIsActive(governorateDetails.getIsActive());
                    return governorateRepository.save(governorate);
                });
    }

    /**
     * Delete a governorate by ID.
     */
    @Transactional
    public boolean deleteGovernorate(Long id) {
        log.info("Deleting governorate with ID: {}", id);
        return governorateRepository.findById(id)
                .map(governorate -> {
                    governorateRepository.delete(governorate);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Activate a governorate by ID.
     */
    @Transactional
    public Optional<Governorate> activateGovernorate(Long id) {
        log.info("Activating governorate with ID: {}", id);
        return governorateRepository.findById(id)
                .map(governorate -> {
                    governorate.setIsActive(true);
                    return governorateRepository.save(governorate);
                });
    }

    /**
     * Deactivate a governorate by ID.
     */
    @Transactional
    public Optional<Governorate> deactivateGovernorate(Long id) {
        log.info("Deactivating governorate with ID: {}", id);
        return governorateRepository.findById(id)
                .map(governorate -> {
                    governorate.setIsActive(false);
                    return governorateRepository.save(governorate);
                });
    }
}
