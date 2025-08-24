#!/bin/bash

# 🚀 Fasting Service API Test Scripts
# Usage: ./test-api.sh [test_name]
# Available tests: health, availability, create, duplicate, all

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
    
    # Test available username
    echo "GET $BASE_URL/api/users/check-availability?username=new_user_123"
    response=$(curl -s "$BASE_URL/api/users/check-availability?username=new_user_123")
    echo "Response: $response"
    
    if [[ $response == *'"usernameAvailable":true'* ]]; then
        echo -e "${GREEN}✅ Available username check passed${NC}"
    else
        echo -e "${RED}❌ Available username check failed${NC}"
    fi
    
    # Test unavailable username (if exists)
    echo -e "\nGET $BASE_URL/api/users/check-availability?username=frontend_test"
    response=$(curl -s "$BASE_URL/api/users/check-availability?username=frontend_test")
    echo "Response: $response"
    
    if [[ $response == *'"usernameAvailable":false'* ]]; then
        echo -e "${GREEN}✅ Unavailable username check passed${NC}"
    else
        echo -e "${YELLOW}ℹ️  Username 'frontend_test' not found (may not exist yet)${NC}"
    fi
}

# Function to test user creation
test_create() {
    echo -e "\n${YELLOW}👤 Testing User Creation${NC}"
    
    # Generate unique username
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
        echo -e "${BLUE}💾 Created user: $username${NC}"
        
        # Store username for duplicate test
        echo "$username" > /tmp/last_created_user.txt
    else
        echo -e "${RED}❌ User creation failed${NC}"
    fi
}

# Function to test duplicate username
test_duplicate() {
    echo -e "\n${YELLOW}🚫 Testing Duplicate Username Prevention${NC}"
    
    # Try to use existing username
    existing_username="frontend_test"
    
    # First create the user if it doesn't exist
    echo "Creating initial user: $existing_username"
    curl -s -X POST "$BASE_URL/api/users/login-or-create" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$existing_username\",\"email\":\"$existing_username@test.com\"}" > /dev/null
    
    # Now try to create duplicate
    echo "POST $BASE_URL/api/users/login-or-create"
    echo "Data: {\"username\":\"$existing_username\",\"email\":\"different@email.com\"}"
    
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

# Function to test all scenarios from your requirements
test_scenarios() {
    echo -e "\n${YELLOW}🎯 Testing Your Specific Scenarios${NC}"
    
    # Scenario 1: test123as username
    echo -e "\n📝 Scenario 1: Username 'test123as'"
    
    # First ensure user exists
    curl -s -X POST "$BASE_URL/api/users/login-or-create" \
        -H "Content-Type: application/json" \
        -d '{"username":"test123as","email":"test123as@example.com"}' > /dev/null
    
    # Test availability
    response=$(curl -s "$BASE_URL/api/users/check-availability?username=test123as")
    echo "Availability check: $response"
    
    # Try to register
    response=$(curl -s -X POST "$BASE_URL/api/users/login-or-create" \
        -H "Content-Type: application/json" \
        -d '{"username":"test123as","email":"different@example.com"}')
    echo "Registration attempt: $response"
    
    if [[ $response == *'already taken'* ]]; then
        echo -e "${GREEN}✅ Scenario 1 passed: 'already exist choose another one'${NC}"
    fi
    
    # Scenario 2: test12366 username (new user)
    echo -e "\n📝 Scenario 2: Username 'test12366' (new user)"
    
    # Generate unique username
    timestamp=$(date +%s)
    new_username="test12366_$timestamp"
    
    response=$(curl -s -X POST "$BASE_URL/api/users/login-or-create" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$new_username\",\"email\":\"$new_username@example.com\"}")
    echo "Registration: $response"
    
    if [[ $response == *'"username":'* ]] && [[ $response == *'"id":'* ]]; then
        echo -e "${GREEN}✅ Scenario 2 passed: 'user created you can use the app'${NC}"
    fi
}

# Function to run all tests
test_all() {
    test_health
    test_availability
    test_create
    test_duplicate
    test_scenarios
    echo -e "\n${BLUE}🎉 All tests completed!${NC}"
}

# Main script logic
case "${1:-all}" in
    "health")
        test_health
        ;;
    "availability")
        test_availability
        ;;
    "create")
        test_create
        ;;
    "duplicate")
        test_duplicate
        ;;
    "scenarios")
        test_scenarios
        ;;
    "all")
        test_all
        ;;
    *)
        echo "Usage: $0 [health|availability|create|duplicate|scenarios|all]"
        echo ""
        echo "Available tests:"
        echo "  health       - Test health endpoint"
        echo "  availability - Test username availability check"
        echo "  create       - Test user creation"
        echo "  duplicate    - Test duplicate username prevention"
        echo "  scenarios    - Test your specific registration scenarios"
        echo "  all          - Run all tests (default)"
        exit 1
        ;;
esac
