package com.autotrader.autotraderbackend.payload.response;

import com.autotrader.autotraderbackend.model.Governorate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Governorate entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernorateResponse {
    
    private Long id;
    private String displayNameEn;
    private String displayNameAr;
    private String slug;
    private String countryCode;
    private String region;
    
    /**
     * Convert a Governorate entity to a GovernorateResponse DTO.
     * 
     * @param governorate The governorate entity to convert
     * @return GovernorateResponse DTO
     */
    public static GovernorateResponse fromEntity(Governorate governorate) {
        if (governorate == null) {
            return null;
        }
        
        return GovernorateResponse.builder()
                .id(governorate.getId())
                .displayNameEn(governorate.getDisplayNameEn())
                .displayNameAr(governorate.getDisplayNameAr())
                .slug(governorate.getSlug())
                .countryCode(governorate.getCountryCode())
                .region(governorate.getRegion())
                .build();
    }
}
