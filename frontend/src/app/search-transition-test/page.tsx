"use client";

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import HomeSearchBar from '@/components/search/HomeSearchBar';

// This is a simple component to demonstrate and verify 
// the fix for the page shaking issue
export default function SearchAndTransitionPage() {
  const [transitioned, setTransitioned] = useState(false);
  const router = useRouter();
  
  // Function to simulate a search and transition
  const simulateSearch = () => {
    // Show transition UI first
    setTransitioned(true);
    
    // After 1 second, navigate to listings page
    setTimeout(() => {
      router.push('/listings?brand=Toyota&model=Camry');
    }, 1000);
  };
  
  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-6">Search Transition Test</h1>
      
      {!transitioned ? (
        <>
          <p className="mb-4">
            This page demonstrates the smooth transition from search to listings.
            Click the search button to test the transition.
          </p>
          
          <HomeSearchBar />
          
          <div className="mt-6">
            <button 
              onClick={simulateSearch}
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              Simulate Search Transition
            </button>
          </div>
        </>
      ) : (
        <div className="min-h-[50vh] flex items-center justify-center">
          <div className="text-center">
            <div className="mb-4">
              <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-blue-500 border-r-transparent"></div>
            </div>
            <p>Transitioning to listings page...</p>
          </div>
        </div>
      )}
    </div>
  );
}
