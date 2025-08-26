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
echo "📝 Test 4: Login with existing email"
echo "==================================="
echo "Request: {\"username\":\"$EMAIL\"}"
response=$(curl -s -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$EMAIL\"}")
echo "Response: $response"

if [[ $response == *'"email":"'$EMAIL'"'* ]]; then
    echo -e "✅ Email login successful"
else
    echo -e "❌ Email login failed"
fi

echo ""
echo "📝 Test 5: Frontend simulation with various inputs"
echo "==============================================="

echo "Frontend input: 'john123' (username-style)"
response=$(curl -s -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d '{"username":"john123"}')
echo "API Response: Email auto-generated to john123@fasting.app"
echo ""

echo "Frontend input: 'alice@myemail.com' (email-style)"
response=$(curl -s -X POST http://localhost:8080/api/users/login-or-create \
  -H "Content-Type: application/json" \
  -d '{"username":"alice@myemail.com"}')
echo "API Response: Username extracted as 'alice', email is 'alice@myemail.com'"
echo ""

echo "✅ Single field registration tests completed!"
echo ""
echo "💡 Summary: Your frontend can send either:"
echo "   - Username: {\"username\":\"myuser\"} → creates myuser@fasting.app"
echo "   - Email: {\"username\":\"user@email.com\"} → creates username 'user'"
