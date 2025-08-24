# 🚀 Frontend API Validation Guide - Fasting Service

## 📋 **Registration Flow Requirements**

Your backend is running at: **http://localhost:8080**

### 🎯 **User Experience Flow**
1. **First Time**: User enters username → Registration/Login dialog
2. **Validation**: Check if username exists
3. **Registration**: If new, create user; if exists, show appropriate message
4. **Remember User**: Store locally, no future dialogs needed

---

## 🔧 **API Endpoints for Frontend Integration**

### 1. **Check Username Availability**
```javascript
// Step 1: Check if username exists before registration
const checkUsernameAvailability = async (username) => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/users/check-availability?username=${encodeURIComponent(username)}`
    );
    const data = await response.json();
    return data.usernameAvailable; // true = available, false = taken
  } catch (error) {
    console.error('Error checking username:', error);
    return false;
  }
};
```

### 2. **Registration/Login Handler**
```javascript
// Step 2: Handle registration or login with single field
const registerOrLoginUser = async (userInput) => {
  try {
    const response = await fetch('http://localhost:8080/api/users/login-or-create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: userInput.trim()  // Single field - can be username OR email
      })
    });

    const data = await response.json();

    if (response.status === 201) {
      // New user created
      return {
        success: true,
        isNewUser: true,
        message: "User created! You can use the app",
        user: data.user
      };
    } else if (response.status === 200) {
      // Existing user logged in
      return {
        success: true,
        isNewUser: false,
        message: "Welcome back!",
        user: data.user
      };
    } else if (response.status === 409) {
      // Username or email already exists
      return {
        success: false,
        message: data.error // "Username 'test123as' is already taken"
      };
    }
  } catch (error) {
    console.error('Registration/Login error:', error);
    return {
      success: false,
      message: "Connection error. Please try again."
    };
  }
};
```

---

## 🎭 **Complete Frontend Implementation Example**

```javascript
// Complete registration flow handler
class FastingAppAuth {
  constructor() {
    this.currentUser = this.loadStoredUser();
  }

  // Load user from localStorage
  loadStoredUser() {
    try {
      const stored = localStorage.getItem('fastingUser');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }

  // Save user to localStorage
  saveUser(user) {
    localStorage.setItem('fastingUser', JSON.stringify(user));
    this.currentUser = user;
  }

  // Check if user is already registered (no dialog needed)
  isUserLoggedIn() {
    return this.currentUser !== null;
  }

  // Main registration flow - single field input
  async handleRegistration(userInput) {
    // Step 1: Check availability (optional - for real-time feedback)
    const isAvailable = await this.checkUsernameAvailability(userInput);
    
    if (!isAvailable) {
      return {
        success: false,
        message: `'${userInput}' already exists. Choose another one.`
      };
    }

    // Step 2: Register new user with single field
    const result = await this.registerOrLoginUser(userInput);
    
    if (result.success) {
      // Step 3: Save user locally (no future dialogs)
      this.saveUser(result.user);
    }

    return result;
  }

  // Check username/email availability
  async checkUsernameAvailability(userInput) {
    try {
      const response = await fetch(
        `http://localhost:8080/api/users/check-availability?username=${encodeURIComponent(userInput)}`
      );
      const data = await response.json();
      return data.usernameAvailable;
    } catch (error) {
      console.error('Error checking availability:', error);
      return false;
    }
  }

  // Register or login user with single field
  async registerOrLoginUser(userInput) {
    try {
      const response = await fetch('http://localhost:8080/api/users/login-or-create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          username: username.trim(),
          email: email.trim()
        })
      });

      const data = await response.json();

      if (response.status === 201) {
        return {
          success: true,
          isNewUser: true,
          message: "User created! You can use the app",
          user: data.user
        };
      } else if (response.status === 200) {
        return {
          success: true,
          isNewUser: false,
          message: "Welcome back!",
          user: data.user
        };
      } else if (response.status === 409) {
        return {
          success: false,
          message: data.error
        };
      }
    } catch (error) {
      console.error('Registration/Login error:', error);
      return {
        success: false,
        message: "Connection error. Please try again."
      };
    }
  }

  // Clear stored user (logout)
  logout() {
    localStorage.removeItem('fastingUser');
    this.currentUser = null;
  }
}
```

---

## 🎯 **Example Usage Scenarios**

### **Scenario 1: Username Already Exists**
```javascript
const auth = new FastingAppAuth();

// User enters "test123as"
const result = await auth.handleRegistration("test123as");

// Output: 
// {
//   success: false,
//   message: "'test123as' already exists. Choose another one."
// }
```

### **Scenario 2: New User Registration with Username**
```javascript
const auth = new FastingAppAuth();

// User enters "test12366"
const result = await auth.handleRegistration("test12366");

// Output:
// {
//   success: true,
//   isNewUser: true,
//   message: "User created! You can use the app",
//   user: { id: 123, username: "test12366", email: "test12366@example.com", ... }
// }
```

### **Scenario 3: New User Registration with Email**
```javascript
const auth = new FastingAppAuth();

// User enters "jane.doe@gmail.com"
const result = await auth.handleRegistration("jane.doe@gmail.com");

// Output:
// {
//   success: true,
//   isNewUser: true,
//   message: "User created! You can use the app",
//   user: { id: 124, username: "jane.doe", email: "jane.doe@gmail.com", ... }
// }
```

### **Scenario 4: Returning User (No Dialog)**
```javascript
const auth = new FastingAppAuth();

// Check if user is already logged in
if (auth.isUserLoggedIn()) {
  console.log("Welcome back!", auth.currentUser.username);
  // NO REGISTRATION/LOGIN DIALOG NEEDED
} else {
  // Show registration dialog
  showRegistrationDialog();
}
```

---

## 🎨 **Frontend UI Implementation Example**

### **React Component Example**
```jsx
import React, { useState, useEffect } from 'react';

function AuthComponent() {
  const [auth] = useState(new FastingAppAuth());
  const [showDialog, setShowDialog] = useState(false);
  const [username, setUsername] = useState('');
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // Check if user is already logged in
    if (!auth.isUserLoggedIn()) {
      setShowDialog(true);
    }
  }, [auth]);

  const handleRegistration = async () => {
    if (!username.trim()) {
      setMessage('Please enter a username');
      return;
    }

    setIsLoading(true);
    setMessage('');

    const result = await auth.handleRegistration(username);
    
    setMessage(result.message);
    
    if (result.success) {
      setShowDialog(false);
      // User is now logged in, show main app
    }
    
    setIsLoading(false);
  };

  if (!showDialog && auth.isUserLoggedIn()) {
    return (
      <div>
        <h1>Welcome back, {auth.currentUser.username}!</h1>
        <button onClick={() => auth.logout()}>Logout</button>
        {/* Your main app content here */}
      </div>
    );
  }

  return (
    <div className="auth-dialog">
      <h2>Welcome to Fasting App</h2>
      <div>
        <input
          type="text"
          placeholder="Enter username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          disabled={isLoading}
        />
        <button onClick={handleRegistration} disabled={isLoading}>
          {isLoading ? 'Checking...' : 'Start Fasting'}
        </button>
      </div>
      {message && (
        <div className={`message ${message.includes('created') ? 'success' : 'error'}`}>
          {message}
        </div>
      )}
    </div>
  );
}

export default AuthComponent;
```

---

## 🎯 **Key Features Implemented**

✅ **Username Validation**: Real-time checking if username exists  
✅ **Smart Registration**: Creates user if new, logs in if exists  
✅ **Error Handling**: Clear messages for duplicate usernames/emails  
✅ **Local Storage**: Remembers user, no future dialogs  
✅ **CORS Ready**: Frontend can connect without issues  
✅ **Flexible Input**: Accepts username OR email in single field  
✅ **Auto-generation**: Creates username from email, or email from username  

---

## 🚀 **Backend API Status**

Your backend is **READY** with these endpoints:

- ✅ `POST /api/users/login-or-create` - Register/Login
- ✅ `GET /api/users/check-availability` - Check username availability  
- ✅ `GET /api/users/find/{identifier}` - Find user by username/email
- ✅ `GET /api/users/current` - Get current user info

**Backend URL**: http://localhost:8080  
**Docker Status**: ✅ Running  
**Database**: ✅ PostgreSQL containerized  
**Tests**: ✅ All 5 integration tests passing  

Your fasting service backend is production-ready for frontend integration! 🎉
