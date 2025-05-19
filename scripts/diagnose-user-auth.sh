#!/bin/bash

# Script to diagnose user authentication issues in Spring Boot application
# Usage: ./diagnose-user-auth.sh [baseUrl]

BASE_URL=${1:-"http://localhost:8080"}
echo "Testing authentication at: $BASE_URL"

# Check if application is running
echo "Checking if application is running..."
HEALTH_RESPONSE=$(curl -s "$BASE_URL/actuator/health")
if [[ "$HEALTH_RESPONSE" == *"UP"* ]]; then
  echo "✅ Application is running!"
else
  echo "❌ Application does not appear to be running. Health check failed."
  echo "Response: $HEALTH_RESPONSE"
  exit 1
fi

# Try to authenticate with admin user
echo -e "\nTrying to authenticate with admin user..."
ADMIN_AUTH=$(curl -s -X POST "$BASE_URL/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}')

# Extract token if possible
ADMIN_TOKEN=$(echo "$ADMIN_AUTH" | jq -r '.token // .accessToken // .access_token // ""')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" == "null" ]; then
  echo "❌ Admin authentication failed"
  echo "Response: $ADMIN_AUTH"
else
  echo "✅ Admin authentication successful"
  echo "Token received (truncated): ${ADMIN_TOKEN:0:25}..."
  
  # Test a protected endpoint
  echo -e "\nTesting protected endpoint with admin token..."
  ADMIN_TEST=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/api/test/admin")
  echo "Response: $ADMIN_TEST"
fi

# Try to create a test user
echo -e "\nCreating test user..."
TEST_USER_CREATION=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"username":"testdiagnose","email":"testdiagnose@example.com","password":"password123"}')
echo "Response: $TEST_USER_CREATION"

sleep 2

# Try to authenticate with test user
echo -e "\nTrying to authenticate with test user..."
TEST_AUTH=$(curl -s -X POST "$BASE_URL/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{"username":"testdiagnose","password":"password123"}')

# Extract token if possible
TEST_TOKEN=$(echo "$TEST_AUTH" | jq -r '.token // .accessToken // .access_token // ""')

if [ -z "$TEST_TOKEN" ] || [ "$TEST_TOKEN" == "null" ]; then
  echo "❌ Test user authentication failed"
  echo "Response: $TEST_AUTH"
else
  echo "✅ Test user authentication successful"
  echo "Token received (truncated): ${TEST_TOKEN:0:25}..."
  
  # Test a protected endpoint
  echo -e "\nTesting protected endpoint with test user token..."
  USER_TEST=$(curl -s -H "Authorization: Bearer $TEST_TOKEN" "$BASE_URL/api/test/user")
  echo "Response: $USER_TEST"
fi

echo -e "\nDiagnostic complete!"
