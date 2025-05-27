import withBundleAnalyzer from '@next/bundle-analyzer';
import type { NextConfig } from 'next'; // Import NextConfig

const analyzeBundles = withBundleAnalyzer({
  enabled: process.env.ANALYZE === 'true',
});

const nextConfig: NextConfig = { // Add NextConfig type
  /* config options here */
  env: {
    NEXTAUTH_SECRET: process.env.NEXTAUTH_SECRET || "AdJ8m5EpqN6qPwEtH7XsKfRzV2yG9LcZ",
    NEXTAUTH_URL: process.env.NEXTAUTH_URL || "http://localhost:3000",
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080",
    NEXT_PUBLIC_MINIO_URL: process.env.NEXT_PUBLIC_MINIO_URL || "http://localhost:9000" // Add MINIO URL
  },
  // App Router handles i18n differently - configuration is done through LanguageProvider
  
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'lh3.googleusercontent.com',
        pathname: '/**',
      },
      {
        protocol: 'https',
        hostname: 'placehold.co',
        pathname: '/**',
      },
      {
        protocol: 'https',
        hostname: 'images.unsplash.com',
        pathname: '/**',
      },
      {
        protocol: 'http',
        hostname: 'localhost',
        port: '9000',
        pathname: '/**',
      },
      {
        protocol: 'http',
        hostname: '127.0.0.1',
        port: '9000',
        pathname: '/**',
      },
    ],
  },

  // Using the stable turbopack configuration
  turbopack: {
    // You can add Turbopack-specific options here if needed
  }
};

export default analyzeBundles(nextConfig);
