"use client";

import { useEffect, useState } from 'react';

export default function FilterTestPage() {
  const [response, setResponse] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function testFilter() {
      try {
        // Create test filters
        const filters = {
          brand: 'Toyota', // Test with a specific brand
          model: 'Camry',  // Test with a specific model
          page: 1,
          limit: 10
        };
        
        // Log what we're about to send
        console.log('[FilterTest] Testing with filters:', filters);
        
        // Convert filters to URL params - same logic as in listings.ts
        const params = new URLSearchParams(
          Object.entries(filters)
            .filter(([_, value]) => value !== undefined && value !== '')
            .map(([key, value]) => [
              key === 'page' ? 'page' : key,
              // Convert page number to 0-based indexing for the API
              key === 'page' ? String(Number(value) - 1) : String(value)
            ])
        );
        
        // Log the constructed URL
        const url = `${process.env.NEXT_PUBLIC_API_URL}/api/listings/filter?${params.toString()}`;
        console.log('[FilterTest] Sending request to:', url);
        
        // Make the request directly using fetch
        const response = await fetch(url, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
          mode: 'cors',
          credentials: 'include',
          cache: 'no-store'
        });
        
        // Log response details
        console.log('[FilterTest] Response status:', response.status);
        
        // Parse the response
        const data = await response.json();
        console.log('[FilterTest] Response data:', data);
        
        setResponse(data);
        setIsLoading(false);
      } catch (err) {
        console.error('[FilterTest] Error:', err);
        setError(err instanceof Error ? err.message : 'Unknown error');
        setIsLoading(false);
      }
    }
    
    testFilter();
  }, []);

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Filter Test Page</h1>
      
      {isLoading && <p>Loading...</p>}
      
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          <p><strong>Error:</strong> {error}</p>
        </div>
      )}
      
      {response && (
        <div className="bg-white shadow-md rounded p-4">
          <h2 className="text-xl font-bold mb-2">Response:</h2>
          <pre className="bg-gray-100 p-2 rounded overflow-auto max-h-96">
            {JSON.stringify(response, null, 2)}
          </pre>
          
          <div className="mt-4">
            <h3 className="text-lg font-bold">Found {response.totalElements} listings</h3>
            <ul className="list-disc pl-5 mt-2">
              {response.content?.map((item: any) => (
                <li key={item.id} className="mb-2">
                  {item.brand} {item.model} ({item.modelYear}) - ${item.price}
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  );
}
