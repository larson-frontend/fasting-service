#!/bin/bash
# Test health endpoint
echo "🏥 Testing Health Endpoint"
echo "========================="
curl -X GET http://localhost:8080/actuator/health
echo -e "\n"
