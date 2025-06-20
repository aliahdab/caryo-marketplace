'use client';

import React from 'react';
import CarMediaGallery from '@/components/CarMediaGallery/CarMediaGallery';
import { CarMedia } from '@/components/CarMediaGallery/types';

const TestGalleryPage: React.FC = () => {
  const sampleMedia: CarMedia[] = [
    {
      type: 'image',
      url: 'https://images.unsplash.com/photo-1549924231-f129b911e442?w=800&h=600&fit=crop&auto=format',
      alt: 'Red sports car front view',
      width: 800,
      height: 600,
    },
    {
      type: 'image',
      url: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=600&fit=crop&auto=format',
      alt: 'Luxury car interior',
      width: 800,
      height: 600,
    },
    {
      type: 'image',
      url: 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&h=600&fit=crop&auto=format',
      alt: 'Blue car side view',
      width: 800,
      height: 600,
    },
    {
      type: 'video',
      url: 'https://www.youtube.com/watch?v=ysz5S6PUM-U',
      thumbnailUrl: 'https://images.unsplash.com/photo-1493238792000-8113da705763?w=800&h=600&fit=crop&auto=format',
      alt: 'Car video tour',
      width: 800,
      height: 600,
    },
    {
      type: 'image',
      url: 'https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800&h=600&fit=crop&auto=format',
      alt: 'White car back view',
      width: 800,
      height: 600,
    },
    {
      type: 'video',
      url: 'https://www.youtube.com/watch?v=LXb3EKWsInQ',
      thumbnailUrl: 'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop&auto=format',
      alt: 'Another car video tour',
      width: 800,
      height: 600,
    }
  ];

  // Ensure you have some images in public/images/stock/ for this to work
  // e.g., car-1.jpg, car-2.jpg, car-3.jpg, car-4.jpg, video-thumb-1.jpg, video-thumb-2.jpg

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Car Media Gallery Test Page</h1>
      
      <div className="space-y-8">
        <div>
          <h2 className="text-xl font-semibold mb-4">Gallery with Images and Videos</h2>
          <div className="max-w-2xl mx-auto">
            <CarMediaGallery media={sampleMedia} />
          </div>
        </div>
        
        <div>
          <h2 className="text-xl font-semibold mb-4">Gallery with Only Images (No Video Section)</h2>
          <div className="max-w-2xl mx-auto">
            <CarMediaGallery 
              media={sampleMedia.filter(item => item.type === 'image')} 
              initialIndex={0} 
            />
          </div>
        </div>
        
        <div>
          <h2 className="text-xl font-semibold mb-4">Gallery with Only Videos (No Main Slider)</h2>
          <div className="max-w-2xl mx-auto">
            <CarMediaGallery 
              media={sampleMedia.filter(item => item.type === 'video')} 
              initialIndex={0} 
            />
          </div>
        </div>
      </div>
      
      <div className="mt-8 p-4 border rounded bg-gray-50">
        <h2 className="text-lg font-semibold mb-2">Notes:</h2>
        <ul className="list-disc list-inside space-y-1">
          <li>This page demonstrates different scenarios of the <code>CarMediaGallery</code> component.</li>
          <li>The first gallery shows the normal use case with both images and videos.</li>
          <li>The second gallery shows only images (video section should not appear).</li>
          <li>The third gallery shows only videos (main slider should show &quot;No images available&quot;).</li>
          <li>Videos open in a separate modal when clicked, images open in a swipeable lightbox.</li>
        </ul>
      </div>
    </div>
  );
};

export default TestGalleryPage;
