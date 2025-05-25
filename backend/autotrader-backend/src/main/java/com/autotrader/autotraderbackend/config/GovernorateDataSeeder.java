package com.autotrader.autotraderbackend.config;

import com.autotrader.autotraderbackend.model.Governorate;
import com.autotrader.autotraderbackend.repository.GovernorateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Seed initial governorate data for Syria.
 * This will only run in development and test environments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
public class GovernorateDataSeeder implements ApplicationRunner {

    private final GovernorateRepository governorateRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (governorateRepository.count() > 0) {
            log.info("Governorates already seeded, skipping...");
            return;
        }

        log.info("Seeding governorates data...");
        
        List<Governorate> governorates = Arrays.asList(
            createGovernorate("Damascus", "دمشق", "damascus", "SY", "Southern Syria", 33.5138, 36.2765),
            createGovernorate("Aleppo", "حلب", "aleppo", "SY", "Northern Syria", 36.2021, 37.1343),
            createGovernorate("Homs", "حمص", "homs", "SY", "Central Syria", 34.7324, 36.7137),
            createGovernorate("Latakia", "اللاذقية", "latakia", "SY", "Coastal Syria", 35.5317, 35.7902),
            createGovernorate("Hama", "حماة", "hama", "SY", "Central Syria", 35.1332, 36.7568),
            createGovernorate("Tartus", "طرطوس", "tartus", "SY", "Coastal Syria", 34.8889, 35.8866),
            createGovernorate("Idlib", "إدلب", "idlib", "SY", "Northern Syria", 35.9306, 36.6339),
            createGovernorate("Daraa", "درعا", "daraa", "SY", "Southern Syria", 32.6189, 36.1055),
            createGovernorate("Deir ez-Zor", "دير الزور", "deir-ez-zor", "SY", "Eastern Syria", 35.3359, 40.1408),
            createGovernorate("Al-Hasakah", "الحسكة", "al-hasakah", "SY", "Northeastern Syria", 36.5024, 40.7563),
            createGovernorate("Raqqa", "الرقة", "raqqa", "SY", "Northern Syria", 35.9528, 39.0089),
            createGovernorate("Al-Suwayda", "السويداء", "al-suwayda", "SY", "Southern Syria", 32.7094, 36.5658),
            createGovernorate("Quneitra", "القنيطرة", "quneitra", "SY", "Southwestern Syria", 33.1254, 35.8240),
            createGovernorate("Damascus Countryside", "ريف دمشق", "damascus-countryside", "SY", "Southern Syria", 33.5138, 36.2765)
        );
        
        governorateRepository.saveAll(governorates);
        log.info("Governorates data seeding completed. Added {} governorates.", governorates.size());
    }
    
    private Governorate createGovernorate(String nameEn, String nameAr, String slug, String countryCode, 
                                        String region, Double latitude, Double longitude) {
        Governorate governorate = new Governorate();
        governorate.setDisplayNameEn(nameEn);
        governorate.setDisplayNameAr(nameAr);
        governorate.setSlug(slug);
        governorate.setCountryCode(countryCode);
        governorate.setRegion(region);
        governorate.setLatitude(latitude);
        governorate.setLongitude(longitude);
        governorate.setIsActive(true);
        return governorate;
    }
}
