#!/bin/bash
# Script to run Postman tests locally

# Check if Newman is installed
if ! command -v newman &> /dev/null; then
    echo "Newman is not installed. Installing now..."
    npm install -g newman newman-reporter-htmlextra
fi

# Create results directory
mkdir -p results

echo "Running Postman collection tests..."

# Try to find the right collection file
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

# Run the tests
newman run "$COLLECTION_PATH" \
    --environment ./postman/test_environment.json \
    --reporters cli,htmlextra \
    --reporter-htmlextra-export results/html-report.html

# Check if tests passed
if [ $? -eq 0 ]; then
    echo "✅ Postman tests completed successfully"
    echo "HTML report available at: $(pwd)/results/html-report.html"
else
    echo "❌ Postman tests failed"
    echo "HTML report available at: $(pwd)/results/html-report.html"
fi
