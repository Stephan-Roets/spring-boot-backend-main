# Vercel Frontend Configuration Guide

This guide explains how to configure your Next.js frontend on Vercel to work with your Spring Boot backend on Railway and Resend email service.

---

## 🎯 Overview

Your application architecture:

```
┌─────────────────────────┐
│   Frontend (Vercel)     │
│   Next.js + TypeScript  │
│   - Signup form         │
│   - Email verification  │
│   - Login form          │
│   - Todo management     │
└───────────┬─────────────┘
            │ HTTPS + JWT
            ▼
┌─────────────────────────┐
│  Backend (Railway)      │
│  Spring Boot + Java     │
│  - REST API             │
│  - Authentication       │◄─── Resend Email API
│  - Email Service        │
└───────────┬─────────────┘
            │ JDBC
            ▼
┌─────────────────────────┐
│  Database (Supabase)    │
│  PostgreSQL             │
└─────────────────────────┘
```

---

## 📋 Prerequisites

Before configuring Vercel, ensure you have:

- ✅ Next.js frontend deployed to Vercel
- ✅ Spring Boot backend deployed to Railway
- ✅ Railway backend URL (e.g., `https://your-backend.railway.app`)
- ✅ Resend configured on Railway
- ✅ Vercel project access

---

## 🚀 Quick Setup (5 Minutes)

### Step 1: Get Your Railway Backend URL

1. Go to **https://railway.app/dashboard**
2. Select your **spring-boot-backend** project
3. Click on your service
4. Go to **Settings** tab
5. Find the **Public Networking** section
6. Copy your public URL (e.g., `your-backend.railway.app`)

**Full URL format:**
```
https://spring-boot-backend-production-xxxx.up.railway.app
```

### Step 2: Configure Vercel Environment Variables

#### Option A: Using Vercel Dashboard (Recommended)

1. Go to **https://vercel.com/dashboard**
2. Select your **frontend** project
3. Go to **Settings** tab
4. Click **Environment Variables** in the left sidebar
5. Add the following variables:

**For Production:**

| Name | Value | Environment |
|------|-------|-------------|
| `NEXT_PUBLIC_API_URL` | `https://your-backend.railway.app` | Production |
| `NEXT_PUBLIC_APP_URL` | `https://your-app-name.vercel.app` | Production |

**For Preview (Optional):**

| Name | Value | Environment |
|------|-------|-------------|
| `NEXT_PUBLIC_API_URL` | `https://your-backend.railway.app` | Preview |
| `NEXT_PUBLIC_APP_URL` | `https://your-app-name.vercel.app` | Preview |

**For Development (Optional):**

| Name | Value | Environment |
|------|-------|-------------|
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | Development |
| `NEXT_PUBLIC_APP_URL` | `http://localhost:3000` | Development |

6. Click **Save**

#### Option B: Using Vercel CLI

```bash
# Install Vercel CLI if you haven't already
npm install -g vercel

# Login to Vercel
vercel login

# Link to your project
vercel link

# Set production environment variables
vercel env add NEXT_PUBLIC_API_URL production
# Enter: https://your-backend.railway.app

vercel env add NEXT_PUBLIC_APP_URL production
# Enter: https://your-app-name.vercel.app

# Pull environment variables to local .env.local (optional)
vercel env pull .env.local
```

### Step 3: Verify Environment Variables in Code

Your Next.js frontend should use these environment variables:

**Example API client configuration:**

```typescript
// lib/api.ts or similar
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export const apiClient = {
  baseURL: API_URL,
  // ... other config
};
```

**Example usage:**

```typescript
// Sign up
const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/signup`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password, name }),
});

// Login
const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/login`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password }),
});

// Verify email
const response = await fetch(
  `${process.env.NEXT_PUBLIC_API_URL}/api/auth/verify-email?token=${token}`,
  { method: 'GET' }
);
```

### Step 4: Redeploy Frontend

After adding environment variables:

1. Go to **Deployments** tab in Vercel
2. Click **Redeploy** on the latest deployment
3. OR push a new commit to your Git repository (auto-deploys)

Wait for the deployment to complete (usually 1-2 minutes).

---

## 🔄 Email Verification Flow

Here's how the email verification works between Vercel, Railway, and Resend:

### 1. User Signs Up

**Frontend (Vercel):**
```typescript
// User fills signup form
POST https://your-app.vercel.app/signup

// Frontend sends to backend
POST https://your-backend.railway.app/api/auth/signup
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "name": "John Doe"
}
```

**Backend (Railway):**
```java
// Creates user (email_verified = false)
// Generates verification token (30 min expiry)
// Sends email via Resend
```

### 2. User Receives Email

**Resend sends email to:** `user@example.com`

**Email contains link:**
```
https://your-app.vercel.app/verify-email?token=eyJhbGc...
```

### 3. User Clicks Verification Link

**Frontend (Vercel):**
```typescript
// User clicks link in email
// Browser opens: https://your-app.vercel.app/verify-email?token=eyJhbGc...

// Frontend calls backend
GET https://your-backend.railway.app/api/auth/verify-email?token=eyJhbGc...
```

**Backend (Railway):**
```java
// Validates token
// Marks email as verified
// Returns success message
```

### 4. User Can Login

**Frontend (Vercel):**
```typescript
// User logs in
POST https://your-backend.railway.app/api/auth/login
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

// Backend returns JWT token
// Frontend stores token and redirects to dashboard
```

---

## ✅ Testing the Complete Flow

### Test 1: Sign Up from Frontend

1. Go to your Vercel URL: `https://your-app-name.vercel.app`
2. Navigate to **Sign Up** page
3. Fill in the form:
   - **Email:** `stefanroetsprograming@gmail.com`
   - **Password:** `TestPass123!`
   - **Name:** `Stefan Roets`
4. Click **Sign Up**

**Expected Result:**
- ✅ Success message: "Account created! Check your email to verify."
- ✅ Redirected to login or verification pending page

### Test 2: Receive and Click Verification Email

1. Check your email inbox (check spam folder!)
2. Look for email from `onboarding@resend.dev`
3. Subject: **"Verify your email - ToDo App"**
4. Click **"Verify Email Address"** button

**Expected Result:**
- ✅ Browser opens: `https://your-app-name.vercel.app/verify-email?token=...`
- ✅ Success message: "Email verified successfully! You can now log in."
- ✅ Redirected to login page

### Test 3: Login with Verified Account

1. Go to login page
2. Enter:
   - **Email:** `stefanroetsprograming@gmail.com`
   - **Password:** `TestPass123!`
3. Click **Login**

**Expected Result:**
- ✅ Successfully logged in
- ✅ JWT token stored (localStorage or cookies)
- ✅ Redirected to dashboard/todos page

### Test 4: Create a Todo

1. On the todos page, click **Add Todo**
2. Fill in:
   - **Title:** `Test Todo`
   - **Description:** `Testing the app`
3. Click **Save**

**Expected Result:**
- ✅ Todo created successfully
- ✅ Todo appears in the list
- ✅ Can update/delete the todo

---

## 🐛 Troubleshooting

### Issue 1: CORS Errors

**Symptom:**
```
Access to fetch at 'https://your-backend.railway.app/api/auth/signup'
from origin 'https://your-app.vercel.app' has been blocked by CORS policy
```

**Solution:**

1. **Check Railway environment variables:**
   ```bash
   CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app
   ```

2. **Ensure NO trailing slash:**
   ```bash
   # ❌ Wrong
   CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app/

   # ✅ Correct
   CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app
   ```

3. **For multiple domains (if needed):**
   ```bash
   CORS_ALLOWED_ORIGINS=https://your-app.vercel.app,https://your-app-preview.vercel.app
   ```

4. **Redeploy Railway service** after changing CORS settings

### Issue 2: API Calls Failing (404 Not Found)

**Symptom:**
```
GET https://your-backend.railway.app/api/auth/signup 404 Not Found
```

**Solution:**

1. **Verify backend URL in Vercel:**
   - Check `NEXT_PUBLIC_API_URL` is correct
   - Should be: `https://your-backend.railway.app` (no `/api` at the end)

2. **Check API endpoint in frontend code:**
   ```typescript
   // ❌ Wrong (doubles the /api)
   fetch(`${API_URL}/api/api/auth/signup`)

   // ✅ Correct
   fetch(`${API_URL}/api/auth/signup`)
   ```

3. **Test backend directly:**
   ```bash
   curl https://your-backend.railway.app/actuator/health
   # Should return: {"status":"UP"}
   ```

### Issue 3: Verification Link Points to Wrong Domain

**Symptom:**
- Email contains: `http://localhost:3000/verify-email?token=...`
- Should be: `https://your-app.vercel.app/verify-email?token=...`

**Solution:**

1. **Update Railway `FRONTEND_URL`:**
   ```bash
   FRONTEND_URL=https://your-app-name.vercel.app
   ```

2. **Ensure NO trailing slash:**
   ```bash
   # ❌ Wrong
   FRONTEND_URL=https://your-app-name.vercel.app/

   # ✅ Correct
   FRONTEND_URL=https://your-app-name.vercel.app
   ```

3. **Redeploy Railway service**

4. **Test with a new signup** (old emails will still have the old URL)

### Issue 4: Environment Variables Not Loading

**Symptom:**
- `process.env.NEXT_PUBLIC_API_URL` is `undefined`
- API calls fail

**Solution:**

1. **Ensure variable name starts with `NEXT_PUBLIC_`:**
   ```bash
   # ❌ Wrong (not accessible in browser)
   API_URL=https://your-backend.railway.app

   # ✅ Correct (accessible in browser)
   NEXT_PUBLIC_API_URL=https://your-backend.railway.app
   ```

2. **Redeploy Vercel after adding variables:**
   - Go to Deployments → Click **Redeploy**

3. **Check variables in build logs:**
   - Go to Deployments → Click on deployment → View logs
   - Look for environment variables being loaded

4. **For local development, create `.env.local`:**
   ```bash
   NEXT_PUBLIC_API_URL=http://localhost:8080
   NEXT_PUBLIC_APP_URL=http://localhost:3000
   ```

### Issue 5: JWT Token Issues

**Symptom:**
- User logged in but API calls return 401 Unauthorized

**Solution:**

1. **Check token is being sent:**
   ```typescript
   fetch(`${API_URL}/api/todos`, {
     headers: {
       'Authorization': `Bearer ${token}`,
       'Content-Type': 'application/json',
     },
   });
   ```

2. **Verify token storage:**
   ```typescript
   // Store token after login
   localStorage.setItem('accessToken', response.accessToken);

   // Retrieve token for API calls
   const token = localStorage.getItem('accessToken');
   ```

3. **Check token expiry:**
   - JWT tokens expire after 24 hours (default)
   - Implement token refresh or re-login

---

## 🔐 Security Best Practices

### 1. Environment Variables

✅ **Use `NEXT_PUBLIC_` prefix** for client-side variables  
✅ **Never commit `.env.local`** to Git (add to `.gitignore`)  
✅ **Use different values** for production, preview, and development  
✅ **Rotate secrets regularly**  

### 2. API Communication

✅ **Always use HTTPS** in production  
✅ **Validate CORS origins** in backend  
✅ **Send JWT in Authorization header**, not URL params  
✅ **Implement request timeout** for API calls  

### 3. Authentication

✅ **Store JWT securely** (httpOnly cookies recommended)  
✅ **Implement token refresh** for better UX  
✅ **Clear token on logout**  
✅ **Handle 401 errors** and redirect to login  

---

## 📊 Environment Variable Reference

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend API base URL | `https://your-backend.railway.app` |

### Optional Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_APP_URL` | Frontend URL (for redirects) | `https://your-app.vercel.app` |

### ⚠️ Important Notes

- Variables starting with `NEXT_PUBLIC_` are **embedded in the browser bundle**
- Don't put secrets in `NEXT_PUBLIC_` variables (they're visible to users)
- Backend secrets should only be in Railway, not Vercel

---

## 🎯 Complete Configuration Checklist

### Vercel Frontend
- [ ] `NEXT_PUBLIC_API_URL` set to Railway backend URL
- [ ] `NEXT_PUBLIC_APP_URL` set to Vercel frontend URL
- [ ] Environment variables set for Production environment
- [ ] Redeployed after adding variables
- [ ] No trailing slashes in URLs
- [ ] API calls use correct endpoint URLs

### Railway Backend
- [ ] `FRONTEND_URL` set to Vercel frontend URL
- [ ] `CORS_ALLOWED_ORIGINS` set to Vercel frontend URL
- [ ] `RESEND_API_KEY` configured
- [ ] Backend is publicly accessible
- [ ] Health endpoint responds: `/actuator/health`

### Testing
- [ ] Can sign up from Vercel frontend
- [ ] Receive verification email
- [ ] Verification link redirects to Vercel URL
- [ ] Can verify email successfully
- [ ] Can login after verification
- [ ] Can create/read/update/delete todos
- [ ] No CORS errors in browser console

---

## 📚 Additional Resources

### Documentation
- **Vercel Environment Variables**: https://vercel.com/docs/concepts/projects/environment-variables
- **Next.js Environment Variables**: https://nextjs.org/docs/basic-features/environment-variables
- **Railway Setup**: See `RAILWAY_RESEND_SETUP.md`
- **Resend Setup**: See `RESEND_SETUP.md`

### Useful Commands

```bash
# Check Vercel deployment
vercel inspect

# View Vercel logs
vercel logs

# List environment variables
vercel env ls

# Pull environment variables to local
vercel env pull .env.local

# Deploy to production
vercel --prod
```

---

## 🎉 You're All Set!

Your Vercel frontend is now configured to work with:
- ✅ Railway backend for API calls
- ✅ Resend for email verification
- ✅ Supabase for database (via backend)

### What's Working:
1. User signs up on Vercel frontend
2. Backend on Railway creates account
3. Resend sends verification email
4. User clicks link, redirects to Vercel
5. Backend verifies email
6. User logs in and uses the app

**Happy building! 🚀**

---

## 🆘 Need Help?

### Check These First:
1. Vercel deployment logs
2. Railway deployment logs
3. Browser console for errors
4. Network tab for failed requests

### Common Issues:
- CORS errors → Check `CORS_ALLOWED_ORIGINS` in Railway
- 404 errors → Verify `NEXT_PUBLIC_API_URL` in Vercel
- Email links wrong → Check `FRONTEND_URL` in Railway
- Variables not loading → Redeploy after adding variables

### Support:
- **Vercel Support**: https://vercel.com/help
- **Railway Support**: https://railway.app/help
- **Resend Support**: hello@resend.com