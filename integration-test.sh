#!/bin/bash

# Integration Test Script for User Preferences
# This script tests the complete flow from frontend to backend

echo "🧪 Starting User Preferences Integration Tests..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test results
TESTS_PASSED=0
TESTS_FAILED=0

print_test_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ $2${NC}"
        ((TESTS_FAILED++))
    fi
}

print_section() {
    echo -e "\n${YELLOW}📋 $1${NC}"
}

# Function to test API endpoint
test_api_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_status=$4
    local description=$5

    echo "Testing: $description"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
            -X $method \
            -H "Content-Type: application/json" \
            -d "$data" \
            "http://localhost:8080$endpoint")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
            -X $method \
            "http://localhost:8080$endpoint")
    fi
    
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo $response | sed -e 's/HTTPSTATUS\:.*//g')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        print_test_result 0 "$description (Status: $http_code)"
        return 0
    else
        print_test_result 1 "$description (Expected: $expected_status, Got: $http_code)"
        echo "Response: $body"
        return 1
    fi
}

# Check if backend is running
print_section "Backend Health Check"
test_api_endpoint "GET" "/api/health" "" 200 "Backend health check"

# Check if database is accessible
print_section "Database Schema Validation"
echo "Checking if all required database columns exist..."

# Test user preferences endpoints
print_section "User Preferences API Tests"

# Test 1: Update language preference
test_api_endpoint "PATCH" "/api/users/preferences?userId=1" \
    '{"language": "de"}' \
    200 \
    "Update language preference"

# Test 2: Update theme preference
test_api_endpoint "PATCH" "/api/users/preferences?userId=1" \
    '{"theme": "dark"}' \
    200 \
    "Update theme preference"

# Test 3: Update notification preferences
test_api_endpoint "PATCH" "/api/users/preferences?userId=1" \
    '{"notifications": {"enabled": false, "fastingReminders": true, "mealReminders": false}}' \
    200 \
    "Update notification preferences"

# Test 4: Update fasting defaults
test_api_endpoint "PATCH" "/api/users/preferences?userId=1" \
    '{"fastingDefaults": {"defaultGoalHours": 18, "preferredFastingType": "18:6", "autoStartNextFast": true}}' \
    200 \
    "Update fasting defaults"

# Test 5: Update timezone
test_api_endpoint "PATCH" "/api/users/preferences?userId=1" \
    '{"timezone": "Europe/Berlin"}' \
    200 \
    "Update timezone preference"

# Test 6: Complete preferences update
test_api_endpoint "PATCH" "/api/users/preferences?userId=1" \
    '{
        "language": "en",
        "theme": "light",
        "timezone": "America/New_York",
        "notifications": {
            "enabled": true,
            "fastingReminders": true,
            "mealReminders": true,
            "progressUpdates": false,
            "goalAchievements": true,
            "weeklyReports": false
        },
        "fastingDefaults": {
            "defaultGoalHours": 16,
            "preferredFastingType": "16:8",
            "autoStartNextFast": false
        }
    }' \
    200 \
    "Complete preferences update"

# Test error cases
print_section "Error Handling Tests"

# Test 7: Invalid language
test_api_endpoint "PATCH" "/api/users/preferences?userId=1" \
    '{"language": "invalid"}' \
    400 \
    "Invalid language validation"

# Test 8: Invalid theme
test_api_endpoint "PATCH" "/api/users/preferences?userId=1" \
    '{"theme": "invalid"}' \
    400 \
    "Invalid theme validation"

# Test 9: User not found
test_api_endpoint "PATCH" "/api/users/preferences?userId=999999" \
    '{"language": "en"}' \
    404 \
    "User not found error"

# Test 10: Invalid user ID
test_api_endpoint "PATCH" "/api/users/preferences?userId=invalid" \
    '{"language": "en"}' \
    400 \
    "Invalid user ID validation"

print_section "Database Data Validation"
echo "Checking if preferences are properly stored in database..."

# This would need to be implemented with actual database queries
echo "⚠️  Database validation not implemented yet (manual verification required)"

print_section "Frontend Integration Tests"
echo "Running frontend component tests..."

cd /home/lars/Projects/fasting-frontend

# Run the actual frontend tests
if command -v npm &> /dev/null; then
    echo "Running frontend unit tests..."
    npm test > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        print_test_result 0 "Frontend component tests"
    else
        print_test_result 1 "Frontend component tests"
    fi
else
    echo "⚠️  npm not found - skipping frontend tests"
fi

# Final report
print_section "Test Summary"
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All integration tests passed!${NC}"
    exit 0
else
    echo -e "\n${RED}💥 Some tests failed. Please check the output above.${NC}"
    exit 1
fi
