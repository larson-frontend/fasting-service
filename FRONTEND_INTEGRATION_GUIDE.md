# Frontend Integration Guide - Fasting Service API

## 🚀 API Status: READY FOR FRONTEND INTEGRATION

Your Spring Boot backend is successfully running at **http://localhost:8080** with PostgreSQL database.

---

## 📋 API Overview

### Base URL
```
http://localhost:8080
```

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/api-docs

---

## 🔧 Key API Customizations & Frontend-Ready Features

### 1. **CORS Configuration** ✅
- **Enabled** for all origins during development
- **Supports** all HTTP methods (GET, POST, PUT, PATCH, DELETE)
- **Headers** allowed for authentication and content-type

```javascript
// Frontend can make requests without CORS issues
const response = await fetch('http://localhost:8080/api/users', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
});
```

### 2. **Unique Identifier Validation** ✅
- **Username AND Email** must both be unique across all users
- **Strict validation** prevents duplicate usernames or emails
- **Clear error messages** for frontend form validation

```javascript
// Check if username/email is available before form submission
const checkAvailability = async (username, email) => {
  const response = await fetch(
    `http://localhost:8080/api/users/check-availability?username=${username}&email=${email}`
  );
  const data = await response.json();
  return data; // { usernameAvailable: true, emailAvailable: false }
};
```

### 3. **Error Handling** ✅
- **Consistent** JSON error responses
- **HTTP status codes** properly implemented
- **Validation errors** with detailed field information

```json
// Example error response for duplicate username
{
  "error": "Username 'john_doe' is already taken",
  "timestamp": "2025-08-23T17:12:37.737Z"
}

// Example error response for duplicate email
{
  "error": "Email 'john@example.com' is already registered", 
  "timestamp": "2025-08-23T17:12:37.737Z"
}
```

### 3. **User Management API** ✅
Complete CRUD operations with frontend-friendly endpoints:

#### **GET /api/users** - List All Users
```javascript
const users = await fetch('http://localhost:8080/api/users')
  .then(res => res.json());
```

#### **POST /api/users** - Create New User
```javascript
const newUser = await fetch('http://localhost:8080/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: "John Doe",
    email: "john@example.com",
    age: 30
  })
}).then(res => res.json());
```

#### **PATCH /api/users/{id}** - Update User (Partial)
```javascript
const updatedUser = await fetch('http://localhost:8080/api/users/1', {
  method: 'PATCH',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: "John Smith"  // Only update name
  })
}).then(res => res.json());
```

#### **DELETE /api/users/{id}** - Delete User
```javascript
await fetch('http://localhost:8080/api/users/1', {
  method: 'DELETE'
});
```

### 4. **Fasting Session Management** ✅

#### **GET /api/fast** - Get All Fasting Sessions
```javascript
const fasts = await fetch('http://localhost:8080/api/fast')
  .then(res => res.json());
```

#### **POST /api/fast/start** - Start New Fast
```javascript
const newFast = await fetch('http://localhost:8080/api/fast/start', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    goalHours: 16
  })
}).then(res => res.json());
```

#### **POST /api/fast/stop** - End Active Fast
```javascript
const completedFast = await fetch('http://localhost:8080/api/fast/stop', {
  method: 'POST'
}).then(res => res.json());
```

#### **GET /api/fast/status** - Get Current Fast Status
```javascript
const status = await fetch('http://localhost:8080/api/fast/status')
  .then(res => res.json());
```

#### **GET /api/fast/history** - Get All Fasting History
```javascript
const history = await fetch('http://localhost:8080/api/fast/history')
  .then(res => res.json());
```

### 5. **User-Specific Fasting Management** ✅ **(Cross-Device Login Support)**

These endpoints support cross-device login by using username or email to fetch user-specific data:

#### **GET /api/fast/user/{identifier}/status** - Get User's Current Fast Status
```javascript
// Login from any device with username or email
const getUserStatus = async (userIdentifier) => {
  const status = await fetch(`http://localhost:8080/api/fast/user/${encodeURIComponent(userIdentifier)}/status`)
    .then(res => res.json());
  return status;
};

// Examples:
await getUserStatus('john_doe');           // by username
await getUserStatus('john@example.com');   // by email
```

#### **GET /api/fast/user/{identifier}/history** - Get User's Fasting History
```javascript
const getUserHistory = async (userIdentifier) => {
  const history = await fetch(`http://localhost:8080/api/fast/user/${encodeURIComponent(userIdentifier)}/history`)
    .then(res => res.json());
  return history;
};
```

#### **POST /api/fast/user/{identifier}/start** - Start Fast for Specific User
```javascript
const startUserFast = async (userIdentifier, goalHours = 16) => {
  const newFast = await fetch(`http://localhost:8080/api/fast/user/${encodeURIComponent(userIdentifier)}/start`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ goalHours })
  }).then(res => res.json());
  return newFast;
};
```

#### **POST /api/fast/user/{identifier}/stop** - Stop User's Active Fast
```javascript
const stopUserFast = async (userIdentifier) => {
  const completedFast = await fetch(`http://localhost:8080/api/fast/user/${encodeURIComponent(userIdentifier)}/stop`, {
    method: 'POST'
  }).then(res => res.json());
  return completedFast;
};
```

---

## 💾 Data Models for Frontend

### User Model
```typescript
interface User {
  id: number;
  name: string;
  email: string;
  age: number;
  createdAt: string;
  updatedAt: string;
}
```

### Fast Session Model
```typescript
interface FastSession {
  id: number;
  user?: {
    id: number;
    username: string;
    email: string;
  };
  startAt: string;
  endAt?: string;
  goalHours: number;
  isActive: boolean;
  durationHours?: number;
}
```

### Fast Status Response
```typescript
interface FastStatusResponse {
  hasActiveFast: boolean;
  currentFast?: FastSession;
  message: string;
}
```

---

## 🎯 Frontend Implementation Examples

### Cross-Device Login & Data Sync
```typescript
import { useState, useEffect } from 'react';

interface UserSession {
  user: User;
  currentFast?: FastSession;
  fastingHistory: FastSession[];
}

const useCrossDeviceLogin = () => {
  const [userSession, setUserSession] = useState<UserSession | null>(null);
  const [loading, setLoading] = useState(false);

  // Login with username or email from any device
  const loginUser = async (identifier: string): Promise<boolean> => {
    setLoading(true);
    try {
      // Step 1: Get user data
      const userResponse = await fetch(`http://localhost:8080/api/users/find/${encodeURIComponent(identifier)}`);
      if (!userResponse.ok) {
        console.error('User not found');
        return false;
      }
      const user = await userResponse.json();

      // Step 2: Get current fasting status
      const statusResponse = await fetch(`http://localhost:8080/api/fast/user/${encodeURIComponent(identifier)}/status`);
      const fastStatus = await statusResponse.json();

      // Step 3: Get fasting history
      const historyResponse = await fetch(`http://localhost:8080/api/fast/user/${encodeURIComponent(identifier)}/history`);
      const fastingHistory = await historyResponse.json();

      // Step 4: Create complete user session
      setUserSession({
        user,
        currentFast: fastStatus.isActive ? fastStatus : undefined,
        fastingHistory
      });

      // Step 5: Save to localStorage for offline access
      localStorage.setItem('fastingUserSession', JSON.stringify({
        user,
        currentFast: fastStatus.isActive ? fastStatus : undefined,
        fastingHistory,
        lastSync: new Date().toISOString()
      }));

      return true;
    } catch (error) {
      console.error('Login error:', error);
      return false;
    } finally {
      setLoading(false);
    }
  };

  // Start fasting session for current user
  const startFasting = async (goalHours: number = 16): Promise<boolean> => {
    if (!userSession?.user) return false;

    try {
      const response = await fetch(`http://localhost:8080/api/fast/user/${encodeURIComponent(userSession.user.username)}/start`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ goalHours })
      });

      if (response.ok) {
        const newFast = await response.json();
        setUserSession(prev => prev ? { ...prev, currentFast: newFast } : null);
        return true;
      }
    } catch (error) {
      console.error('Error starting fast:', error);
    }
    return false;
  };

  // Stop current fasting session
  const stopFasting = async (): Promise<boolean> => {
    if (!userSession?.user) return false;

    try {
      const response = await fetch(`http://localhost:8080/api/fast/user/${encodeURIComponent(userSession.user.username)}/stop`, {
        method: 'POST'
      });

      if (response.ok) {
        const completedFast = await response.json();
        setUserSession(prev => prev ? { 
          ...prev, 
          currentFast: undefined,
          fastingHistory: [completedFast, ...prev.fastingHistory]
        } : null);
        return true;
      }
    } catch (error) {
      console.error('Error stopping fast:', error);
    }
    return false;
  };

  // Load user session from localStorage on app start
  useEffect(() => {
    const savedSession = localStorage.getItem('fastingUserSession');
    if (savedSession) {
      try {
        const session = JSON.parse(savedSession);
        setUserSession(session);
        
        // Optionally refresh data from server
        if (session.user) {
          loginUser(session.user.username);
        }
      } catch (error) {
        console.error('Error loading saved session:', error);
        localStorage.removeItem('fastingUserSession');
      }
    }
  }, []);

  return {
    userSession,
    loading,
    loginUser,
    startFasting,
    stopFasting,
    logout: () => {
      setUserSession(null);
      localStorage.removeItem('fastingUserSession');
    }
  };
};

// Usage in React component
const FastingApp = () => {
  const { userSession, loading, loginUser, startFasting, stopFasting, logout } = useCrossDeviceLogin();
  const [loginInput, setLoginInput] = useState('');

  if (!userSession) {
    return (
      <div>
        <h2>Login to Fasting App</h2>
        <input
          type="text"
          placeholder="Enter username or email"
          value={loginInput}
          onChange={(e) => setLoginInput(e.target.value)}
        />
        <button 
          onClick={() => loginUser(loginInput)}
          disabled={loading || !loginInput.trim()}
        >
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </div>
    );
  }

  return (
    <div>
      <h1>Welcome back, {userSession.user.username}!</h1>
      <button onClick={logout}>Logout</button>
      
      {userSession.currentFast ? (
        <div>
          <h2>Current Fast: {userSession.currentFast.goalHours}h goal</h2>
          <p>Started: {new Date(userSession.currentFast.startAt).toLocaleString()}</p>
          <button onClick={stopFasting}>Stop Fasting</button>
        </div>
      ) : (
        <div>
          <h2>Start New Fast</h2>
          <button onClick={() => startFasting(16)}>Start 16h Fast</button>
          <button onClick={() => startFasting(24)}>Start 24h Fast</button>
        </div>
      )}
      
      <div>
        <h3>Fasting History ({userSession.fastingHistory.length} sessions)</h3>
        {userSession.fastingHistory.slice(0, 5).map(fast => (
          <div key={fast.id}>
            {fast.goalHours}h goal - {new Date(fast.startAt).toLocaleDateString()}
            {fast.endAt && ` (Completed: ${Math.round((new Date(fast.endAt).getTime() - new Date(fast.startAt).getTime()) / (1000 * 60 * 60))}h)`}
          </div>
        ))}
      </div>
    </div>
  );
};
```

### React Hook for User Management
```typescript
import { useState, useEffect } from 'react';

const useUsers = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchUsers = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/users');
      const data = await response.json();
      setUsers(data);
    } catch (error) {
      console.error('Error fetching users:', error);
    } finally {
      setLoading(false);
    }
  };

  const createUser = async (userData: Omit<User, 'id' | 'createdAt' | 'updatedAt'>) => {
    try {
      const response = await fetch('http://localhost:8080/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData)
      });
      const newUser = await response.json();
      setUsers(prev => [...prev, newUser]);
      return newUser;
    } catch (error) {
      console.error('Error creating user:', error);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  return { users, loading, createUser, refetch: fetchUsers };
};
```

### Vue.js Composition API Example
```typescript
import { ref, onMounted } from 'vue';

export const useFastingService = () => {
  const currentFast = ref<FastSession | null>(null);
  const isLoading = ref(false);

  const startFast = async (userId: number, targetHours: number) => {
    isLoading.value = true;
    try {
      const response = await fetch('http://localhost:8080/api/fasts/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, targetHours })
      });
      
      if (response.ok) {
        currentFast.value = await response.json();
      }
    } catch (error) {
      console.error('Error starting fast:', error);
    } finally {
      isLoading.value = false;
    }
  };

  const endFast = async (fastId: number) => {
    try {
      const response = await fetch(`http://localhost:8080/api/fasts/end/${fastId}`, {
        method: 'POST'
      });
      
      if (response.ok) {
        const completedFast = await response.json();
        currentFast.value = null;
        return completedFast;
      }
    } catch (error) {
      console.error('Error ending fast:', error);
    }
  };

  return {
    currentFast,
    isLoading,
    startFast,
    endFast
  };
};
```

---

## ⚡ Performance & Optimization Features

### 1. **Database Connection Pooling** ✅
- HikariCP connection pool configured
- Optimized for concurrent requests

### 2. **JSON Response Optimization** ✅
- Efficient serialization
- Proper HTTP caching headers

### 3. **Validation** ✅
- Input validation on all endpoints
- Detailed error messages for form validation

---

## 🔒 Security Considerations for Frontend

### Input Validation
All endpoints validate:
- ✅ **Email format** validation
- ✅ **Required fields** checking
- ✅ **Data types** enforcement
- ✅ **Boundary conditions** (age, hours, etc.)

### Error Handling
- ✅ **No sensitive data** in error responses
- ✅ **Consistent error format** for easy frontend handling
- ✅ **HTTP status codes** properly mapped

---

## 🎨 Frontend Testing Endpoints

### Quick Test Commands
```bash
# Test user creation
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","age":25}'

# Test fasting session start
curl -X POST http://localhost:8080/api/fasts/start \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"targetHours":16}'

# Get current status
curl http://localhost:8080/api/fasts/status
```

---

## 🚀 Production Deployment Notes

When deploying to production:

1. **Update CORS configuration** to specific frontend domains
2. **Enable HTTPS** for secure communication
3. **Configure proper database** connection strings
4. **Set up environment variables** for sensitive configuration
5. **Enable security headers** and authentication if needed

---

## 📞 API Support & Troubleshooting

### Common Frontend Issues & Solutions

**CORS Errors:**
- ✅ Already configured for development
- For production: Update `@CrossOrigin` annotations

**Date/Time Handling:**
- All timestamps in ISO 8601 format
- Use `new Date(timestamp)` in JavaScript

**Error Handling:**
- Always check `response.ok` before parsing JSON
- Handle network errors separately from API errors

### Health Check Endpoint
```
GET http://localhost:8080/actuator/health
```

---

## 🎯 Next Steps for Frontend Development

1. **Start with user management** - Create user registration/login forms
2. **Implement fasting timer** - Real-time countdown display
3. **Add fasting history** - List and visualize past fasting sessions
4. **Create dashboard** - Overview of user's fasting progress
5. **Add notifications** - Alerts for fasting milestones

Your backend is fully ready for frontend integration! All endpoints are tested, validated, and properly documented. The API follows REST conventions and provides consistent, frontend-friendly responses.
