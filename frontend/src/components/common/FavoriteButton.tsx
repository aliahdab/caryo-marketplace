"use client";

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { addToFavorites, removeFromFavorites, isFavorited } from '@/services/favorites';
import { useTranslation } from 'react-i18next';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { FavoriteButtonProps } from '@/types/components'; // Import shared props

export const FavoriteButton: React.FC<FavoriteButtonProps> = ({
  listingId,
  className = '',
  size = 'md',
  variant = 'filled',
  onToggle,
  initialFavorite = false
}) => {
  const { t } = useTranslation('common');
  const [isFavorite, setIsFavorite] = useState(initialFavorite);
  const [isLoading, setIsLoading] = useState(false);
  const [isAnimating, setIsAnimating] = useState(false);
  const { data: session } = useSession();
  const router = useRouter();
  const animationTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const statusCheckedRef = useRef(false);

  // Validate the listing ID
  useEffect(() => {
    if (!listingId) {
      console.error('[FAVORITE] Missing listing ID');
    }
  }, [listingId]);

  // Clean up animation timeout on unmount
  useEffect(() => {
    return () => {
      if (animationTimeoutRef.current) {
        clearTimeout(animationTimeoutRef.current);
      }
    };
  }, []);

  const sizeClasses = {
    sm: 'w-8 h-8',
    md: 'w-10 h-10',
    lg: 'w-12 h-12',
  };

  const variantClasses = {
    filled: isFavorite
      ? 'bg-red-500 text-white hover:bg-red-600'
      : 'bg-white text-gray-500 hover:bg-gray-100 dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600',
    outline: isFavorite
      ? 'border-2 border-red-500 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20'
      : 'border-2 border-gray-300 text-gray-500 hover:border-gray-400 dark:border-gray-600 dark:text-gray-300 dark:hover:border-gray-500',
  };

  // Log session information whenever it changes (only errors and important info)
  useEffect(() => {
    if (!session?.user || !session?.accessToken) {
      console.warn('[FAVORITE] No valid session found');
    }
  }, [session]);

  const checkFavoriteStatus = useCallback(async (force = false) => {
    if (!listingId || !session?.user || !session?.accessToken) {
      setIsFavorite(false);
      return;
    }

    // Skip if already checked, unless forced
    if (statusCheckedRef.current && !force) {
      return;
    }
    
    try {
      setIsLoading(true);
      const result = await isFavorited(listingId, undefined, session);
      setIsFavorite(result.isFavorite);
      statusCheckedRef.current = true;
    } catch (err) {
      console.error(`[FAVORITE] Error checking favorite status for ${listingId}:`, err);
      setIsFavorite(false);
    } finally {
      setIsLoading(false);
    }
  }, [listingId, session]);

  // Effect to initialize status and handle session changes
  useEffect(() => {
    if (!listingId) {
      setIsFavorite(false);
      return;
    }

    if (!session?.user || !session?.accessToken) {
      setIsFavorite(false);
      statusCheckedRef.current = false;
      return;
    }

    // Check status immediately when session is available
    checkFavoriteStatus(true);

    // Set up periodic refresh
    const refreshInterval = setInterval(() => {
      if (session?.user) {
        checkFavoriteStatus(true);
      }
    }, 30000); // Refresh every 30 seconds
    
    return () => {
      clearInterval(refreshInterval);
    };
  }, [listingId, session, checkFavoriteStatus]);

  // Re-check status when the component becomes visible again
  useEffect(() => {
    if (typeof document !== 'undefined') {
      const handleVisibilityChange = () => {
        if (document.visibilityState === 'visible') {
          checkFavoriteStatus(true);
        }
      };

      document.addEventListener('visibilitychange', handleVisibilityChange);
      return () => {
        document.removeEventListener('visibilitychange', handleVisibilityChange);
      };
    }
  }, [checkFavoriteStatus]);

  const startAnimation = useCallback(() => {
    if (animationTimeoutRef.current) {
      clearTimeout(animationTimeoutRef.current);
      animationTimeoutRef.current = null;
    }
    setIsAnimating(true);
    animationTimeoutRef.current = setTimeout(() => {
      setIsAnimating(false);
      animationTimeoutRef.current = null;
    }, 300);
  }, []); // No dependencies, so it's stable.

  // Effect to process pending favorite action after login
  useEffect(() => {
    const processPendingFavorite = async () => {
      if (session?.user && listingId) {
        const pendingActionJSON = localStorage.getItem('pendingFavoriteAction');
        if (pendingActionJSON) {
          let pendingAction;
          try {
            pendingAction = JSON.parse(pendingActionJSON);
          } catch (parseError) {
            console.error('[FAVORITE] Error parsing pendingFavoriteAction:', parseError);
            localStorage.removeItem('pendingFavoriteAction'); // Clear corrupted data
            return;
          }

          // Optional: Check timestamp to ignore very old actions
          // const MAX_AGE_MS = 5 * 60 * 1000; // 5 minutes
          // if (pendingAction.timestamp && (Date.now() - pendingAction.timestamp > MAX_AGE_MS)) {
          //   console.log('[FAVORITE] Stale pending action ignored:', pendingAction);
          //   localStorage.removeItem('pendingFavoriteAction');
          //   return;
          // }

          if (pendingAction.listingId === listingId && pendingAction.action === 'add') {
            localStorage.removeItem('pendingFavoriteAction'); // Remove immediately

            setIsLoading(true);
            setIsFavorite(true); // Optimistic update
            if (onToggle) onToggle(true);
            startAnimation();

            try {
              await addToFavorites(listingId, undefined, session);
              // Optimistic update holds. The other useEffect with checkFavoriteStatus will eventually re-confirm.
            } catch (err) {
              console.error('[FAVORITE] Error executing pending favorite action:', err);
              setIsFavorite(false); // Revert optimistic update
              if (onToggle) onToggle(false);
              // Explicitly call checkFavoriteStatus to sync to actual state after failure
              await checkFavoriteStatus(); 
            } finally {
              setIsLoading(false);
            }
          } else if (pendingAction.listingId !== listingId) {
            // Action is for a different listing. Ignore it here.
            // It will be processed if the user navigates to that listing's page.
          } else if (pendingAction.action !== 'add') {
            // Invalid action type or already processed, remove it.
            console.warn('[FAVORITE] Invalid or unexpected pending action found:', pendingAction);
            localStorage.removeItem('pendingFavoriteAction');
          }
        }
      }
    };

    processPendingFavorite();
  }, [session, listingId, onToggle, startAnimation, checkFavoriteStatus]); // Dependencies

  const handleToggleFavorite = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (isLoading) return;
    
    if (!session?.user || !session?.accessToken) {
      if (!isFavorite) { // Only store intent if action is to ADD to favorites
        localStorage.setItem('pendingFavoriteAction', JSON.stringify({ 
          listingId: listingId, 
          action: 'add',
          timestamp: Date.now()
        }));
      }
      // Get current URL to return to after login
      const returnUrl = encodeURIComponent(window.location.href);
      router.push(`/auth/signin?returnUrl=${returnUrl}&action=favorite&listingId=${listingId}`);
      return;
    }
    
    try {
      setIsLoading(true);
      startAnimation();
      
      const wasAlreadyFavorite = isFavorite;
      
      if (wasAlreadyFavorite) {
        await removeFromFavorites(listingId, undefined, session);
        setIsFavorite(false);
        if (onToggle) onToggle(false);
      } else {
        await addToFavorites(listingId, undefined, session);
        setIsFavorite(true);
        if (onToggle) onToggle(true);
      }
    } catch (error) {
      console.error(`[FAVORITE] Error toggling favorite for ${listingId}:`, error);
      // Attempt to sync with actual state on error
      await checkFavoriteStatus();
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <button
      type="button"
      onClick={handleToggleFavorite}
      disabled={isLoading || !listingId} // Disable if no listingId
      className={`rounded-full flex items-center justify-center shadow-sm 
        ${sizeClasses[size]} ${variantClasses[variant]} ${className}
        ${isAnimating ? 'scale-110' : 'scale-100'} 
        transition-all duration-200 ease-in-out
        hover:scale-105 active:scale-95`}
      aria-label={isFavorite ? t('listings.removeFromFavorites') : t('listings.addToFavorites')}
      title={isFavorite ? t('listings.removeFromFavorites') : t('listings.addToFavorites')}
    >
      {isLoading ? (
        <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      ) : (
        <svg 
          xmlns="http://www.w3.org/2000/svg" 
          className="h-5 w-5 transition-colors duration-200"
          viewBox="0 0 24 24"
          fill={isFavorite ? 'currentColor' : 'none'}
          stroke="currentColor"
          strokeWidth="2"
        >
          <path 
            strokeLinecap="round" 
            strokeLinejoin="round" 
            d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" 
          />
        </svg>
      )}
    </button>
  );
};

export default FavoriteButton;
