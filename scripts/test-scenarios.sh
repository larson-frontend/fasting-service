#!/bin/bash
# Test your specific scenarios from requirements

echo "🎯 Testing Frontend Registration Scenarios"
echo "=========================================="

echo ""
echo "📝 Scenario 1: test123as username (should show 'already exist choose another one')"
echo "=================================================================================="

# Ensure user exists first
curl -s -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d '{"username":"test123as","email":"test123as@example.com"}' > /dev/null

# Check availability
echo "Checking availability:"
curl -X GET "http://localhost:8080/api/users/check-availability?username=test123as"
echo ""

# Try to register
echo "Attempting registration:"
curl -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d '{"username":"test123as","email":"different@example.com"}' \
  | jq '.' 2>/dev/null || cat

echo -e "\n"

echo "📝 Scenario 2: test12366 username (should show 'user created you can use the app')"
echo "================================================================================="

# Use unique timestamp to ensure new user
TIMESTAMP=$(date +%s)
NEW_USERNAME="test12366_$TIMESTAMP"

echo "Creating new user: $NEW_USERNAME"
curl -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$NEW_USERNAME\",\"email\":\"$NEW_USERNAME@example.com\"}" \
  | jq '.' 2>/dev/null || cat

echo -e "\n"

echo "📝 Scenario 3: Future visits (user stored locally, no dialog needed)"
echo "=================================================================="
echo "This would be handled by frontend localStorage - user data is returned above ☝️"
echo -e "\n"
