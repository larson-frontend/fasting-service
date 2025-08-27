#!/bin/bash
# Test user creation
USERNAME=${1:-"test_user_$(date +%s)"}
EMAIL=${2:-"$USERNAME@example.com"}

echo "👤 Testing User Creation"
echo "========================"
echo "Creating user: $USERNAME"
echo "Email: $EMAIL"
echo ""

curl -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\"}" \
  | jq '.' 2>/dev/null || cat

echo -e "\n"
