#!/bin/bash
# Test username availability
USERNAME=${1:-"test_user_$(date +%s)"}

echo "🔍 Testing Username Availability"
echo "================================"
echo "Checking username: $USERNAME"
echo ""

curl -X GET "http://localhost:8080/api/users/check-availability?username=$USERNAME"
echo -e "\n"

# Also test with email
echo "Checking with email parameter:"
curl -X GET "http://localhost:8080/api/users/check-availability?username=$USERNAME&email=$USERNAME@example.com"
echo -e "\n"
