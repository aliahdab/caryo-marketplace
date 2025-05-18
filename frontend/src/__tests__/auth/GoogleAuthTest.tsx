import { render, screen, waitFor } from '@testing-library/react';
import { act } from 'react'; // Import act from react instead of react-dom/test-utils
import { signIn } from 'next-auth/react';
import GoogleSignInButton from '@/components/auth/GoogleSignInButton';
import userEvent from '@testing-library/user-event'; 
import React from 'react'; // Import React for JSX

// Mock next-auth/react
jest.mock('next-auth/react', () => ({
  signIn: jest.fn(),
}));

// Mock next/navigation
const mockRouterPush = jest.fn();
const mockRouterReplace = jest.fn();
const mockRouterRefresh = jest.fn(); 
const mockRouterBack = jest.fn();
const mockRouterForward = jest.fn();
const mockRouterPrefetch = jest.fn();
const mockSearchParamsGet = jest.fn();
const mockSearchParamsHas = jest.fn();
const mockSearchParamsForEach = jest.fn();

jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockRouterPush,
    replace: mockRouterReplace,
    refresh: mockRouterRefresh, 
    back: mockRouterBack,
    forward: mockRouterForward,
    prefetch: mockRouterPrefetch,
  }),
  useSearchParams: () => ({
    get: mockSearchParamsGet,
    has: mockSearchParamsHas,
    forEach: mockSearchParamsForEach,
  }),
  usePathname: jest.fn(() => '/auth/signin'), 
}));

// Mock react-icons
jest.mock('react-icons/fc', () => ({
  FcGoogle: () => <div data-testid="google-icon">Google Icon</div>
}));

// Mock i18n
jest.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, options?: { defaultValue?: string } | string) => {
      const defaultValue = typeof options === 'string' 
        ? options 
        : (typeof options === 'object' && options !== null ? options.defaultValue : undefined);
      // Provide some basic translations or return the key
      const translations: Record<string, string> = {
        'auth.continueWithGoogle': 'Continue with Google',
        'auth.username': 'Username',
        'auth.password': 'Password',
        'auth.signin': 'Sign In',
      };
      return translations[key] || defaultValue || key;
    },
    i18n: {
      changeLanguage: jest.fn().mockResolvedValue(undefined),
      language: 'en',
      isInitialized: true,
      resolvedLanguage: 'en',
      dir: () => 'ltr',
    },
  }),
}));


describe('GoogleSignInButton', () => {
  const user = userEvent.setup();
  
  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();
    
    // Reset router mocks
    mockRouterPush.mockClear();
    mockRouterReplace.mockClear();
    mockRouterRefresh.mockClear();
    mockRouterBack.mockClear();
    mockRouterForward.mockClear();
    mockRouterPrefetch.mockClear();

    // Reset searchParams mocks
    mockSearchParamsGet.mockClear();
    mockSearchParamsHas.mockClear();
    mockSearchParamsForEach.mockClear();

    // Mock signIn to resolve successfully by default
    (signIn as jest.Mock).mockResolvedValue({ ok: true, error: null, url: '/' });
  });

  test('should call signIn with google provider when Google button is clicked', async () => {
    render(<GoogleSignInButton />);
    
    const googleButton = screen.getByRole('button', { name: /continue with google/i });
    expect(googleButton).toBeInTheDocument();
    
    // Use act to wrap the state updates
    await act(async () => {
      await user.click(googleButton);
    });
    
    expect(signIn).toHaveBeenCalledWith('google', { 
      callbackUrl: '/dashboard', 
      redirect: true 
    });
  });

  test('should show loading state and handle successful Google sign-in', async () => {
    // Create a promise that we can resolve later
    let resolveSignInPromise: (value: { ok: boolean; error?: string | null; url?: string }) => void;
    const signInPromise = new Promise<{ ok: boolean; error?: string | null; url?: string }>(resolve => {
      resolveSignInPromise = resolve;
    });
    
    (signIn as jest.Mock).mockReturnValue(signInPromise);
    
    render(<GoogleSignInButton />);
    
    const googleButton = screen.getByRole('button', { name: /continue with google/i });
    
    // Use act to wrap the state updates
    await act(async () => {
      await user.click(googleButton);
    });
    
    // Button should be in loading state
    expect(googleButton).toBeDisabled();
    
    // Simulate successful sign-in
    await act(async () => {
      resolveSignInPromise!({ ok: true });
    });
    
    // Wait for loading state to be cleared
    await waitFor(() => {
      expect(googleButton).not.toBeDisabled();
    });
  });

  test('should handle errors during Google sign-in', async () => {
    // Mock signIn to reject with an error
    (signIn as jest.Mock).mockRejectedValue(new Error('Google authentication failed'));
    
    render(<GoogleSignInButton />);
    
    const googleButton = screen.getByRole('button', { name: /continue with google/i });
    
    // Use act to wrap the state updates
    await act(async () => {
      await user.click(googleButton);
    });
    
    // Wait for the error message to appear
    await waitFor(() => {
      // Find the error div that contains the error text
      const errorElement = screen.getByText(/Google authentication failed/i);
      expect(errorElement).toBeInTheDocument();
      expect(errorElement.closest('div')).toHaveClass('text-red-600');
    });
    
    // Button should no longer be disabled
    expect(googleButton).not.toBeDisabled();
  });
});
