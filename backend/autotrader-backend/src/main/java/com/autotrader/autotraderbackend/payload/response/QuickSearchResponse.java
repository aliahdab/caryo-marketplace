package com.autotrader.autotraderbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response object for the quick search API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickSearchResponse {
    
    private List<CarListingResponse> listings;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    
    /**
     * Search metadata
     */
    private String searchTerm;
    private Long governorateId;
    private String language;
}
