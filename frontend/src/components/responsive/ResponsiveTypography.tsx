'use client';

import React, { memo } from 'react';
import { responsiveFontSize } from '@/utils/responsive';
import type { 
  ResponsiveTextProps,
  FontSizeType,
  ComponentType,
} from '@/types/responsive';

/**
 * A responsive typography component that adjusts text styling based on screen size
 */
const ResponsiveText = ({
  children,
  size = 'base',
  component: Component = 'p',
  className = '',
  weight = 'normal',
  color = 'text-gray-900 dark:text-gray-100',
  align = 'left',
  id,
  testId,
}: ResponsiveTextProps) => {
  // Map string size to numeric value for responsiveFontSize
  const fontSizeMap: Record<FontSizeType, number> = {
    xs: 0.75,
    sm: 0.875,
    base: 1,
    lg: 1.125,
    xl: 1.25,
    '2xl': 1.5,
    '3xl': 1.875,
    '4xl': 2.25,
    '5xl': 3,
  };
  const sizeValue = fontSizeMap[size] || fontSizeMap.base;
  const sizeClasses = responsiveFontSize(sizeValue);
  
  // Generate weight classes
  const weightClass = weight === 'normal' 
    ? 'font-normal' 
    : weight === 'medium' 
      ? 'font-medium'
      : weight === 'semibold'
        ? 'font-semibold'
        : 'font-bold';
  
  // Generate alignment class
  const alignClass = align === 'center'
    ? 'text-center'
    : align === 'right'
      ? 'text-right'
      : 'text-left';
  
  return (
    <Component 
      className={`${sizeClasses} ${weightClass} ${color} ${alignClass} ${className}`}
      id={id}
      data-testid={testId}
    >
      {children}
    </Component>
  );
};

// Add display name for better debugging
ResponsiveText.displayName = 'ResponsiveText';

/**
 * Props for ResponsiveHeading component
 */
interface ResponsiveHeadingProps extends Omit<ResponsiveTextProps, 'component'> {
  /** Heading level from h1 to h6 */
  level: 1 | 2 | 3 | 4 | 5 | 6;
}

/**
 * A responsive heading component with different levels (h1-h6)
 * Automatically assigns appropriate font sizes based on heading level if not explicitly specified
 * 
 * @param {ResponsiveHeadingProps} props - Component properties
 * @returns {React.ReactElement} - Rendered heading component
 */
export const ResponsiveHeading = memo<ResponsiveHeadingProps>(({
  level,
  children,
  size,
  className = '',
  weight = 'bold',
  color = 'text-gray-900 dark:text-gray-100',
  align = 'left',
  id,
  testId,
}) => {
  // Default size based on heading level if not specified
  const defaultSize: Record<number, FontSizeType> = {
    1: '4xl',
    2: '3xl',
    3: '2xl',
    4: 'xl',
    5: 'lg',
    6: 'base'
  };
  
  const headingSize = size || defaultSize[level];
  const Component = `h${level}` as ComponentType;
  
  return (
    <ResponsiveText
      component={Component}
      size={headingSize}
      className={className}
      weight={weight}
      color={color}
      align={align}
      id={id}
      testId={testId}
    >
      {children}
    </ResponsiveText>
  );
});

// Add display name for better debugging
ResponsiveHeading.displayName = 'ResponsiveHeading';
