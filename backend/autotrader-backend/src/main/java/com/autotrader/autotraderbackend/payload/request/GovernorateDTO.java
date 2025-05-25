package com.autotrader.autotraderbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating a governorate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernorateDTO {
    
    @NotBlank(message = "English name is required")
    @Size(max = 100, message = "English name must not exceed 100 characters")
    private String displayNameEn;
    
    @NotBlank(message = "Arabic name is required")
    @Size(max = 100, message = "Arabic name must not exceed 100 characters")
    private String displayNameAr;
    
    @NotBlank(message = "Slug is required")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;
    
    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be 2 uppercase letters")
    private String countryCode;
    
    private String region;
    
    private Double latitude;
    
    private Double longitude;
    
    @Builder.Default
    private Boolean isActive = true;
}
