import { 
  FavoriteServiceOptions, 
  FavoriteStatusResponse, 
  UserFavoritesResponse
} from '@/types/favorites';
import { getSession } from 'next-auth/react';
import { Session } from 'next-auth';

// Constants
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const MAX_RETRIES = 2;
const RETRY_DELAY_BASE = 500; // ms

// Error types for better error handling
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

// Utility functions
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

const validateSession = (session: Session | null): string => {
  if (!session?.accessToken) {
    throw new FavoriteServiceError(
      'UNAUTHORIZED',
      'UNAUTHORIZED'
    );
  }
  return session.accessToken;
};

const sleep = (ms: number): Promise<void> => 
  new Promise(resolve => setTimeout(resolve, ms));

const createApiHeaders = (token: string): Record<string, string> => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${token}`
});

const makeApiRequest = async (
  url: string,
  method: 'GET' | 'POST' | 'DELETE',
  token: string
): Promise<Response> => {
  return fetch(url, {
    method,
    headers: createApiHeaders(token),
    credentials: 'include'
  });
};

/**
 * Add a listing to favorites with improved error handling and retry logic
 */
export async function addToFavorites(
  listingId: string, 
  options?: FavoriteServiceOptions,
  session?: Session | null
): Promise<void> {
  try {
    const numericId = validateListingId(listingId);
    const currentSession = session || await getSession();
    const token = validateSession(currentSession);
    
    const url = `${API_URL}/api/favorites/${numericId}`;
    
    // Retry logic with exponential backoff
    let lastError: Error | null = null;
    
    for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      try {
        const response = await makeApiRequest(url, 'POST', token);
        
        if (response.ok) {
          return; // Success
        }
        
        const errorText = await response.text();
        
        // Handle specific server errors
        if (response.status === 500) {
          const { isHibernateSerializationError } = await import('./error-handlers');
          if (isHibernateSerializationError(errorText)) {
            // Verify if operation actually succeeded despite error
            const actualState = await isFavorited(listingId, options, currentSession);
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
        
      } catch (error) {
        lastError = error instanceof Error ? error : new Error(String(error));
        
        // Don't retry on the last attempt
        if (attempt < MAX_RETRIES) {
          await sleep(RETRY_DELAY_BASE * Math.pow(2, attempt));
        }
      }
    }
    
    // Handle exhausted retries
    if (lastError) {
      const { handleFavoriteApiError } = await import('./error-handlers');
      const errorHandled = await handleFavoriteApiError(
        lastError, 
        listingId, 
        true, // isAdd
        options
      );
      
      if (errorHandled) {
        return;
      }
      
      // Final verification
      const currentSession = session || await getSession();
      const actualState = await isFavorited(listingId, options, currentSession);
      if (actualState.isFavorite) {
        return; // Operation succeeded despite errors
      }
      
      throw lastError;
    }
    
  } catch (error) {
    console.error('[FAVORITES] Error adding to favorites:', error);
    throw error;
  }
}

/**
 * Remove a listing from favorites with improved error handling and retry logic
 */
export async function removeFromFavorites(
  listingId: string,
  options?: FavoriteServiceOptions,
  session?: Session | null
): Promise<void> {
  try {
    const numericId = validateListingId(listingId);
    const currentSession = session || await getSession();
    const token = validateSession(currentSession);
    
    const url = `${API_URL}/api/favorites/${numericId}`;
    
    // Retry logic with exponential backoff
    let lastError: Error | null = null;
    
    for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      try {
        const response = await makeApiRequest(url, 'DELETE', token);
        
        if (response.ok) {
          return; // Success
        }
        
        const errorText = await response.text();
        
        // Handle specific server errors
        if (response.status === 500) {
          const { isHibernateSerializationError } = await import('./error-handlers');
          if (isHibernateSerializationError(errorText)) {
            // Verify if operation actually succeeded despite error
            const actualState = await isFavorited(listingId, options, currentSession);
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
        
      } catch (error) {
        lastError = error instanceof Error ? error : new Error(String(error));
        
        // Don't retry on the last attempt
        if (attempt < MAX_RETRIES) {
          await sleep(RETRY_DELAY_BASE * Math.pow(2, attempt));
        }
      }
    }
    
    // Handle exhausted retries
    if (lastError) {
      const { handleFavoriteApiError } = await import('./error-handlers');
      const errorHandled = await handleFavoriteApiError(
        lastError, 
        listingId, 
        false, // isAdd
        options
      );
      
      if (errorHandled) {
        return;
      }
      
      // Final verification
      const currentSession = session || await getSession();
      const actualState = await isFavorited(listingId, options, currentSession);
      if (!actualState.isFavorite) {
        return; // Operation succeeded despite errors
      }
      
      throw lastError;
    }
    
  } catch (error) {
    console.error('[FAVORITES] Error removing from favorites:', error);
    throw error;
  }
}

/**
 * Get user's favorite listings with better response handling
 */
export async function getUserFavorites(
  options?: FavoriteServiceOptions,
  session?: Session | null
): Promise<UserFavoritesResponse> {
  try {
    const currentSession = session || await getSession();
    const token = validateSession(currentSession);
    
    const url = `${API_URL}/api/favorites/user`;
    const response = await makeApiRequest(url, 'GET', token);
    
    if (!response.ok) {
      const errorText = await response.text();
      throw new FavoriteServiceError(
        `Failed to fetch favorites: ${errorText}`,
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
    
  } catch (error) {
    if (error instanceof FavoriteServiceError) {
      console.error('[FAVORITES] Error fetching user favorites:', error);
      throw error;
    }
    
    console.error('[FAVORITES] Unexpected error fetching user favorites:', error);
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
  options?: FavoriteServiceOptions,
  session?: Session | null
): Promise<FavoriteStatusResponse> {
  const baseResponse = { listingId };
  
  try {
    const numericId = validateListingId(listingId);
    const currentSession = session || await getSession();
    
    // Return false if not authenticated
    if (!currentSession?.accessToken) {
      return { ...baseResponse, isFavorite: false };
    }
    
    const url = `${API_URL}/api/favorites/check/${numericId}`;
    const response = await makeApiRequest(url, 'GET', currentSession.accessToken);
    
    if (!response.ok) {
      // Log warning but don't throw - gracefully return false
      if (response.status !== 404) {
        console.warn(`[FAVORITES] Failed to check favorite status: ${response.status}`);
      }
      return { ...baseResponse, isFavorite: false };
    }
    
    const text = await response.text();
    const isFavorite = parseFavoriteStatusResponse(text);
    
    return { ...baseResponse, isFavorite };
    
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

// ...existing code...