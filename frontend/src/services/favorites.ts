import { 
  FavoriteServiceOptions, 
  FavoriteStatusResponse, 
  UserFavoritesResponse
} from '@/types/favorites';
import { getSession } from 'next-auth/react';
import { Session } from 'next-auth';

// API URL configuration
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Add a listing to favorites
 */
export async function addToFavorites(
  listingId: string, 
  options?: FavoriteServiceOptions | undefined,
  session?: Session | null
): Promise<void> {
  try {
    // Ensure listingId is a valid number
    const numericId = parseInt(listingId, 10);
    if (isNaN(numericId)) {
      throw new Error('Invalid listing ID');
    }
    
    // Prepare API call
    const api_url = API_URL;
    const endpoint = `/api/favorites/${numericId}`;
    const url = `${api_url}${endpoint}`;
    
    // Use provided session or get current session
    const currentSession = session || await getSession();
    
    if (!currentSession?.accessToken) {
      throw new Error('UNAUTHORIZED: No valid token');
    }
    
    const token = currentSession.accessToken;
    
    // Make direct fetch call with retry mechanism
    const MAX_RETRIES = 2;
    let retryCount = 0;
    let lastError: unknown;
    
    while (retryCount <= MAX_RETRIES) {
      try {
        const response = await fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          credentials: 'include'
        });
        
        // Check if the request was successful
        if (!response.ok) {
          const errorText = await response.text();
          
          // If this is a server error that might be related to Hibernate, check if operation succeeded
          if (response.status === 500) {
            const { isHibernateSerializationError } = await import('./error-handlers');
            if (isHibernateSerializationError(errorText)) {
              // Verify actual state before throwing
              const actualState = await isFavorited(listingId, options, currentSession);
              if (actualState.isFavorite === true) {
                return; // Exit successfully
              }
            }
          }
          
          throw new Error(`Error adding favorite: ${response.status} ${response.statusText}\n${errorText}`);
        }
        
        return; // Success - exit function
        
      } catch (apiError) {
        lastError = apiError;
        retryCount++;
        
        if (retryCount <= MAX_RETRIES) {
          // Exponential backoff: 500ms, 1000ms, etc.
          const backoffMs = 500 * retryCount;
          await new Promise(resolve => setTimeout(resolve, backoffMs));
        }
      }
    }
    
    // If we got here, we've exhausted our retries. Try to handle the error.
    const { handleFavoriteApiError } = await import('./error-handlers');
    
    // Try to handle the error with our utility function
    const errorHandled = await handleFavoriteApiError(
      lastError, 
      listingId, 
      true, // isAdd = true for add operation
      options
    );
    
    // If the error was handled and the operation actually succeeded, we can continue
    if (errorHandled) {
      return;
    }
    
    // Otherwise, rethrow the error
    throw lastError;
    
  } catch (error) {
    console.error('[FAVORITES] Error adding to favorites:', error);
    
    // One final check to verify actual state
    try {
      const currentSession = session || await getSession();
      const actualState = await isFavorited(listingId, options, currentSession);
      
      // If the state is already what we wanted, suppress the error
      if (actualState.isFavorite === true) {
        return;
      }
    } catch (verifyError) {
      console.error('[FAVORITES] Failed to verify final state:', verifyError);
    }
    
    // Re-throw the original error
    throw error;
  }
}

/**
 * Remove a listing from favorites
 */
export async function removeFromFavorites(
  listingId: string,
  options?: FavoriteServiceOptions | undefined,
  session?: Session | null
): Promise<void> {
  try {
    // Ensure listingId is a valid number
    const numericId = parseInt(listingId, 10);
    if (isNaN(numericId)) {
      throw new Error('Invalid listing ID');
    }
    
    // Prepare API call
    const api_url = API_URL;
    const endpoint = `/api/favorites/${numericId}`;
    const url = `${api_url}${endpoint}`;
    
    // Use provided session or get current session
    const currentSession = session || await getSession();
    
    if (!currentSession?.accessToken) {
      throw new Error('UNAUTHORIZED: No valid token');
    }
    
    const token = currentSession.accessToken;
    
    // Make direct fetch call with retry mechanism
    const MAX_RETRIES = 2;
    let retryCount = 0;
    let lastError: unknown;
    
    while (retryCount <= MAX_RETRIES) {
      try {
        const response = await fetch(url, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          credentials: 'include'
        });
        
        // Check if the request was successful
        if (!response.ok) {
          const errorText = await response.text();
          
          // If this is a server error that might be related to Hibernate, check if operation succeeded
          if (response.status === 500) {
            const { isHibernateSerializationError } = await import('./error-handlers');
            if (isHibernateSerializationError(errorText)) {
              // Verify actual state before throwing
              const actualState = await isFavorited(listingId, options, currentSession);
              if (actualState.isFavorite === false) {
                return; // Exit successfully
              }
            }
          }
          
          throw new Error(`Error removing favorite: ${response.status} ${response.statusText}\n${errorText}`);
        }
        
        return; // Success - exit function
        
      } catch (apiError) {
        lastError = apiError;
        retryCount++;
        
        if (retryCount <= MAX_RETRIES) {
          // Exponential backoff: 500ms, 1000ms, etc.
          const backoffMs = 500 * retryCount;
          await new Promise(resolve => setTimeout(resolve, backoffMs));
        }
      }
    }
    
    // If we got here, we've exhausted our retries. Try to handle the error.
    const { handleFavoriteApiError } = await import('./error-handlers');
    
    // Try to handle the error with our utility function
    const errorHandled = await handleFavoriteApiError(
      lastError, 
      listingId, 
      false, // isAdd = false for remove operation
      options
    );
    
    // If the error was handled and the operation actually succeeded, we can continue
    if (errorHandled) {
      return;
    }
    
    // Otherwise, rethrow the error
    throw lastError;
    
  } catch (error) {
    console.error('[FAVORITES] Error removing from favorites:', error);
    
    // One final check to verify actual state
    try {
      const currentSession = session || await getSession();
      const actualState = await isFavorited(listingId, options, currentSession);
      
      // If the state is already what we wanted, suppress the error
      if (actualState.isFavorite === false) {
        return;
      }
    } catch (verifyError) {
      console.error('[FAVORITES] Failed to verify final state:', verifyError);
    }
    
    // Re-throw the original error
    throw error;
  }
}

/**
 * Get user's favorite listings
 */
export async function getUserFavorites(
  options?: FavoriteServiceOptions,
  session?: Session | null
): Promise<UserFavoritesResponse> {
  try {
    // Use provided session or get current session
    const currentSession = session || await getSession();
    
    if (!currentSession?.accessToken) {
      throw new Error('UNAUTHORIZED: No access token available');
    }

    const url = `${API_URL}/api/favorites/user`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${currentSession.accessToken}`
      },
      credentials: 'include'
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error fetching favorites: ${response.status} ${response.statusText}\n${errorText}`);
    }

    const text = await response.text();
    
    if (!text || text.trim() === '') {
      // Return empty array if no content
      return { favorites: [], total: 0 };
    }

    try {
      const data = JSON.parse(text);
      
      // Handle array response
      if (Array.isArray(data)) {
        return { favorites: data, total: data.length };
      }
      
      // Handle object with favorites array
      if (data && Array.isArray(data.favorites)) {
        return { favorites: data.favorites, total: data.favorites.length };
      }
      
      // Handle object with data array
      if (data && data.data && Array.isArray(data.data)) {
        return { favorites: data.data, total: data.data.length };
      }
      
      // Default fallback
      return { favorites: [], total: 0 };
      
    } catch (parseError) {
      console.error('[FAVORITES] Error parsing favorites response:', parseError);
      return { favorites: [], total: 0 };
    }

  } catch (error) {
    console.error('[FAVORITES] Error fetching user favorites:', error);
    throw error;
  }
}

/**
 * Check if a listing is favorited by the current user
 */
export async function isFavorited(
  listingId: string,
  options?: FavoriteServiceOptions,
  session?: Session | null
): Promise<FavoriteStatusResponse> {
  try {
    // Ensure listingId is a valid number
    const numericId = parseInt(listingId, 10);
    if (isNaN(numericId)) {
      throw new Error('Invalid listing ID');
    }
    
    // Use provided session or get current session
    const currentSession = session || await getSession();
    
    if (!currentSession?.accessToken) {
      return { isFavorite: false, listingId };
    }

    const url = `${API_URL}/api/favorites/check/${numericId}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${currentSession.accessToken}`
      },
      credentials: 'include'
    });

    if (!response.ok) {
      return { isFavorite: false, listingId };
    }

    const text = await response.text();
    
    // Handle boolean string responses
    if (text === 'true') return { isFavorite: true, listingId };
    if (text === 'false') return { isFavorite: false, listingId };
    
    // Try to parse as JSON
    try {
      const data = JSON.parse(text);
      
      // Handle various response formats
      if (typeof data === 'boolean') {
        return { isFavorite: data, listingId };
      }
      
      if (data && typeof data.isFavorite === 'boolean') {
        return { isFavorite: data.isFavorite, listingId };
      }
      
      if (data && typeof data.favorited === 'boolean') {
        return { isFavorite: data.favorited, listingId };
      }
      
      // Default to false for unknown formats
      return { isFavorite: false, listingId };
      
    } catch {
      // If we can't parse, default to false
      return { isFavorite: false, listingId };
    }

  } catch {
    return { isFavorite: false, listingId };
  }
}

// ...existing code...