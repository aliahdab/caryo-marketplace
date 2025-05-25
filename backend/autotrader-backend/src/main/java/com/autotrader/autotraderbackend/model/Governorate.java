package com.autotrader.autotraderbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a governorate (administrative division) in the system.
 * Supports multilingual names.
 */
@Entity
@Table(name = "governorates", indexes = {
    @Index(name = "idx_governorate_slug", columnList = "slug"),
    @Index(name = "idx_governorate_country_code", columnList = "country_code")
})
@Getter
@Setter
@NoArgsConstructor
public class Governorate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the governorate in English
     */
    @Column(name = "display_name_en", nullable = false, length = 100)
    private String displayNameEn;

    /**
     * The name of the governorate in Arabic
     */
    @Column(name = "display_name_ar", nullable = false, length = 100)
    private String displayNameAr;

    /**
     * URL-friendly slug for the governorate
     */
    @Column(nullable = false, unique = true)
    private String slug;

    /**
     * ISO 3166-1 alpha-2 country code (e.g., "SY" for Syria)
     */
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    /**
     * Optional region grouping (e.g., "Northern Region")
     */
    private String region;

    /**
     * Latitude coordinate of the governorate center
     */
    private Double latitude;

    /**
     * Longitude coordinate of the governorate center
     */
    private Double longitude;

    /**
     * Flag indicating whether the governorate is active in the system
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Returns the display name based on the locale
     * @param isArabic true for Arabic, false for English
     * @return The localized display name
     */
    public String getLocalizedName(boolean isArabic) {
        return isArabic ? displayNameAr : displayNameEn;
    }
}
