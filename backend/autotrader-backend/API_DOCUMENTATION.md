# Autotrader Marketplace API Documentation

This document provides detailed information about the available API endpoints in the Autotrader Marketplace backend service.

## Quick Start Guide

### Starting the Application
```bash
# Navigate to the project directory
cd backend/autotrader-backend

# Start the Spring Boot application
./gradlew bootRun
```

### Testing Authentication Flow
```bash
# 1. Register a new user
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser1","email":"testuser1@example.com","password":"password123"}'

# 2. Login to get a JWT token
curl -X POST http://localhost:8080/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser1","password":"password123"}'

# 3. Save the token from the response
# Example: TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# 4. Use the token to access protected endpoints
curl -X GET http://localhost:8080/api/listings/my-listings \
  -H "Authorization: Bearer $TOKEN"
```

### Running Postman Tests

#### Automated Testing Script
```bash
# Run all API tests automatically
./src/test/scripts/run_postman_tests.sh
```
This script will start the application, run all tests, and shut down when complete.
The HTML report will be available at `build/test-reports/postman/report.html`.

#### Manual Testing with Postman
1. Start the application with `./gradlew bootRun`
2. Import the collection and environment into Postman:
   - Collection: `src/test/resources/postman/autotrader-api-tests.json`
   - Environment: `src/test/resources/postman/environment.json`
3. Run the collection in Postman

## Base URL

For local development: `http://localhost:8080`

## Authentication

Most endpoints require authentication using JWT tokens. Include the token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## API Endpoints

### Authentication

#### Register a User

- **Endpoint**: `POST /auth/signup`
- **Access**: Public
- **Description**: Creates a new user account
- **Request Body**:
  ```json
  {
    "username": "yourUsername",
    "email": "your.email@example.com",
    "password": "yourPassword"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "message": "User registered successfully!"
  }
  ```
- **Response (400 Bad Request)** - Username/email already taken:
  ```json
  {
    "message": "Error: Username is already taken!"
  }
  ```

#### Login User

- **Endpoint**: `POST /auth/signin`
- **Access**: Public
- **Description**: Authenticates a user and returns a JWT token
- **Request Body**:
  ```json
  {
    "username": "yourUsername",
    "password": "yourPassword"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "id": 1,
    "username": "yourUsername",
    "email": "your.email@example.com",
    "roles": ["ROLE_USER"],
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
  ```
- **Response (401 Unauthorized)** - Invalid credentials:
  ```json
  {
    "message": "Invalid username or password"
  }
  ```

### Car Listings

#### Create Car Listing

- **Endpoint**: `POST /api/listings`
- **Access**: Authenticated users
- **Description**: Creates a new car listing
- **Authentication**: Required (JWT token)
- **Request Body**:
  ```json
  {
    "title": "2019 Toyota Camry",
    "brand": "Toyota",
    "model": "Camry",
    "modelYear": 2019,
    "price": 18500,
    "mileage": 35000,
    "location": "New York, NY",
    "description": "Excellent condition, one owner, no accidents",
    "imageUrl": "https://example.com/camry.jpg"
  }
  ```
- **Response (201 Created)**:
  ```json
  {
    "id": 1,
    "title": "2019 Toyota Camry",
    "brand": "Toyota",
    "model": "Camry",
    "modelYear": 2019,
    "price": 18500,
    "mileage": 35000,
    "location": "New York, NY",
    "description": "Excellent condition, one owner, no accidents",
    "imageUrl": "https://example.com/camry.jpg",
    "approved": false,
    "userId": 1,
    "createdAt": "2025-04-30T10:15:30Z",
    "updatedAt": null
  }
  ```
- **Response (400 Bad Request)** - Invalid data:
  ```json
  {
    "message": "Error: Invalid listing data",
    "errors": {
      "price": "Price must be greater than 0",
      "title": "Title is required"
    }
  }
  ```

#### Get User's Listings

- **Endpoint**: `GET /api/listings/my-listings`
- **Access**: Authenticated users
- **Description**: Retrieves all listings created by the authenticated user
- **Authentication**: Required (JWT token)
- **Response (200 OK)**:
  ```json
  [
    {
      "id": 1,
      "title": "2019 Toyota Camry",
      "brand": "Toyota",
      "model": "Camry",
      "modelYear": 2019,
      "price": 18500,
      "mileage": 35000,
      "location": "New York, NY",
      "description": "Excellent condition, one owner, no accidents",
      "imageUrl": "https://example.com/camry.jpg",
      "approved": false,
      "userId": 1,
      "createdAt": "2025-04-30T10:15:30Z",
      "updatedAt": null
    }
  ]
  ```

#### Favorites

#### Add Listing to Favorites

- **Endpoint**: `POST /api/favorites/{listingId}`
- **Access**: Authenticated users
- **Description**: Adds a car listing to the user's favorites/watchlist
- **Authentication**: Required (JWT token)
- **Parameters**:
  - `listingId` (path parameter): ID of the car listing to add to favorites
- **Response (200 OK)**:
  ```json
  {
    "id": 1,
    "carListing": {
      "id": 123,
      "title": "2019 Toyota Camry",
      // ... other car listing fields
    },
    "createdAt": "2024-03-21T10:15:30Z"
  }
  ```
- **Response (404 Not Found)**:
  ```json
  {
    "status": 404,
    "message": "Resource not found",
    "details": "Car listing with id '123' was not found",
    "timestamp": "2024-03-21T10:15:30.123Z"
  }
  ```
- **Response (401 Unauthorized)**:
  ```json
  {
    "status": 401,
    "message": "Unauthorized",
    "details": "User must be authenticated",
    "timestamp": "2024-03-21T10:15:30.123Z"
  }
  ```

#### Remove Listing from Favorites

- **Endpoint**: `DELETE /api/favorites/{listingId}`
- **Access**: Authenticated users
- **Description**: Removes a car listing from the user's favorites/watchlist
- **Authentication**: Required (JWT token)
- **Parameters**:
  - `listingId` (path parameter): ID of the car listing to remove from favorites
- **Response (200 OK)**: Empty response
- **Response (404 Not Found)**:
  ```json
  {
    "status": 404,
    "message": "Resource not found",
    "details": "Car listing with id '123' was not found",
    "timestamp": "2024-03-21T10:15:30.123Z"
  }
  ```
- **Response (401 Unauthorized)**:
  ```json
  {
    "status": 401,
    "message": "Unauthorized",
    "details": "User must be authenticated",
    "timestamp": "2024-03-21T10:15:30.123Z"
  }
  ```

#### Get User's Favorites

- **Endpoint**: `GET /api/favorites`
- **Access**: Authenticated users
- **Description**: Returns all car listings that the user has marked as favorite/watchlist
- **Authentication**: Required (JWT token)
- **Response (200 OK)**:
  ```json
  [
    {
      "id": 123,
      "title": "2019 Toyota Camry",
      "brand": "Toyota",
      "model": "Camry",
      "modelYear": 2019,
      "price": 18500,
      "mileage": 35000,
      "location": "New York, NY",
      "description": "Excellent condition, one owner, no accidents",
      "imageUrl": "https://example.com/camry.jpg",
      "approved": true,
      "userId": 1,
      "createdAt": "2024-03-21T10:15:30Z",
      "updatedAt": null
    }
    // ... more listings
  ]
  ```
- **Response (401 Unauthorized)**:
  ```json
  {
    "status": 401,
    "message": "Unauthorized",
    "details": "User must be authenticated",
    "timestamp": "2024-03-21T10:15:30.123Z"
  }
  ```

#### Check if Listing is Favorite

- **Endpoint**: `GET /api/favorites/{listingId}/check`
- **Access**: Authenticated users
- **Description**: Checks if a specific car listing is in the user's favorites/watchlist
- **Authentication**: Required (JWT token)
- **Parameters**:
  - `listingId` (path parameter): ID of the car listing to check
- **Response (200 OK)**:
  ```json
  true
  ```
  or
  ```json
  false
  ```
- **Response (404 Not Found)**:
  ```json
  {
    "status": 404,
    "message": "Resource not found",
    "details": "Car listing with id '123' was not found",
    "timestamp": "2024-03-21T10:15:30.123Z"
  }
  ```
- **Response (401 Unauthorized)**:
  ```json
  {
    "status": 401,
    "message": "Unauthorized",
    "details": "User must be authenticated",
    "timestamp": "2024-03-21T10:15:30.123Z"
  }
  ```

### Status Endpoints

#### Check Service Status

- **Endpoint**: `GET /status`
- **Access**: Public
- **Description**: Simple health check to verify the service is running
- **Response (200 OK)**:
  ```json
  {
    "status": "Service is up and running",
    "timestamp": "2025-04-30T12:34:56Z"
  }
  ```

#### Check API Status

- **Endpoint**: `GET /api/status`
- **Access**: Public
- **Description**: Health check for the API layer
- **Response (200 OK)**:
  ```json
  {
    "status": "API is up and running",
    "timestamp": "2025-04-30T12:34:56Z"
  }
  ```

## Error Responses

### Standard Error Format

Most error responses follow this format:

```json
{
  "timestamp": "2025-04-30T12:34:56Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Error message details",
  "path": "/api/endpoint"
}
```

### Common HTTP Status Codes

- **200 OK**: Request succeeded
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid request data
- **401 Unauthorized**: Authentication required or failed
- **403 Forbidden**: Authenticated but not authorized
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

## Testing the API

### Using cURL

#### Register a User
```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser1","email":"testuser1@example.com","password":"password123"}'
```

#### Login
```bash
curl -X POST http://localhost:8080/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser1","password":"password123"}'
```

#### Create a Car Listing (after login)
```bash
# First get a token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser1","password":"password123"}' | grep -o '"accessToken":"[^"]*' | cut -d':' -f2 | tr -d '"')

# Then create a listing
curl -X POST http://localhost:8080/api/listings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"2019 Toyota Camry","brand":"Toyota","model":"Camry","modelYear":2019,"price":18500,"mileage":35000,"location":"New York, NY","description":"Excellent condition, one owner, no accidents","imageUrl":"https://example.com/camry.jpg"}'
```

#### Get Your Listings
```bash
# Using the token from previous step
curl -X GET http://localhost:8080/api/listings/my-listings \
  -H "Authorization: Bearer $TOKEN"
```

#### Add a Listing to Favorites
```bash
# Using the token from previous step
curl -X POST http://localhost:8080/api/favorites/123 \
  -H "Authorization: Bearer $TOKEN"
```

#### Remove a Listing from Favorites
```bash
# Using the token from previous step
curl -X DELETE http://localhost:8080/api/favorites/123 \
  -H "Authorization: Bearer $TOKEN"
```

#### Get Your Favorite Listings
```bash
# Using the token from previous step
curl -X GET http://localhost:8080/api/favorites \
  -H "Authorization: Bearer $TOKEN"
```

#### Check if a Listing is in Your Favorites
```bash
# Using the token from previous step
curl -X GET http://localhost:8080/api/favorites/123/check \
  -H "Authorization: Bearer $TOKEN"
```

### Using Postman

Import the collection and environment files:
- Collection: `src/test/resources/postman/autotrader-api-tests.json`
- Environment: `src/test/resources/postman/environment.json`

### Using the Automated Test Script

To run all API tests automatically:
```bash
./src/test/scripts/run_postman_tests.sh
```

## Core Endpoints

- `GET /api/listings` - Get all approved car listings (public)
- `GET /api/listings/{id}` - Get details of a specific car listing (public)
- `PUT /api/listings/{id}` - Update a listing (authenticated owner only)
- `DELETE /api/listings/{id}` - Delete a listing (authenticated owner only)
- `POST /api/admin/listings/{id}/approve` - Approve a listing (admin only)
- `PUT /api/listings/{id}/pause` - Pause a listing (authenticated owner only)
- `PUT /api/listings/{id}/resume` - Resume a listing (authenticated owner only)
- `POST /api/listings/{id}/mark-sold` - Mark a listing as sold (authenticated owner only)
- `POST /api/listings/{id}/archive` - Archive a listing (authenticated owner only)
- `POST /api/listings/{id}/unarchive` - Unarchive a listing (authenticated owner only)

### Listing Management (Admin)

- **POST** `/api/admin/listings/{id}/unarchive`
  - Description: Unarchive any listing.
- **DELETE** /api/listings/admin/{id}
  - Description: Delete any listing.

### Listing Endpoints (Detailed)

#### Create Listing (No Image)
- **Endpoint**: `POST /api/listings` (consumes application/json)
- **Access**: Authenticated users
- **Description**: Creates a new car listing without an image
- **Authentication**: Required (JWT token)
- **Request Body**: JSON representation of listing details
- **Response**: Created listing with status 201

#### Create Listing With Image
- **Endpoint**: `POST /api/listings/with-image` (consumes multipart/form-data)
- **Access**: Authenticated users
- **Description**: Creates a new car listing with an initial image
- **Authentication**: Required (JWT token)
- **Request Parts**: 
  - `listing`: JSON representation of listing details
  - `image`: Image file (JPEG, PNG, etc.)
- **Response**: Created listing with media and status 201

#### Upload Image for Listing
- **Endpoint**: `POST /api/listings/{listingId}/upload-image` (consumes multipart/form-data)
- **Access**: Authenticated owner of the listing
- **Description**: Uploads an image for an existing listing
- **Authentication**: Required (JWT token)
- **Parameters**:
  - `listingId`: ID of the listing
  - `file`: Image file to upload
- **Response**: Success message with image key

#### Filter Listings (GET)
- **Endpoint**: `GET /api/listings/filter`
- **Access**: Public
- **Description**: Returns filtered listings using query parameters
- **Parameters**: brand, model, minYear, maxYear, location, locationId, minPrice, maxPrice, minMileage, maxMileage, isSold, isArchived
- **Response**: Paginated list of listings

#### Filter Listings (POST)
- **Endpoint**: `POST /api/listings/filter`
- **Access**: Public
- **Description**: Returns filtered listings using request body
- **Request Body**: JSON with filter criteria
- **Response**: Paginated list of listings

## Pause & Resume Functionality

### Pause a Listing

- **Endpoint**: `PUT /api/listings/{id}/pause`
- **Access**: Authenticated owner of the listing
- **Description**: Temporarily hides a listing from public view while preserving all its data
- **Authentication**: Required (JWT token)
- **Parameters**:
  - `id` (path parameter): ID of the car listing to pause
- **Response (200 OK)**:
  ```json
  {
    "id": 123,
    "title": "2019 Toyota Camry",
    "brand": "Toyota",
    "model": "Camry",
    "isUserActive": false,
    "approved": true,
    // ... other listing fields
  }
  ```
- **Response (401 Unauthorized)**:
  ```json
  {
    "message": "Unauthorized"
  }
  ```
- **Response (403 Forbidden)**:
  ```json
  {
    "message": "You are not authorized to pause this listing"
  }
  ```
- **Response (404 Not Found)**:
  ```json
  {
    "message": "Listing not found"
  }
  ```
- **Response (409 Conflict)**:
  ```json
  {
    "message": "Listing is already paused or cannot be paused in its current state"
  }
  ```

### Resume a Listing

- **Endpoint**: `PUT /api/listings/{id}/resume`
- **Access**: Authenticated owner of the listing
- **Description**: Makes a previously paused listing visible again in public listings
- **Authentication**: Required (JWT token)
- **Parameters**:
  - `id` (path parameter): ID of the car listing to resume
- **Response (200 OK)**:
  ```json
  {
    "id": 123,
    "title": "2019 Toyota Camry",
    "brand": "Toyota",
    "model": "Camry",
    "isUserActive": true,
    "approved": true,
    // ... other listing fields
  }
  ```
- **Response (401 Unauthorized)**:
  ```json
  {
    "message": "Unauthorized"
  }
  ```
- **Response (403 Forbidden)**:
  ```json
  {
    "message": "You are not authorized to resume this listing"
  }
  ```
- **Response (404 Not Found)**:
  ```json
  {
    "message": "Listing not found"
  }
  ```
- **Response (409 Conflict)**:
  ```json
  {
    "message": "Listing is already active or cannot be resumed in its current state"
  }
  ```

### Reference Data Endpoints

#### Car Conditions
- **Endpoint**: `GET /api/car-conditions`
- **Access**: Public
- **Description**: Returns all car condition options
- **Response**: List of car conditions

#### Drive Types
- **Endpoint**: `GET /api/drive-types`
- **Access**: Public
- **Description**: Returns all drive type options
- **Response**: List of drive types

#### Body Styles
- **Endpoint**: `GET /api/body-styles`
- **Access**: Public
- **Description**: Returns all body style options
- **Response**: List of body styles

#### Transmissions
- **Endpoint**: `GET /api/transmissions`
- **Access**: Public
- **Description**: Returns all transmission options
- **Response**: List of transmissions

#### Commonly Used Values

- **Car Brands**: Toyota, Ford, Honda, BMW, Tesla
- **Car Models**: Camry, Mustang, Civic, 3 Series, Model S
- **Locations**: New York, NY; Los Angeles, CA; Chicago, IL; Houston, TX; Phoenix, AZ
