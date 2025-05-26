"use client";

import React, { useState, useEffect, ChangeEvent, FormEvent } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { ListingFormData, Governorate } from '@/types/listings';
import { fetchGovernorates } from '@/services/api';
import ListingExpiry from "../../components/ListingExpiry";
import { formatNumber } from '../../../../../utils/localization';

// Mock data for a listing (in a real app, this would come from an API fetch)
const MOCK_LISTING: ListingFormData = {
  id: "1",
  governorateId: "", // Added missing governorateId
  title: "Toyota Camry 2020",
  description: "Well maintained Toyota Camry with low mileage. One owner, service history available.",
  make: "Toyota",
  model: "Camry",
  year: "2020",
  price: "25000",
  currency: "USD", // Added currency
  condition: "used",
  mileage: "45000",
  exteriorColor: "Silver",
  interiorColor: "Black",
  transmission: "automatic",
  fuelType: "gasoline",
  features: ["airConditioning", "bluetoothConnectivity", "cruiseControl", "alloyWheels"],
  location: "Dubai Marina",
  city: "Dubai",
  contactPreference: "both",
  images: [], // This empty array is assignable to File[]
  status: "active",
  created: "2023-05-15",
  expires: "2023-08-15",
  views: 120
};

// Client component
export default function EditListingPageClient({ id }: { id: string }) {
  const router = useRouter();
  const [formData, setFormData] = useState<ListingFormData>({
    ...MOCK_LISTING, // Assuming MOCK_LISTING is the initial data for the form
    governorateId: MOCK_LISTING.governorateId || "", // Ensure governorateId is initialized
  });
  const [governorates, setGovernorates] = useState<Governorate[]>([]);
  const [isLoadingGovernorates, setIsLoadingGovernorates] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Available car features
  const carFeatures = [
    "airConditioning", "leatherSeats", "sunroof", "navigation",
    "bluetoothConnectivity", "parkingSensors", "reverseCam",
    "cruiseControl", "alloyWheels", "electricWindows"
  ];

  useEffect(() => {
    // In a real app, fetch the listing data based on listingId
    // For now, we're using MOCK_LISTING, but we'll ensure governorateId is part of the form state
    // and potentially update it if the fetched listing has one.
    console.log("Fetching listing data for ID:", id);
    // Simulate fetching and setting data
    // setFormData(MOCK_LISTING); // This is already done in useState initialization

    const loadGovernorates = async () => {
      try {
        setIsLoadingGovernorates(true);
        const fetchedGovernorates = await fetchGovernorates();
        setGovernorates(fetchedGovernorates);
      } catch (err) {
        console.error("Failed to fetch governorates", err);
        // Optionally set an error state to display to the user
      } finally {
        setIsLoadingGovernorates(false);
      }
    };
    loadGovernorates();
  }, [id]); // listingId dependency is for fetching the listing itself, governorates are fetched once

  // Handle input change
  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSelectChange = (value: string, fieldName: keyof ListingFormData) => {
    setFormData(prev => ({
      ...prev,
      [fieldName]: value
    }));
  };

  // Handle checkbox change for features
  const handleFeatureChange = (feature: string) => {
    setFormData(prev => {
      if (prev.features.includes(feature)) {
        return {
          ...prev,
          features: prev.features.filter(f => f !== feature)
        };
      } else {
        return {
          ...prev,
          features: [...prev.features, feature]
        };
      }
    });
  };

  // Handle image upload
  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const newImages = Array.from(e.target.files);
      setFormData(prev => ({
        ...prev,
        images: [...prev.images, ...newImages]
      }));
    }
  };

  // Remove image
  const removeImage = (index: number) => {
    setFormData(prev => ({
      ...prev,
      images: prev.images.filter((_, i) => i !== index)
    }));
  };

  // Submit form
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsLoading(true);

    // In a real app, perform API call to update listing
    console.log("Updating listing data:", formData);

    // Simulate API call
    try {
      setTimeout(() => {
        setIsLoading(false);
        // Redirect to listings page after successful update
        router.push("/dashboard/listings");
      }, 1500);
    } catch (error) {
      console.error("Error updating listing:", error);
      setIsLoading(false);
      setError("Failed to update listing. Please try again.");
    }
  };

  // Handle listing renewal
  const handleRenewal = (id: string, duration: number) => {
    // In a real app, perform API call to renew listing
    console.log(`Renewing listing ${id} for ${duration} days`);

    // Update expiry date in the local state
    const newExpiry = new Date();
    newExpiry.setDate(newExpiry.getDate() + duration);

    setFormData(prev => ({
      ...prev,
      status: "active",
      expires: newExpiry.toISOString().split('T')[0]
    }));
  };

  if (!formData.id) {
    // This check might be too simple if MOCK_LISTING always has an ID.
    // In a real scenario, you'd have a loading state for the listing fetch.
    return <div>Loading listing data...</div>;
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6 p-4 md:p-6 lg:p-8 max-w-2xl mx-auto">
      <h1 className="text-2xl font-semibold">Edit Listing</h1>
      {error && <p className="text-red-500">{error}</p>}

      {/* Title */}
      <div>
        <Label htmlFor="title">Title</Label>
        <Input
          id="title"
          name="title"
          value={formData.title}
          onChange={handleChange}
          placeholder="e.g., Toyota Camry 2020"
          required
        />
      </div>

      {/* Description */}
      <div>
        <Label htmlFor="description">Description</Label>
        <Textarea
          id="description"
          name="description"
          value={formData.description}
          onChange={handleChange}
          placeholder="Detailed description of the car"
          required
        />
      </div>

      {/* Governorate and Location */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <Label htmlFor="governorateId">Governorate</Label>
          <Select
            name="governorateId"
            value={formData.governorateId}
            onValueChange={(value) => handleSelectChange(value, 'governorateId')}
            required
          >
            <SelectTrigger id="governorateId">
              <SelectValue placeholder={isLoadingGovernorates ? "Loading governorates..." : "Select Governorate"} />
            </SelectTrigger>
            <SelectContent>
              {isLoadingGovernorates ? (
                <SelectItem value="loading" disabled>Loading...</SelectItem>
              ) : (
                governorates.map((gov) => (
                  <SelectItem key={gov.id} value={String(gov.id)}>
                    {gov.name}
                  </SelectItem>
                ))
              )}
            </SelectContent>
          </Select>
        </div>
        <div>
          <Label htmlFor="location">Specific Location / Address</Label>
          <Input
            id="location"
            name="location"
            value={formData.location || ''}
            onChange={handleChange}
            placeholder="e.g., Street Name, Building No."
            // Not required anymore
          />
        </div>
      </div>
      
      {/* Make */}
      <div>
        <Label htmlFor="make">Make</Label>
        <Input
          id="make"
          name="make"
          value={formData.make}
          onChange={handleChange}
          placeholder="e.g., Toyota"
          required
        />
      </div>

      {/* Model */}
      <div>
        <Label htmlFor="model">Model</Label>
        <Input
          id="model"
          name="model"
          value={formData.model}
          onChange={handleChange}
          placeholder="e.g., Camry"
          required
        />
      </div>

      {/* Year */}
      <div>
        <Label htmlFor="year">Year</Label>
        <Input
          id="year"
          name="year"
          type="number"
          value={formData.year}
          onChange={handleChange}
          placeholder="e.g., 2020"
          required
        />
      </div>

      {/* Price */}
      <div>
        <Label htmlFor="price">Price</Label>
        <Input
          id="price"
          name="price"
          type="number"
          value={formData.price}
          onChange={handleChange}
          placeholder="e.g., 25000"
          required
        />
      </div>

      {/* Currency */}
      <div>
        <Label htmlFor="currency">Currency</Label>
        <Select
          name="currency"
          value={formData.currency}
          onValueChange={(value) => handleSelectChange(value, 'currency')}
          required
        >
          <SelectTrigger id="currency">
            <SelectValue placeholder="Select Currency" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="USD">USD</SelectItem>
            <SelectItem value="EUR">EUR</SelectItem>
            <SelectItem value="AED">AED</SelectItem>
            {/* Add more currencies as needed */}
          </SelectContent>
        </Select>
      </div>

      {/* Condition */}
      <div>
        <Label htmlFor="condition">Condition</Label>
        <Select
          name="condition"
          value={formData.condition}
          onValueChange={(value) => handleSelectChange(value, 'condition')}
          required
        >
          <SelectTrigger id="condition">
            <SelectValue placeholder="Select Condition" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="new">New</SelectItem>
            <SelectItem value="used">Used</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Mileage */}
      <div>
        <Label htmlFor="mileage">Mileage (km)</Label>
        <Input
          id="mileage"
          name="mileage"
          type="number"
          value={formData.mileage}
          onChange={handleChange}
          placeholder="e.g., 45000"
          required
        />
      </div>

      {/* Exterior Color */}
      <div>
        <Label htmlFor="exteriorColor">Exterior Color</Label>
        <Input
          id="exteriorColor"
          name="exteriorColor"
          value={formData.exteriorColor}
          onChange={handleChange}
          placeholder="e.g., Silver"
          required
        />
      </div>

      {/* Interior Color */}
      <div>
        <Label htmlFor="interiorColor">Interior Color</Label>
        <Input
          id="interiorColor"
          name="interiorColor"
          value={formData.interiorColor}
          onChange={handleChange}
          placeholder="e.g., Black"
          required
        />
      </div>

      {/* Transmission */}
      <div>
        <Label htmlFor="transmission">Transmission</Label>
        <Select
          name="transmission"
          value={formData.transmission}
          onValueChange={(value) => handleSelectChange(value, 'transmission')}
          required
        >
          <SelectTrigger id="transmission">
            <SelectValue placeholder="Select Transmission" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="automatic">Automatic</SelectItem>
            <SelectItem value="manual">Manual</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Fuel Type */}
      <div>
        <Label htmlFor="fuelType">Fuel Type</Label>
        <Select
          name="fuelType"
          value={formData.fuelType}
          onValueChange={(value) => handleSelectChange(value, 'fuelType')}
          required
        >
          <SelectTrigger id="fuelType">
            <SelectValue placeholder="Select Fuel Type" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="gasoline">Gasoline</SelectItem>
            <SelectItem value="diesel">Diesel</SelectItem>
            <SelectItem value="electric">Electric</SelectItem>
            <SelectItem value="hybrid">Hybrid</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Features */}
      <div>
        <Label>Features</Label>
        <div className="grid grid-cols-2 gap-4">
          {carFeatures.map((feature) => (
            <div key={feature} className="flex items-center">
              <input
                type="checkbox"
                id={feature}
                checked={formData.features.includes(feature)}
                onChange={() => handleFeatureChange(feature)}
                className="mr-2"
              />
              <label htmlFor={feature} className="text-sm">{feature}</label>
            </div>
          ))}
        </div>
      </div>

      {/* Images */}
      <div>
        <Label>Images</Label>
        <input
          type="file"
          accept="image/*"
          multiple
          onChange={handleImageUpload}
          className="mt-1 block w-full text-sm"
        />
        <div className="mt-2 grid grid-cols-3 gap-2">
          {formData.images.map((image, index) => (
            <div key={index} className="relative">
              <img src={URL.createObjectURL(image)} alt={`Uploaded image ${index + 1}`} className="w-full h-auto rounded-md" />
              <button
                type="button"
                onClick={() => removeImage(index)}
                className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 text-xs"
              >
                &times;
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Section for Listing Status and Expiry */}
      <div className="mt-6 p-4 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 shadow">
        <h3 className="text-xl font-semibold mb-4 text-gray-900 dark:text-white">Listing Status & Expiry</h3>
        {formData.expires && formData.expires.trim() !== "" ? (
          <ListingExpiry
            listingId={id}
            expiryDate={formData.expires}
            status={formData.status as 'active' | 'expired' | 'pending'}
            onRenew={handleRenewal}
          />
        ) : (
          <p className="text-gray-600 dark:text-gray-400">
            Expiry date is not currently set for this listing.
          </p>
        )}
      </div>

      <div className="flex justify-end space-x-3">
        <Button
          type="button"
          onClick={() => router.push('/dashboard/listings')}
          variant="outline"
          className="px-4 py-2 text-sm"
        >
          Cancel
        </Button>
        <Button
          type="submit"
          isLoading={isLoading}
          className="px-4 py-2 text-sm bg-primary text-white rounded-md hover:bg-primary-dark"
        >
          Save Changes
        </Button>
      </div>
    </form>
  );
}
