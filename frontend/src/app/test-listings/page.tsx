"use client";

import { useEffect, useState } from 'react';

export default function TestListingsPage() {
  const [message, setMessage] = useState('Component mounting...');

  useEffect(() => {
    console.log('[TestListingsPage] useEffect running!');
    setMessage('useEffect has run!');
    
    // Test the API call
    fetch('http://localhost:8080/api/listings/filter')
      .then(res => res.json())
      .then(data => {
        console.log('[TestListingsPage] API call successful:', data.content?.length, 'listings');
        setMessage(`API call successful! Got ${data.content?.length || 0} listings`);
      })
      .catch(err => {
        console.error('[TestListingsPage] API call failed:', err);
        setMessage(`API call failed: ${err.message}`);
      });
  }, []);

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">Test Listings Page</h1>
      <p>{message}</p>
    </div>
  );
}
