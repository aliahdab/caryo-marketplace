import { useState, useEffect, useCallback, useRef } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { MdFavorite, MdFavoriteBorder } from 'react-icons/md';
import { addToFavorites, removeFromFavorites, isFavorited } from '@/services/favorites';
import { LegacyFavoriteButtonProps } from '@/types/components'; // Import shared props

export default function FavoriteButton({ 
  listingId, 
  className = '', 
  size = 24,
  showText = false 
}: LegacyFavoriteButtonProps) { // Use imported LegacyFavoriteButtonProps
  const { data: session } = useSession();
  const router = useRouter();
  const [isFavorite, setIsFavorite] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const statusCheckedRef = useRef(false);

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
      const status = await isFavorited(listingId);
      setIsFavorite(status.isFavorite);
      statusCheckedRef.current = true;
    } catch (err) {
      console.error('Error checking favorite status:', err);
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
      console.log(`[FAVORITE] No session for listing ${listingId}. Resetting favorite status.`);
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

  const handleClick = async (e: React.MouseEvent) => {
    e.preventDefault(); // Prevent navigation if button is inside a link
    e.stopPropagation(); // Prevent event bubbling

    if (!session) {
      // Store the intent to add to favorites
      localStorage.setItem('pendingFavoriteAction', JSON.stringify({ 
        listingId: listingId, 
        action: 'add',
        timestamp: Date.now()
      }));
      // Get current URL to return to after login
      const returnUrl = encodeURIComponent(window.location.href);
      router.push(`/auth/signin?returnUrl=${returnUrl}&action=favorite&listingId=${listingId}`);
      return;
    }

    if (isLoading) return;

    try {
      setIsLoading(true);
      if (isFavorite) {
        await removeFromFavorites(listingId);
      } else {
        await addToFavorites(listingId);
      }
      setIsFavorite(!isFavorite);
    } catch (err) {
      console.error('Error toggling favorite:', err);
      // Show error toast or message
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <button
      onClick={handleClick}
      disabled={isLoading}
      className={`inline-flex items-center justify-center transition-colors ${
        isFavorite
          ? 'text-red-600 hover:text-red-700 dark:text-red-500 dark:hover:text-red-400'
          : 'text-gray-400 hover:text-red-600 dark:text-gray-500 dark:hover:text-red-500'
      } ${isLoading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'} ${className}`}
      title={isFavorite ? 'Remove from favorites' : 'Add to favorites'}
    >
      {isFavorite ? (
        <MdFavorite size={size} className={isLoading ? 'animate-pulse' : ''} />
      ) : (
        <MdFavoriteBorder size={size} className={isLoading ? 'animate-pulse' : ''} />
      )}
      {showText && (
        <span className="ml-2 text-sm">
          {isFavorite ? 'Remove from favorites' : 'Add to favorites'}
        </span>
      )}
    </button>
  );
}