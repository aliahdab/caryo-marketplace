import { Listing } from './listings';
import { Session } from 'next-auth';

/**
 * Interface for favorite action handlers
 */
export interface FavoriteHandlers {
  onToggle?: (isFavorite: boolean) => void;
  initialFavorite?: boolean;
}

/**
 * Type for favorites service options
 */
export interface FavoriteServiceOptions {
  session: Session;
}

/**
 * Interface for favorite status response
 */
export interface FavoriteStatusResponse {
  isFavorite: boolean;
  listingId: string;
}

/**
 * Interface for user favorites response
 */
export interface UserFavoritesResponse {
  favorites: Listing[];
  total: number;
}

/**
 * Interface for favorite response from backend
 */
export interface FavoriteResponse {
  id: number;
  userId: number;
  carListingId: number;
  createdAt: string;
}

/**
 * Raw API response types
 */
export type RawFavoritesResponse = 
  | Listing[] 
  | { data: Listing[] }
  | { favorites: Listing[]; total: number };

export type RawFavoriteStatusResponse = 
  | boolean 
  | { isFavorite: boolean } 
  | { status: boolean };
