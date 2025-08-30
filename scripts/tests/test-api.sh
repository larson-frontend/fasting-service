#!/bin/bash

# 🚀 Fasting Service API Test Scripts
# Usage: ./scripts/tests/test-api.sh [test_name]
# Available tests: health, availability, create, duplicate, scenarios, all

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Fasting Service API Tests${NC}"
echo "================================="

# Function to test health endpoint
test_health() {
    echo -e "\n${YELLOW}📋 Testing Health Endpoint${NC}"
    echo "GET $BASE_URL/actuator/health"
    response=$(curl -s "$BASE_URL/actuator/health")
    echo "Response: $response"
    
    if [[ $response == *'"status":"UP"'* ]]; then
        echo -e "${GREEN}✅ Health check passed${NC}"
    else
        echo -e "${RED}❌ Health check failed${NC}"
    fi
}

# Function to test username availability
test_availability() {
    echo -e "\n${YELLOW}🔍 Testing Username Availability${NC}"
    
    echo "GET $BASE_URL/api/users/check-availability?username=new_user_123"
    response=$(curl -s "$BASE_URL/api/users/check-availability?username=new_user_123")
    echo "Response: $response"
    
    if [[ $response == *'"usernameAvailable":true'* ]]; then
        echo -e "${GREEN}✅ Available username check passed${NC}"
    else
        echo -e "${RED}❌ Available username check failed${NC}"
    fi
}

# Function to test user creation
test_create() {
    echo -e "\n${YELLOW}👤 Testing User Creation${NC}"
    timestamp=$(date +%s)
    username="test_user_$timestamp"
    email="test_$timestamp@example.com"
    
    echo "POST $BASE_URL/api/users/login-or-create"
    echo "Data: {\"username\":\"$username\",\"email\":\"$email\"}"
    
    response=$(curl -s -X POST "$BASE_URL/api/users/login-or-create" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"email\":\"$email\"}")
    
    echo "Response: $response"
    
    if [[ $response == *'"username":'* ]] && [[ $response == *'"id":'* ]]; then
        echo -e "${GREEN}✅ User creation passed${NC}"
    else
        echo -e "${RED}❌ User creation failed${NC}"
    fi
}

# Function to test duplicate username
test_duplicate() {
    echo -e "\n${YELLOW}🚫 Testing Duplicate Username Prevention${NC}"
    existing_username="frontend_test"
    curl -s -X POST "$BASE_URL/api/users/login-or-create" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$existing_username\",\"email\":\"$existing_username@test.com\"}" > /dev/null
    response=$(curl -s -X POST "$BASE_URL/api/users/login-or-create" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$existing_username\",\"email\":\"different@email.com\"}")
    echo "Response: $response"
    if [[ $response == *'"error":'* ]] && [[ $response == *'already taken'* ]]; then
        echo -e "${GREEN}✅ Duplicate prevention passed${NC}"
    else
        echo -e "${RED}❌ Duplicate prevention failed${NC}"
    fi
}

test_scenarios() {
    echo -e "\n${YELLOW}🎯 Testing Your Specific Scenarios${NC}"
    curl -s -X POST "$BASE_URL/api/users/login-or-create" \
        -H "Content-Type: application/json" \
        -d '{"username":"test123as","email":"test123as@example.com"}' > /dev/null
    response=$(curl -s "$BASE_URL/api/users/check-availability?username=test123as")
    echo "Availability check: $response"
    response=$(curl -s -X POST "$BASE_URL/api/users/login-or-create" \
        -H "Content-Type: application/json" \
        -d '{"username":"test123as","email":"different@example.com"}')
    echo "Registration attempt: $response"
}

test_all() {
    test_health
    test_availability
    test_create
    test_duplicate
    test_scenarios
    echo -e "\n${BLUE}🎉 All tests completed!${NC}"
}

case "${1:-all}" in
    health) test_health ;;
    availability) test_availability ;;
    create) test_create ;;
    duplicate) test_duplicate ;;
    scenarios) test_scenarios ;;
    all) test_all ;;
    *)
        echo "Usage: $0 [health|availability|create|duplicate|scenarios|all]"; exit 1
        ;;
esac
