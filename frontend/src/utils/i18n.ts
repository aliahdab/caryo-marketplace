"use client";

import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import HttpApi from "i18next-http-backend";

/**
 * DEVELOPER NOTE:
 * -----------------------------------------------------------------------------
 * When using translations with useTranslation('common'), use the direct key format:
 * 
 * ✅ CORRECT: t('key')
 * ❌ INCORRECT: t('common.key')
 * 
 * The namespace ('common') is already specified in useTranslation('common'),
 * so keys should be accessed directly without the namespace prefix.
 * 
 * HTTP Backend: Using i18next-http-backend for dynamic loading of translation files
 * from /public/locales/ directory structure. This is the recommended approach for
 * production applications as it allows for lazy loading and better performance.
 * -----------------------------------------------------------------------------
 */

// Define supported languages
export const LANGUAGES = {
  EN: 'en',
  AR: 'ar'
} as const;

export type SupportedLanguage = typeof LANGUAGES[keyof typeof LANGUAGES];

/**
 * Gets the current language from various sources
 * Order: cookie -> Next.js route -> localStorage -> browser language -> default
 * @returns The detected language code ('en' or 'ar')
 */
const getCurrentLanguage = (): SupportedLanguage => {
  // Only run in browser environment
  if (typeof window !== 'undefined') {
    try {
      // Check cookie first - more reliable with Next.js
      const getCookie = (name: string): string | null => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) {
          const cookieValue = parts.pop()?.split(';').shift();
          return cookieValue || null;
        }
        return null;
      };
      
      const cookieLang = getCookie('NEXT_LOCALE');
      if (cookieLang && isValidLanguage(cookieLang)) {
        return cookieLang as SupportedLanguage;
      }
      
      // Check URL/route for language (useful with Next.js i18n routing)
      const pathLang = window.location.pathname.split('/')[1];
      if (pathLang && isValidLanguage(pathLang)) {
        return pathLang as SupportedLanguage;
      }
      
      // Then check localStorage
      const localLang = localStorage.getItem('NEXT_LOCALE');
      if (localLang && isValidLanguage(localLang)) {
        return localLang as SupportedLanguage;
      }
      
      // If no saved preference, check browser language
      const browserLang = navigator.language.split('-')[0];
      if (isValidLanguage(browserLang)) {
        return browserLang as SupportedLanguage;
      }
    } catch (error) {
      console.warn("Error detecting language:", error);
      // Fall through to default
    }
  }
  
  return LANGUAGES.AR; // Default to Arabic
};

/**
 * Validates if a language code is supported
 * @param lang Language code to validate
 * @returns Whether the language is supported
 */
function isValidLanguage(lang: string): lang is SupportedLanguage {
  return Object.values(LANGUAGES).includes(lang as SupportedLanguage);
}

// Initialize i18next with HTTP backend for dynamic loading
i18n
  .use(HttpApi) // Use HTTP backend for loading translation files
  .use(LanguageDetector) // Detect user language
  .use(initReactI18next) // Pass i18n to react-i18next
  .init({
    ns: ['common', 'translation', 'errors', 'listings', 'auth'], // Available namespaces
    defaultNS: 'common',
    lng: getCurrentLanguage(),
    fallbackLng: LANGUAGES.AR, // Default to Arabic if language detection fails
    
    // HTTP backend configuration
    backend: {
      loadPath: '/locales/{{lng}}/{{ns}}.json',
      addPath: '/locales/add/{{lng}}/{{ns}}',
      allowMultiLoading: false,
      crossDomain: false,
      withCredentials: false,
      requestOptions: {
        cache: 'default',
        credentials: 'same-origin',
        mode: 'cors',
      }
    },
    
    interpolation: {
      escapeValue: false, // Not needed for React
    },
    
    detection: {
      order: ["cookie", "localStorage", "navigator", "htmlTag"],
      caches: ["cookie", "localStorage"],
      lookupCookie: "NEXT_LOCALE",
      lookupLocalStorage: "NEXT_LOCALE",
    },
    
    react: {
      useSuspense: false, // Prevent issues with SSR
    },
    
    // Load all namespaces on init for better performance
    preload: [LANGUAGES.EN, LANGUAGES.AR],
    load: 'languageOnly', // Load only language part, not region
    
    // Error handling
    saveMissing: false, // Don't save missing keys automatically
    debug: process.env.NODE_ENV === 'development', // Enable debug logging in development
    missingKeyHandler: (lng, ns, key) => {
      console.warn(`Missing translation key: ${ns}:${key} for language: ${lng}`);
    },
  });

/**
 * Helper function to change the application language
 * @param language The language code to change to
 * @returns Promise resolving when language is changed
 */
export const changeLanguage = async (language: SupportedLanguage): Promise<void> => {
  if (!isValidLanguage(language)) {
    console.error(`Invalid language code: ${language}`);
    return;
  }
  
  try {
    // Update cookie for persistence (use cookies-next for better Next.js integration)
    import('cookies-next').then(({ setCookie }) => {
      setCookie('NEXT_LOCALE', language, { 
        path: '/',
        maxAge: 31536000, // 1 year expiry
        sameSite: 'lax'
      });
    });
    
    // Update localStorage (with error handling)
    try {
      localStorage.setItem('NEXT_LOCALE', language);
    } catch (e) {
      // localStorage might be unavailable in some contexts
      console.warn('Could not save language preference to localStorage', e);
    }
    
    // Change i18next language
    await i18n.changeLanguage(language);
    
    // Update document language attribute and direction
    if (typeof document !== 'undefined') {
      const isRTL = language === LANGUAGES.AR;
      document.documentElement.lang = language;
      document.documentElement.dir = isRTL ? 'rtl' : 'ltr';
      
      // Dispatch an event so other components can react to the language change
      document.dispatchEvent(new CustomEvent('languagechange', { 
        detail: { language, direction: isRTL ? 'rtl' : 'ltr' } 
      }));
    }
  } catch (error) {
    console.error("Error changing language:", error);
  }
};

export default i18n;
