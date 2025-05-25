package com.autotrader.autotraderbackend.controller;

import com.autotrader.autotraderbackend.payload.response.CarListingResponse;
import com.autotrader.autotraderbackend.payload.response.QuickSearchResponse;
import com.autotrader.autotraderbackend.service.CarListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search API", description = "API endpoints for searching car listings")
public class SearchController {

    private final CarListingService carListingService;

    @GetMapping("/quick")
    @Operation(summary = "Quick search for car listings",
               description = "Search for car listings by term and governorate with optimal performance")
    public ResponseEntity<QuickSearchResponse> quickSearch(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Long governorateId,
            @RequestParam(defaultValue = "en") String language,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Quick search request with term: '{}', governorateId: {}, language: {}", 
                term, governorateId, language);
        
        Page<CarListingResponse> listingsPage = carListingService.quickSearch(term, governorateId, language, pageable);
        
        QuickSearchResponse response = QuickSearchResponse.builder()
                .listings(listingsPage.getContent())
                .totalElements(listingsPage.getTotalElements())
                .totalPages(listingsPage.getTotalPages())
                .currentPage(listingsPage.getNumber())
                .pageSize(listingsPage.getSize())
                .hasNext(listingsPage.hasNext())
                .hasPrevious(listingsPage.hasPrevious())
                .searchTerm(term)
                .governorateId(governorateId)
                .language(language)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-brand")
    @Operation(summary = "Search car listings by brand",
               description = "Search for car listings by brand name with optimal performance")
    public ResponseEntity<Page<CarListingResponse>> searchByBrand(
            @RequestParam String brand,
            @RequestParam(defaultValue = "en") String language,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Brand search request for: '{}', language: {}", brand, language);
        
        Page<CarListingResponse> listings = carListingService.searchByBrand(brand, language, pageable);
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/by-model")
    @Operation(summary = "Search car listings by model",
               description = "Search for car listings by model name with optimal performance")
    public ResponseEntity<Page<CarListingResponse>> searchByModel(
            @RequestParam String model,
            @RequestParam(defaultValue = "en") String language,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Model search request for: '{}', language: {}", model, language);
        
        Page<CarListingResponse> listings = carListingService.searchByModel(model, language, pageable);
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/by-governorate")
    @Operation(summary = "Search car listings by governorate",
               description = "Search for car listings by governorate name with optimal performance")
    public ResponseEntity<Page<CarListingResponse>> searchByGovernorate(
            @RequestParam String governorate,
            @RequestParam(defaultValue = "en") String language,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Governorate search request for: '{}', language: {}", governorate, language);
        
        Page<CarListingResponse> listings = carListingService.searchByGovernorate(governorate, language, pageable);
        return ResponseEntity.ok(listings);
    }
}
