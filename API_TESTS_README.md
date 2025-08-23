# 🧪 API Test Scripts

This directory contains test scripts for the Fasting Service API endpoints.

## 🚀 Quick Start

```bash
# Make scripts executable (if not already)
chmod +x *.sh scripts/*.sh

# Run all tests
./test-api.sh

# Get help with available commands
./scripts/help.sh
```

## 📋 Available Scripts

### Main Test Script
- **`./test-api.sh`** - Comprehensive test suite with all scenarios

Usage:
```bash
./test-api.sh [test_type]
```

Options:
- `health` - Test health endpoint
- `availability` - Test username availability
- `create` - Test user creation
- `duplicate` - Test duplicate prevention
- `scenarios` - Test your specific registration scenarios
- `all` - Run all tests (default)

### Individual Test Scripts

#### **`./scripts/test-health.sh`**
Tests the health endpoint to verify API is running.

```bash
./scripts/test-health.sh
```

#### **`./scripts/test-availability.sh [username]`**
Tests username availability checking.

```bash
./scripts/test-availability.sh                    # Random username
./scripts/test-availability.sh myusername         # Specific username
```

#### **`./scripts/test-create-user.sh [username] [email]`**
Tests user creation endpoint.

```bash
./scripts/test-create-user.sh                     # Random user
./scripts/test-create-user.sh john john@test.com  # Specific user
```

#### **`./scripts/test-duplicate.sh [username]`**
Tests duplicate username prevention.

```bash
./scripts/test-duplicate.sh                       # Default test user
./scripts/test-duplicate.sh myuser                # Specific username
```

#### **`./scripts/test-scenarios.sh`**
Tests your specific registration flow scenarios:
1. `test123as` → "already exist choose another one"
2. `test12366` → "user created you can use the app"  
3. Future visits → no dialog needed

```bash
./scripts/test-scenarios.sh
```

#### **`./scripts/help.sh`**
Shows API documentation and available commands.

```bash
./scripts/help.sh
```

## 🎯 Expected Responses

### ✅ Successful User Creation
```json
{
  "user": {
    "id": "1",
    "username": "john_doe",
    "email": "john@example.com",
    "createdAt": "2025-08-23T21:00:00Z",
    "lastLoginAt": "2025-08-23T21:00:00Z",
    "preferences": {
      "language": "en",
      "theme": "system",
      "notifications": {
        "fastingReminders": true,
        "mealReminders": true,
        "progressUpdates": true
      }
    }
  },
  "token": null
}
```

### ❌ Duplicate Username Error
```json
{
  "error": "Username 'john_doe' is already taken",
  "timestamp": "2025-08-23T21:00:00Z"
}
```

### 🔍 Username Availability
```json
{
  "usernameAvailable": true
}
```

```json
{
  "usernameAvailable": false
}
```

### 🏥 Health Check
```json
{
  "status": "UP"
}
```

## 🔧 API Endpoints Tested

- **GET** `/actuator/health` - Health check
- **GET** `/api/users/check-availability?username=USER` - Check availability
- **POST** `/api/users/login-or-create` - Register/login user
- **GET** `/api/users/find/{identifier}` - Find user by username/email

## 💡 Frontend Integration

These scripts test the exact API endpoints your frontend will use:

1. **Before registration**: Check username availability
2. **Registration attempt**: Create user or show error
3. **Local storage**: Save user data to avoid future dialogs

Use these scripts during frontend development to verify API connectivity and behavior.

## 🐛 Troubleshooting

If tests fail:

1. **Check if Docker is running**:
   ```bash
   docker ps
   ```

2. **Restart services**:
   ```bash
   docker-compose down && docker-compose up -d
   ```

3. **Check logs**:
   ```bash
   docker logs fasting_app
   ```

4. **Verify API is accessible**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
