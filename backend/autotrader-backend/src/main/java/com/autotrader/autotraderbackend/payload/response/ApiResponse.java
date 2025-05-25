package com.autotrader.autotraderbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * General API response for success/failure operations.
 */
@Data
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
}
