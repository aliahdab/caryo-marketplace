import { 
  FavoriteServiceOptions, 
  FavoriteStatusResponse, 
  UserFavoritesResponse
} from '@/types/favorites';
import { Session } from 'next-auth';

/**
 * Configuration constants for the favorites service
 * @constant {string} API_URL - Base URL for API endpoints
 * @constant {number} MAX_RETRIES - Maximum number of retry attempts for failed operations
 * @constant {number} RETRY_DELAY_BASE - Base delay in milliseconds between retries
 */
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const MAX_RETRIES = 2;
const RETRY_DELAY_BASE = 500; // ms

/**
 * Generic retry function for API operations with exponential backoff
 * @template T - The type of the operation result
 * @param {() => Promise<T>} operation - The async operation to retry
 * @param {(error: unknown) => boolean} errorCheck - Function to determine if an error should trigger a retry
 * @param {number} maxRetries - Maximum number of retry attempts
 * @param {number} baseDelay - Base delay between retries in milliseconds
 * @returns {Promise<T>} - The operation result
 */
async function retryOperation<T>(
  operation: () => Promise<T>,
  errorCheck: (error: unknown) => boolean = () => true,
  maxRetries: number = MAX_RETRIES, 
  baseDelay: number = RETRY_DELAY_BASE
): Promise<T> {
  let lastError: unknown = null;
  
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      return await operation();
    } catch (error) {
      lastError = error;
      
      if (!errorCheck(error)) {
        throw error;
      }
      
      if (attempt < maxRetries) {
        await sleep(baseDelay * Math.pow(2, attempt));
      }
    }
  }
  
  throw lastError;
}

/**
 * Custom error class for favorite-related operations
 * @extends Error
 */
class FavoriteServiceError extends Error {
  constructor(
    message: string,
    public readonly code: string,
    public readonly status?: number
  ) {
    super(message);
    this.name = 'FavoriteServiceError';
  }
}

/**
 * Validates and converts a listing ID string to a number
 * @param {string} listingId - The listing ID to validate
 * @returns {number} - The validated numeric listing ID
 * @throws {FavoriteServiceError} - If the ID is invalid
 */
const validateListingId = (listingId: string): number => {
  const numericId = parseInt(listingId, 10);
  if (isNaN(numericId) || numericId <= 0) {
    throw new FavoriteServiceError(
      'Invalid listing ID provided',
      'INVALID_LISTING_ID'
    );
  }
  return numericId;
};

/**
 * Creates a delay using Promise
 * @param {number} ms - The delay duration in milliseconds
 * @returns {Promise<void>}
 */
const sleep = (ms: number): Promise<void> => 
  new Promise(resolve => setTimeout(resolve, ms));

/**
 * Add a listing to favorites with improved error handling and retry logic
 */
export async function addToFavorites(
  listingId: string, 
  _options?: FavoriteServiceOptions,
  _session?: Session | null
): Promise<void> {
  try {
    const numericId = validateListingId(listingId);
    
    // Import apiRequest for consistent session validation and API calls
    // This will handle authentication validation, token refreshes, and redirects if needed
    const { apiRequest, validateSession } = await import('./auth/session-manager');
    
    // First validate the session to ensure we have a valid token
    const sessionCheck = await validateSession();
    if (!sessionCheck.isValid) {
      throw new FavoriteServiceError(
        'User is not authenticated',
        'UNAUTHORIZED',
        401
      );
    }
    
    const url = `${API_URL}/api/favorites/${numericId}`;
    
    // Use retryOperation for cleaner retry logic
    await retryOperation(
      async () => {
        const response = await apiRequest(url, { method: 'POST' });
        
        if (response.ok) {
          return; // Success
        }
        
        const errorText = await response.text();
        
        // Handle specific server errors
        if (response.status === 500) {
          const { isHibernateSerializationError } = await import('./error-handlers');
          if (isHibernateSerializationError(errorText)) {
            // Verify if operation actually succeeded despite error
            const actualState = await isFavorited(listingId);
            if (actualState.isFavorite) {
              return; // Operation succeeded despite error
            }
          }
        }
        
        throw new FavoriteServiceError(
          `Failed to add favorite: ${errorText}`,
          'API_ERROR',
          response.status
        );
      },
      // Only retry certain errors
      (error) => {
        // Don't retry unauthorized errors
        if (isAuthenticationError(error)) return false;
        
        // Don't retry validation errors
        if (error instanceof FavoriteServiceError && error.code === 'INVALID_LISTING_ID') return false;
        
        // Retry all other errors
        return true;
      }
    );
    
    // Final verification
    const actualState = await isFavorited(listingId);
    if (!actualState.isFavorite) {
      throw new FavoriteServiceError(
        'Operation completed but favorite was not added',
        'VERIFICATION_FAILED'
      );
    }
    
  } catch (error) {
    // Handle authentication errors
    if (isAuthenticationError(error)) {
      throw new FavoriteServiceError(
        'Authentication required',
        'UNAUTHORIZED',
        401
      );
    }
    
    // Rethrow FavoriteServiceErrors
    if (error instanceof FavoriteServiceError) {
      throw error;
    }
    
    // Wrap all other errors
    throw new FavoriteServiceError(
      `Failed to add favorite: ${error instanceof Error ? error.message : String(error)}`,
      'UNKNOWN_ERROR'
    );
  }
}

/**
 * Remove a listing from favorites with improved error handling and retry logic
 */
export async function removeFromFavorites(
  listingId: string,
  _options?: FavoriteServiceOptions,
  _session?: Session | null
): Promise<void> {
  try {
    const numericId = validateListingId(listingId);
    
    // Import apiRequest for consistent session validation and API calls
    const { apiRequest, validateSession } = await import('./auth/session-manager');
    
    // First validate the session to ensure we have a valid token
    const sessionCheck = await validateSession();
    if (!sessionCheck.isValid) {
      throw new FavoriteServiceError(
        'User is not authenticated',
        'UNAUTHORIZED',
        401
      );
    }
    
    const url = `${API_URL}/api/favorites/${numericId}`;
    
    // Use retryOperation for cleaner retry logic
    await retryOperation(
      async () => {
        const response = await apiRequest(url, { method: 'DELETE' });
        
        if (response.ok) {
          return; // Success
        }
        
        const errorText = await response.text();
        
        // Handle specific server errors
        if (response.status === 500) {
          const { isHibernateSerializationError } = await import('./error-handlers');
          if (isHibernateSerializationError(errorText)) {
            // Verify if operation actually succeeded despite error
            const actualState = await isFavorited(listingId);
            if (!actualState.isFavorite) {
              return; // Operation succeeded despite error
            }
          }
        }
        
        throw new FavoriteServiceError(
          `Failed to remove favorite: ${errorText}`,
          'API_ERROR',
          response.status
        );
      },
      // Only retry certain errors
      (error) => {
        // Don't retry unauthorized errors
        if (isAuthenticationError(error)) return false;
        
        // Don't retry validation errors
        if (error instanceof FavoriteServiceError && error.code === 'INVALID_LISTING_ID') return false;
        
        // Retry all other errors
        return true;
      }
    );
    
    // Final verification
    const actualState = await isFavorited(listingId);
    if (actualState.isFavorite) {
      throw new FavoriteServiceError(
        'Operation completed but favorite was not removed',
        'VERIFICATION_FAILED'
      );
    }
    
  } catch (error) {
    // Handle authentication errors
    if (isAuthenticationError(error)) {
      throw new FavoriteServiceError(
        'Authentication required',
        'UNAUTHORIZED',
        401
      );
    }
    
    // Rethrow FavoriteServiceErrors
    if (error instanceof FavoriteServiceError) {
      throw error;
    }
    
    // Wrap all other errors
    throw new FavoriteServiceError(
      `Failed to remove favorite: ${error instanceof Error ? error.message : String(error)}`,
      'UNKNOWN_ERROR'
    );
  }
}

/**
 * Get user's favorite listings with better response handling
 */
export async function getUserFavorites(
  _options?: FavoriteServiceOptions,
  _session?: Session | null
): Promise<UserFavoritesResponse> {
  try {
    // Import apiRequest for consistent session validation and API calls
    const { apiRequest, validateSession } = await import('./auth/session-manager');
    
    // First validate the session to ensure we have a valid token
    const sessionCheck = await validateSession();
    if (!sessionCheck.isValid) {
      throw new FavoriteServiceError(
        'User is not authenticated',
        'UNAUTHORIZED',
        401
      );
    }
    
    const url = `${API_URL}/api/favorites`;
    
    try {
      const response = await apiRequest(url, { 
        method: 'GET' 
      });
      
      if (!response.ok) {
        throw new FavoriteServiceError(
          `Failed to fetch favorites: ${response.status}`,
          'API_ERROR',
          response.status
        );
      }
      
      const text = await response.text();
      
      // Handle empty response
      if (!text?.trim()) {
        return { favorites: [], total: 0 };
      }
      
      return parseUserFavoritesResponse(text);
    } catch (apiError) {
      // Check if it's an authentication error and rethrow with appropriate code
      if (isAuthenticationError(apiError)) {
        throw new FavoriteServiceError(
          'Authentication required',
          'UNAUTHORIZED',
          401
        );
      }
      
      throw new FavoriteServiceError(
        apiError instanceof Error ? apiError.message : String(apiError),
        'API_ERROR'
      );
    }
    
  } catch (error) {
    // If it's already a FavoriteServiceError, just rethrow
    if (error instanceof FavoriteServiceError) {
      throw error;
    }
    
    // For generic errors
    throw new FavoriteServiceError(
      'An unexpected error occurred while fetching favorites',
      'UNKNOWN_ERROR'
    );
  }
}

/**
 * Parse various formats of user favorites response
 */
const parseUserFavoritesResponse = (text: string): UserFavoritesResponse => {
  try {
    const data = JSON.parse(text);
    
    // Handle array response
    if (Array.isArray(data)) {
      return { favorites: data, total: data.length };
    }
    
    // Handle object with favorites array
    if (data?.favorites && Array.isArray(data.favorites)) {
      return { 
        favorites: data.favorites, 
        total: data.total ?? data.favorites.length 
      };
    }
    
    // Handle object with data array
    if (data?.data && Array.isArray(data.data)) {
      return { 
        favorites: data.data, 
        total: data.total ?? data.data.length 
      };
    }
    
    // Default fallback
    return { favorites: [], total: 0 };
    
  } catch (parseError) {
    console.error('[FAVORITES] Error parsing favorites response:', parseError);
    return { favorites: [], total: 0 };
  }
};

/**
 * Check if a listing is favorited by the current user with better error handling
 */
export async function isFavorited(
  listingId: string,
  _options?: FavoriteServiceOptions,
  _session?: Session | null
): Promise<FavoriteStatusResponse> {
  const baseResponse = { listingId };
  
  try {
    const numericId = validateListingId(listingId);
    
    // Import the apiRequest utility
    const { apiRequest, validateSession } = await import('./auth/session-manager');
    
    // First validate the session to ensure we have a valid token
    const sessionCheck = await validateSession();
    if (!sessionCheck.isValid) {
      // For this function, we'll just return not favorited if the session is invalid
      // rather than redirecting or throwing an error
      return { ...baseResponse, isFavorite: false };
    }
    
    const url = `${API_URL}/api/favorites/check/${numericId}`;
    
    try {
      // Use the apiRequest utility which handles session validation automatically
      const response = await apiRequest(url, {
        method: 'GET'
      });
      
      if (!response.ok) {
        if (response.status !== 404) {
          console.warn(`[FAVORITES] Failed to check favorite status: ${response.status}`);
        }
        return { ...baseResponse, isFavorite: false };
      }
      
      const text = await response.text();
      const isFavorite = parseFavoriteStatusResponse(text);
      
      return { ...baseResponse, isFavorite };
    } catch (apiError) {
      // API request failed, likely due to authentication issues
      // The apiRequest utility will have already handled redirects if needed
      console.warn('[FAVORITES] API request failed in isFavorited:', apiError);
      return { ...baseResponse, isFavorite: false };
    }
    
  } catch (error) {
    // Log error but don't throw - gracefully return false
    if (error instanceof FavoriteServiceError && error.code === 'INVALID_LISTING_ID') {
      console.error('[FAVORITES] Invalid listing ID:', listingId);
    } else {
      console.warn('[FAVORITES] Error checking favorite status:', error);
    }
    
    return { ...baseResponse, isFavorite: false };
  }
}

/**
 * Parse various formats of favorite status response
 */
const parseFavoriteStatusResponse = (text: string): boolean => {
  // Handle boolean string responses
  if (text === 'true') return true;
  if (text === 'false') return false;
  
  // Try to parse as JSON
  try {
    const data = JSON.parse(text);
    
    // Handle boolean response
    if (typeof data === 'boolean') {
      return data;
    }
    
    // Handle object with isFavorite property
    if (data && typeof data.isFavorite === 'boolean') {
      return data.isFavorite;
    }
    
    // Handle object with favorited property
    if (data && typeof data.favorited === 'boolean') {
      return data.favorited;
    }
    
    // Default to false for unknown formats
    return false;
    
  } catch {
    // If we can't parse, default to false
    return false;
  }
};

/**
 * Utility function to detect if an error is related to authentication
 */
export function isAuthenticationError(error: unknown): boolean {
  if (!error) return false;
  
  // Check for FavoriteServiceError with UNAUTHORIZED code
  if (error instanceof FavoriteServiceError && error.code === 'UNAUTHORIZED') {
    return true;
  }
  
  // Check error message for authentication-related keywords
  if (error instanceof Error) {
    const errorMessage = error.message.toLowerCase();
    return (
      errorMessage.includes('unauthorized') ||
      errorMessage.includes('authentication') ||
      errorMessage.includes('auth') ||
      errorMessage.includes('login') ||
      errorMessage.includes('401')
    );
  }
  
  // For other types of errors, check string representation
  const errorStr = String(error).toLowerCase();
  return (
    errorStr.includes('unauthorized') ||
    errorStr.includes('authentication') ||
    errorStr.includes('auth') ||
    errorStr.includes('login') ||
    errorStr.includes('401')
  );
}