"use client";

import React from 'react';
import { useTranslation } from 'react-i18next';
import { formatNumber } from '../../../../utils/localization';
import { Listing } from '@/types/listings';
import { 
  Tag, 
  Car, 
  CalendarDays, 
  Fuel, 
  Cog, 
  Gauge, 
  Paintbrush, 
  CheckCircle2 
} from 'lucide-react';

interface CarFactsProps {
  listing: Listing;
}

const CarFacts: React.FC<CarFactsProps> = ({ listing }) => {
  const { t, i18n } = useTranslation('common');

  const facts = [
    // Brand (Märke)
    {
      label: t('brand'),
      value: listing.make || listing.brand || '-',
      icon: <Tag className="w-5 h-5 text-blue-600" />
    },
    // Model (Modell)
    {
      label: t('model'),
      value: listing.model || '-',
      icon: <Car className="w-5 h-5 text-blue-600" />
    },
    // Model Year (Modellår)
    {
      label: t('modelYear'),
      value: listing.year ? listing.year.toString() : '-',
      icon: <CalendarDays className="w-5 h-5 text-blue-600" />
    },
    // Fuel Type (Bränsle)
    {
      label: t('fuelType'),
      value: listing.fuelType || '-',
      icon: <Fuel className="w-5 h-5 text-blue-600" />
    },
    // Transmission (Växellåda)
    {
      label: t('transmission'),
      value: listing.transmission || '-',
      icon: <Cog className="w-5 h-5 text-blue-600" />
    },
    // Mileage (Miltal)
    {
      label: t('mileage'),
      value: listing.mileage ? `${formatNumber(listing.mileage, i18n.language)} ${t('km')}` : '-',
      icon: <Gauge className="w-5 h-5 text-blue-600" />
    },
    // Color (Färg)
    {
      label: t('color'),
      value: listing.exteriorColor || '-',
      icon: <Paintbrush className="w-5 h-5 text-blue-600" />
    },
    // Condition
    {
      label: t('condition'),
      value: listing.condition || '-',
      icon: <CheckCircle2 className="w-5 h-5 text-blue-600" />
    }
  ];

  return (
    <div className="mb-6 sm:mb-8">
      <h2 className="text-xl sm:text-2xl font-semibold mb-4 text-gray-900 dark:text-white flex items-center">
        <Car className="w-5 h-5 sm:w-6 sm:h-6 mr-2 rtl:ml-2 rtl:mr-0 text-blue-600" />
        {t('listings.specifications')}
      </h2>
      
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4 sm:p-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {facts.map((fact, index) => (
            <div 
              key={index}
              className="flex items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors"
            >
              <div className="flex-shrink-0 mr-3 rtl:ml-3 rtl:mr-0">
                {fact.icon}
              </div>
              <div className="flex-grow min-w-0">
                <div className="text-sm text-gray-600 dark:text-gray-400 mb-1">
                  {fact.label}
                </div>
                <div className="text-sm font-medium text-gray-900 dark:text-white truncate">
                  {fact.value}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default CarFacts;
