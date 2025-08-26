#!/bin/bash
# Test single field (username only) registration

echo "🎯 Testing Single Field Registration"
echo "===================================="

echo ""
echo "📝 Test 1: Username only (email auto-generated)"
echo "==============================================="
TIMESTAMP=$(date +%s)
USERNAME="user_$TIMESTAMP"

echo "Request: {\"username\":\"$USERNAME\"}"
curl -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\"}" \
  | jq '.' 2>/dev/null || cat

echo -e "\n"

echo "📝 Test 2: Same username again (should login existing user)"
echo "========================================================="
echo "Request: {\"username\":\"$USERNAME\"}"
curl -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\"}" \
  | jq '.' 2>/dev/null || cat

echo -e "\n"

echo "📝 Test 3: Username with custom email"
echo "====================================="
NEW_USERNAME="user_custom_$TIMESTAMP"
CUSTOM_EMAIL="$NEW_USERNAME@mycustom.com"

echo "Request: {\"username\":\"$NEW_USERNAME\",\"email\":\"$CUSTOM_EMAIL\"}"
curl -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$NEW_USERNAME\",\"email\":\"$CUSTOM_EMAIL\"}" \
  | jq '.' 2>/dev/null || cat

echo -e "\n"

echo "✅ Single field registration tests completed!"
