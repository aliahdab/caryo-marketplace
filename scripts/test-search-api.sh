#!/bin/bash

# Test script for validating the search functionality
# This script runs a series of curl commands to test the search endpoints

echo "Testing Search API Endpoints"
echo "============================"

BASE_URL="http://localhost:8080/api"

# Function to make a GET request and display the result
function test_get() {
    local endpoint=$1
    local description=$2
    
    echo -e "\n$description"
    echo "GET $endpoint"
    curl -s -X GET "$BASE_URL$endpoint" | jq
    echo -e "\n--------------------"
}

# Test quick search
test_get "/search/quick?term=toyota" "Testing quick search for Toyota"
test_get "/search/quick?governorateId=1" "Testing quick search in Damascus"
test_get "/search/quick?term=toyota&governorateId=1" "Testing quick search for Toyota in Damascus"
test_get "/search/quick?term=toyota&language=ar" "Testing quick search in Arabic"

# Test brand search
test_get "/search/by-brand?brand=toyota" "Testing search by brand: Toyota"
test_get "/search/by-brand?brand=bmw&language=ar" "Testing search by brand in Arabic: BMW"

# Test model search
test_get "/search/by-model?model=camry" "Testing search by model: Camry"
test_get "/search/by-model?model=x5&language=ar" "Testing search by model in Arabic: X5"

# Test governorate search
test_get "/search/by-governorate?governorate=damascus" "Testing search by governorate: Damascus"
test_get "/search/by-governorate?governorate=حلب&language=ar" "Testing search by governorate in Arabic: Aleppo"

# Test governorate API
test_get "/governorates" "Testing get all governorates"
test_get "/governorates/1" "Testing get governorate by ID"
test_get "/governorates/slug/damascus" "Testing get governorate by slug"
test_get "/governorates/country/SY" "Testing get governorates by country code"
test_get "/governorates/search?name=dam" "Testing search governorates by name"

echo "All tests completed"
