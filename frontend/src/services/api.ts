'use client';

import { CarMake, CarModel, CarTrim } from '@/types/car';
import { ApiError } from '@/utils/apiErrorHandler';

// Base URL for the API - will be set from environment variables
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

type RequestOptions = {
  method: string;
  headers: Record<string, string>;
  body?: string;
  timeout?: number; // Request timeout in ms
  credentials?: RequestCredentials;
  mode?: RequestMode;
};

// Define a type for request data
type RequestData = Record<string, unknown> | unknown[] | null | undefined;

/**
 * Generic function to make API requests
 */
async function apiRequest<T>(
  endpoint: string, 
  method: string, 
  data?: RequestData, 
  customHeaders?: Record<string, string>,
  timeout: number = 15000 // Default timeout: 15 seconds
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  console.log(`Making ${method} request to: ${url}`);
  
  const options: RequestOptions = {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...customHeaders,
    },
    mode: 'cors',
    credentials: 'include',
    timeout
  };

  if (data) {
    options.body = JSON.stringify(data);
  }

  try {
    // Create a controller to handle timeout
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);
    
    console.log('Request options:', JSON.stringify(options, null, 2));
    
    const response = await fetch(url, {
      ...options,
      signal: controller.signal,
      cache: 'no-store'
    });
    
    clearTimeout(timeoutId);
    
    console.log('Response status:', response.status);
    console.log('Response headers:', Object.fromEntries(response.headers.entries()));
    
    // Parse the response
    let responseData: unknown;
    
    // Try to parse as JSON first
    const contentType = response.headers.get('content-type');
    console.log('Response content-type:', contentType);
    
    if (contentType && contentType.includes('application/json')) {
      responseData = await response.json();
      console.log('Parsed JSON response:', responseData);
    } else {
      responseData = await response.text();
      console.log('Text response:', responseData);
    }

    // Check for errors
    if (!response.ok) {
      // Create an ApiError with status code and response data
      const errorMessage = typeof responseData === 'string' 
        ? responseData 
        : (responseData as { message?: string })?.message || `Error ${response.status}: Request failed`;
        
      console.error('API error:', { status: response.status, message: errorMessage });
      throw new ApiError(errorMessage, response.status, responseData);
    }

    return responseData as T;
  } catch (error) {
    console.error('Request failed:', error);
    
    // Handle timeout/abort errors explicitly
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new ApiError('Request timed out', 0);
    }
    
    // Re-throw ApiErrors
    if (error instanceof ApiError) {
      throw error;
    }
    
    // Convert other errors to ApiError
    throw new ApiError(
      error instanceof Error ? error.message : 'Unknown error occurred',
      0
    );
  }
}

// Export common request methods
export const api = {
  get: <T>(endpoint: string, customHeaders?: Record<string, string>, timeout?: number) => 
    apiRequest<T>(endpoint, 'GET', undefined, customHeaders, timeout),
  
  post: <T>(endpoint: string, data: RequestData, customHeaders?: Record<string, string>, timeout?: number) => 
    apiRequest<T>(endpoint, 'POST', data, customHeaders, timeout),
  
  put: <T>(endpoint: string, data: RequestData, customHeaders?: Record<string, string>, timeout?: number) => 
    apiRequest<T>(endpoint, 'PUT', data, customHeaders, timeout),
  
  delete: <T>(endpoint: string, customHeaders?: Record<string, string>) => 
    apiRequest<T>(endpoint, 'DELETE', undefined, customHeaders),
};

/**
 * Fetches all car brands
 */
export async function fetchCarBrands(): Promise<CarMake[]> {
  console.log('Fetching car brands from API');
  return api.get<CarMake[]>('/api/reference-data/brands');
}

/**
 * Fetches models for a specific brand
 */
export async function fetchCarModels(brandId: number): Promise<CarModel[]> {
  console.log(`Fetching car models for brand ${brandId} from API`);
  return api.get<CarModel[]>(`/api/reference-data/brands/${brandId}/models`);
}

/**
 * Fetches trims for a specific model
 */
export async function fetchCarTrims(brandId: number, modelId: number): Promise<CarTrim[]> {
  console.log(`Fetching car trims for brand ${brandId} model ${modelId} from API`);
  return api.get<CarTrim[]>(`/api/reference-data/brands/${brandId}/models/${modelId}/trims`);
}
