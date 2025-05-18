#!/bin/bash
# Script to debug and run Postman tests

# Show current directory
echo "Current directory: $(pwd)"

# Check if test_environment.json exists
if [ -f "./postman/test_environment.json" ]; then
  echo "Environment file found at ./postman/test_environment.json"
  ls -la ./postman/test_environment.json
else
  echo "Environment file NOT found at ./postman/test_environment.json"
  
  # Check for file at absolute path
  FILE_PATH="/Users/aliahdab/Documents/Dev/caryo-marketplace/postman/test_environment.json"
  if [ -f "$FILE_PATH" ]; then
    echo "Environment file found at absolute path: $FILE_PATH"
    
    # Create results directory
    mkdir -p results
    
    # Run the tests with absolute path
    echo "Running Postman tests with absolute path..."
    COLLECTION_PATH=""
    if [ -f "./postman/Caryo_Marketplace_API_Tests.json" ]; then
      COLLECTION_PATH="./postman/Caryo_Marketplace_API_Tests.json"
    elif [ -f "./backend/autotrader-backend/src/test/resources/postman/autotrader-api-collection.json" ]; then
      COLLECTION_PATH="./backend/autotrader-backend/src/test/resources/postman/autotrader-api-collection.json"
      echo "Using autotrader-api-collection.json as collection file"
    else
      echo "ERROR: Postman collection file not found"
      exit 1
    fi
    
    newman run "$COLLECTION_PATH" \
      --environment "$FILE_PATH" \
      --reporters cli,htmlextra \
      --reporter-htmlextra-export results/html-report.html
    
    echo "HTML report available at: $(pwd)/results/html-report.html"
  else
    echo "Environment file also NOT found at absolute path: $FILE_PATH"
    echo "Searching for environment.json files in project..."
    find /Users/aliahdab/Documents/Dev/caryo-marketplace -name "environment.json" -o -name "test_environment.json"
  fi
fi
