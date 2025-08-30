#!/bin/bash
# Test single field registration (username field can be username OR email)

echo "🎯 Testing Single Field Registration (Username or Email)"
echo "========================================================"

echo ""
echo "📝 Test 1: Username in single field"
echo "=================================="
TIMESTAMP=$(date +%s)
USERNAME="testuser_$TIMESTAMP"

echo "Request: {\"username\":\"$USERNAME\"}"
response=$(curl -s -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\"}")
echo "Response: $response"

if [[ $response == *'"username":"'$USERNAME'"'* ]]; then
    echo -e "✅ Username registration successful"
else
    echo -e "❌ Username registration failed"
fi

echo ""
echo "📝 Test 2: Email in single field"
echo "==============================="
EMAIL="testemail_$TIMESTAMP@example.com"

echo "Request: {\"username\":\"$EMAIL\"}"
response=$(curl -s -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$EMAIL\"}")
echo "Response: $response"

if [[ $response == *'"email":"'$EMAIL'"'* ]]; then
    echo -e "✅ Email registration successful"
else
    echo -e "❌ Email registration failed"
fi

echo ""
echo "📝 Test 3: Login with existing username"
echo "======================================"
echo "Request: {\"username\":\"$USERNAME\"}"
response=$(curl -s -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\"}")
echo "Response: $response"

if [[ $response == *'"username":"'$USERNAME'"'* ]]; then
    echo -e "✅ Username login successful"
else
    echo -e "❌ Username login failed"
fi

echo ""
echo "💡 Summary: Frontend can send either username or email in a single field."
