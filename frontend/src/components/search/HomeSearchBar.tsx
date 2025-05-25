"use client";

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';

import { CarMake, CarModel } from '@/types/car';
import { fetchCarBrands, fetchCarModels } from '@/services/api';

const HomeSearchBar: React.FC = () => {
  const { t, i18n } = useTranslation('common');
  const router = useRouter();
  
  const [selectedMake, setSelectedMake] = useState<number | null>(null);
  const [selectedModel, setSelectedModel] = useState<number | null>(null);
  const [location, setLocation] = useState<string>('');
  
  const [carMakes, setCarMakes] = useState<CarMake[]>([]);
  const [availableModels, setAvailableModels] = useState<CarModel[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Get display name based on current language
  const getDisplayName = (item: { displayNameEn: string; displayNameAr: string }) => {
    return i18n.language === 'ar' ? item.displayNameAr : item.displayNameEn;
  };
  
  // Fetch car brands on component mount
  useEffect(() => {
    const loadBrands = async () => {
      try {
        setIsLoading(true);
        setError(null);
        console.log('Fetching car brands...');
        const brands = await fetchCarBrands();
        console.log('Received brands:', brands);
        if (!brands || !Array.isArray(brands)) {
          throw new Error('Invalid response format for brands');
        }
        setCarMakes(brands);
      } catch (err) {
        console.error('Failed to fetch car brands:', err);
        setError(t('search.errorLoadingBrands'));
        setCarMakes([]); // Reset brands on error
      } finally {
        setIsLoading(false);
      }
    };

    loadBrands();
  }, [t]);
  
  // Update available models when make changes
  useEffect(() => {
    const loadModels = async () => {
      if (selectedMake !== null) {
        try {
          setIsLoading(true);
          setError(null);
          console.log('Fetching models for brand:', selectedMake);
          const models = await fetchCarModels(selectedMake);
          console.log('Received models:', models);
          if (!models || !Array.isArray(models)) {
            throw new Error('Invalid response format for models');
          }
          setAvailableModels(models);
          setSelectedModel(null); // Reset model when make changes
        } catch (err) {
          console.error('Failed to fetch car models:', err);
          setError(t('search.errorLoadingModels'));
          setAvailableModels([]);
        } finally {
          setIsLoading(false);
        }
      } else {
        setAvailableModels([]);
        setSelectedModel(null);
      }
    };

    loadModels();
  }, [selectedMake, t]);
  
  const locations = ['Damascus', 'Aleppo', 'Homs', 'Latakia', 'Hama', 'Tartus'];
  
  const handleSearch = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    
    const params = new URLSearchParams();
    
    if (selectedMake !== null) {
      params.append('make', selectedMake.toString());
      
      // Find the make for display name
      const selectedBrand = carMakes.find(make => make.id === selectedMake);
      if (selectedBrand) {
        params.append('makeName', getDisplayName(selectedBrand));
      }
    }
    
    if (selectedModel !== null) {
      params.append('model', selectedModel.toString());
      
      // Find the model for display name
      const selectedCarModel = availableModels.find(model => model.id === selectedModel);
      if (selectedCarModel) {
        params.append('modelName', getDisplayName(selectedCarModel));
      }
    }
    
    if (location) {
      params.append('location', location);
    }

    router.push(`/listings?${params.toString()}`);
  };

  return (
    <form onSubmit={handleSearch} className="w-full">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 p-4">
          {/* Brand Select */}
          <div className="col-span-1">
            <label htmlFor="brand" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              {t('search.selectBrand', 'Brand')}
            </label>
            <select
              id="brand"
              value={selectedMake ?? ''}
              onChange={(e) => setSelectedMake(e.target.value ? Number(e.target.value) : null)}
              className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              disabled={isLoading}
            >
              <option value="">{t('search.selectBrand', 'Select Brand')}</option>
              {carMakes.map((make) => (
                <option key={make.id} value={make.id}>{getDisplayName(make)}</option>
              ))}
            </select>
            {isLoading && !selectedMake && (
              <div className="mt-1 text-sm text-gray-500 dark:text-gray-400">{t('search.loadingBrands', 'Loading brands...')}</div>
            )}
          </div>

          {/* Model Select */}
          <div className="col-span-1">
            <label htmlFor="model" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              {t('search.selectModel', 'Model')}
            </label>
            <select
              id="model"
              value={selectedModel ?? ''}
              onChange={(e) => setSelectedModel(e.target.value ? Number(e.target.value) : null)}
              className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              disabled={!selectedMake || isLoading}
            >
              <option value="">{t('search.selectModel', 'Select Model')}</option>
              {availableModels.map((model) => (
                <option key={model.id} value={model.id}>{getDisplayName(model)}</option>
              ))}
            </select>
            {isLoading && selectedMake && (
              <div className="mt-1 text-sm text-gray-500 dark:text-gray-400">{t('search.loadingModels', 'Loading models...')}</div>
            )}
          </div>

          {/* Location Select */}
          <div className="col-span-1">
            <label htmlFor="location" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              {t('search.location', 'Location')}
            </label>
            <select
              id="location"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">{t('search.enterLocation', 'Enter Location')}</option>
              {locations.map((loc) => (
                <option key={loc} value={loc}>{loc}</option>
              ))}
            </select>
          </div>

          {/* Search Button */}
          <div className="col-span-1 flex items-end">
            <button
              type="submit"
              className="w-full p-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-md shadow-md hover:shadow-lg transition-all duration-200"
            >
              {t('search.searchButton', 'Search Cars')}
            </button>
          </div>
        </div>

        {error && (
          <div className="px-4 pb-4 text-red-500 text-sm flex items-center justify-between">
            <span>{error}</span>
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="text-blue-500 hover:text-blue-700 text-sm font-medium"
            >
              {t('search.tryAgain', 'Try Again')}
            </button>
          </div>
        )}
      </div>
    </form>
  );
};

export default HomeSearchBar;
