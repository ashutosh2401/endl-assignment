# Endl Assignment

This project contains authentication APIs for user signup, login, profile, and 2FA functionality.

---

## How to Run the App with Docker Compose

### 1. Clone the Repository

```bash
git clone https://github.com/ashutosh2401/endl-assignment.git
cd endl-assignment
```

### 2. Run Docker Compose

```bash
docker-compose up --build
```

# API Testing Guide (Postman Collection)

This document explains how to test all the authentication APIs using Postman.

---

## Postman Collection Link

You can import the complete postman collection shared.

---

## API Requests

### 1️⃣ Signup

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/signup`
- **Headers**: None
- **Body**: `raw` / `JSON`

```json
{
  "email": "mishrashutosh1998@gmail.com",
  "password": "test@123",
  "name": "Ashutosh Mishra",
  "designation": "Developer"
}
```

---

### 2️⃣ Login

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/login`
- **Headers**: None
- **Body**: `raw` / `JSON`

```json
{
  "email": "mishrashutosh1998@gmail.com",
  "password": "test@123"
}
```

Save the `token` from the response. You’ll need it in `Authorization` headers below.

---

### 3️⃣ Verify Email OTP

- **Method**: `GET`
- **URL**: *(Not specified — refer to the server logs or backend route for OTP verification)*
- **Headers**: None
- **Body**: `raw` / `JSON`
```json
{
  "email": "mishrashutosh1998@gmail.com",
  "otp": "546098"
}
```

---

### 4️⃣ Profile Info

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/auth/profile`
- **Headers**:

```text
Authorization: <token-from-login>
```

- ✅ Returns the logged-in user's profile information.

---

### 5️⃣ Update Profile

- **Method**: `PUT`
- **URL**: `http://localhost:8080/api/auth/profile`
- **Headers**:

```text
Authorization: <token-from-login>
Content-Type: application/json
```

- **Body**: `raw` / `JSON`

```json
{
  "name": "Ashutosh Updated",
  "designation": "Lead Developer"
}
```

---

### 6️⃣ Enable 2FA

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/enable-2fa`
- **Headers**:

```text
Authorization: <token-from-login>
```

Returns a QR code URL or a secret to configure 2FA in an app like Google Authenticator.

---

### 7️⃣ Verify 2FA

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/verify-2fa?code=123456`
- **Headers**: None
- **Query Parameters**:

```text
code=123456
```

Confirms whether the provided 2FA code is valid.

---

## Notes

- Replace `<token-from-login>` with the actual token received after logging in.
- If the port or domain differs, adjust the URLs accordingly in Postman.
- Make sure to start your backend server before running the tests.

---
   
