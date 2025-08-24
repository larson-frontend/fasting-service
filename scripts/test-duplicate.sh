#!/bin/bash
# Test duplicate username scenario
USERNAME=${1:-"duplicate_test"}

echo "🚫 Testing Duplicate Username Prevention"
echo "========================================"
echo "Username: $USERNAME"
echo ""

# First create the user
echo "1. Creating initial user..."
curl -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$USERNAME@first.com\"}" \
  | jq '.' 2>/dev/null || cat

echo -e "\n"

# Now try to create duplicate
echo "2. Attempting to create duplicate user..."
curl -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$USERNAME@second.com\"}" \
  | jq '.' 2>/dev/null || cat

echo -e "\n"
