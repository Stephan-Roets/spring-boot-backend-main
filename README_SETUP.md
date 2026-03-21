# Spring Boot Todo App - Setup Guide

## 🎯 Quick Start: Get Your App Running in 30 Minutes

This guide will help you:
1. **Fix Supabase RLS** - Enable your Spring Boot backend to access the database
2. **Configure SMTP** - Send email verification to new users
3. **Deploy to Railway** - Get everything working in production

---

## 📋 Prerequisites

Before starting, make sure you have:

- ✅ Spring Boot backend code (this repository)
- ✅ Supabase project created with PostgreSQL database
- ✅ Railway account with backend deployed
- ✅ Vercel account with Next.js frontend deployed
- ✅ Gmail account (or other email provider for SMTP)

---

## 🚀 Part 1: Fix Supabase RLS (5 minutes)

### The Problem

You enabled Row Level Security (RLS) on your tables but haven't created any policies. This prevents your Spring Boot backend from accessing the data.

### The Solution

**Disable RLS** because your Spring Boot backend manages authentication (not Supabase Auth).

### Steps:

1. Go to your **Supabase Dashboard** → https://supabase.com/dashboard
2. Select your project
3. Click **SQL Editor** in the left sidebar
4. Click **New Query**
5. Copy and paste this SQL:

```sql
-- Disable RLS for Spring Boot backend
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE todos DISABLE ROW LEVEL SECURITY;
ALTER TABLE verification_tokens DISABLE ROW LEVEL SECURITY;

-- Verify (should all show 'false')
FROM pg_tables
WHERE tablename IN ('users', 'todos', 'verification_tokens')
ORDER BY tablename;
```

6. Click **Run** or press `Ctrl/Cmd + Enter`
7. Verify all tables show `RLS Enabled = false`

✅ **Done!** Your Spring Boot backend can now access the database.

> 📖 **More Details:** See `SUPABASE_RLS_SETUP.md` for in-depth explanation and alternative approaches.

---

## 📧 Part 2: Set Up Email Verification

> ⭐ **RECOMMENDED:** Use **Resend API** (modern, simpler, better deliverability)
> 
> See **`RESEND_QUICK_START.md`** for 5-minute setup with Resend.
> 
> Already using Resend? Skip to Part 3. Otherwise, continue below for SMTP setup.

---

### Option A: Resend API (Recommended - 5 minutes)

**Why Resend?**
- ✅ No 2FA or App Passwords required
- ✅ Better deliverability
- ✅ 3,000 emails/month free (vs Gmail's 500/day)
- ✅ Simple API key setup

**Quick Setup:**
1. Sign up at https://resend.com
2. Get API key from dashboard
3. Set environment variable: `RESEND_API_KEY=re_xxxxxxxxx`
4. Done!

📖 **Full Guide:** See `RESEND_SETUP.md` or `RESEND_QUICK_START.md`

---

### Option B: Gmail SMTP (Alternative - 15 minutes)

#### Step 1: Enable 2-Factor Authentication

1. Go to https://myaccount.google.com/security
2. Under "How you sign in to Google", enable **2-Step Verification**
3. Follow the setup prompts (you'll need your phone)

#### Step 2: Create App Password

1. Go to https://myaccount.google.com/apppasswords
2. Sign in if prompted
3. Under "App name", type: `Todo App Backend`
4. Click **Create**
5. Copy the 16-character password (example: `abcd efgh ijkl mnop`)
6. **Save it** - you'll only see it once!

#### Step 3: Configure for Local Development

**Option A:** Create `application-local.properties` in `src/main/resources/`

```properties
# Database
DATABASE_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres
DATABASE_USER=postgres
DATABASE_PASSWORD=your-supabase-password

# JWT
JWT_SECRET=your-256-bit-secret-key-change-this

# SMTP
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=abcdefghijklmnop

# Frontend
FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

> ⚠️ **Important:** `application-local.properties` is in `.gitignore` and won't be committed to Git.

**Option B:** Set environment variables in your IDE

```
DATABASE_PASSWORD=your-supabase-password
JWT_SECRET=your-jwt-secret
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=abcdefghijklmnop
```

> 📖 **More Details:** See `SMTP_SETUP.md` for alternative providers (SendGrid, Mailgun, etc.)

---

## ☁️ Part 3: Deploy to Railway (5 minutes)

### Configure Environment Variables

1. Go to https://railway.app/dashboard
2. Select your **spring-boot-backend** project
3. Click on your service
4. Go to **Variables** tab
5. Click **RAW Editor**
6. Add/update these variables:

```bash
# Database (should already exist)
DATABASE_URL=your-supabase-connection-string
DATABASE_USER=postgres
DATABASE_PASSWORD=your-supabase-password

# JWT Secret
JWT_SECRET=your-production-jwt-secret-256-bits-minimum

# SMTP Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-16-char-app-password

# Frontend URL (your Vercel deployment)
FRONTEND_URL=https://your-app-name.vercel.app

# CORS
CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app
```

7. **Save** (or variables auto-save)
8. Railway will automatically redeploy

---

## ✅ Part 4: Test Everything

### Test 1: Local Testing

Start your Spring Boot app locally:

```bash
./mvnw spring-boot:run
```

Or in IntelliJ IDEA: Run `TodoAppApplication.java`

### Test 2: Signup and Email Verification

Using **Postman**, **curl**, or your **Next.js frontend**:

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!",
    "name": "Test User"
  }'
```

**Expected Results:**

1. ✅ API returns 200 OK with user data (email_verified = false)
2. ✅ Email arrives in your inbox (check spam folder first time)
3. ✅ Email contains "Verify Email Address" button
4. ✅ Check logs for: `INFO c.t.service.EmailService - Verification email sent to test@example.com`

### Test 3: Verify Email

1. Click the verification link in the email
2. You're redirected to: `http://localhost:3000/verify-email?token=...`
3. Frontend calls: `GET /api/auth/verify-email?token=...`
4. Response: "Email verified successfully"

### Test 4: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!"
  }'
```

**Expected Results:**

1. ❌ **Before verification:** `401 Unauthorized - "Please verify your email before logging in"`
2. ✅ **After verification:** `200 OK` with access token and user data

### Test 5: Production Testing

1. Go to your Vercel frontend: `https://your-app-name.vercel.app`
2. Click **Sign Up**
3. Fill out the form and submit
4. Check email for verification link
5. Click the link
6. Login with your verified account
7. Create a todo to confirm everything works end-to-end

---

## 🐛 Troubleshooting

### Problem: "No data returned" from database

**Solution:** RLS is still enabled. Re-run the SQL from Part 1.

### Problem: "Authentication failed" SMTP error

**Solutions:**
- Use App Password (16 chars), not your regular Gmail password
- Remove spaces from the app password
- Verify 2FA is enabled on your Google account

### Problem: Emails not arriving

**Solutions:**
- Check spam/junk folder
- Verify `SMTP_USERNAME` is your full email address
- Check Railway logs: `railway logs`
- Look for SMTP errors in application logs

### Problem: "Please verify your email" even after clicking link

**Solutions:**
- Token may have expired (30 min limit) - try resending
- Check RLS is disabled on `verification_tokens` table
- Check Railway logs for database errors

### Problem: Frontend can't connect to backend

**Solutions:**
- Verify `CORS_ALLOWED_ORIGINS` includes your frontend URL
- Check Railway deployment is running
- Test backend health: `https://your-backend.railway.app/actuator/health`

---

## 🏗️ Architecture Overview

```
┌──────────────────────────┐
│   Frontend (Vercel)      │
│   Next.js + TypeScript   │
│   - Signup form          │
│   - Email verification   │
│   - Login form           │
│   - Todo management      │
└────────────┬─────────────┘
             │ HTTPS + JWT
             ▼
┌──────────────────────────┐
│  Backend (Railway)       │
│  Spring Boot + Java      │
│  - Spring Security       │◄─── JWT Authentication
│  - Email Service         │◄─── SMTP (Gmail)
│  - Todo API              │
│  - Auth API              │
└────────────┬─────────────┘
             │ JDBC (postgres user)
             ▼
┌──────────────────────────┐
│  Database (Supabase)     │
│  PostgreSQL              │
│  - users                 │
│  - todos                 │
│  - verification_tokens   │
│  - RLS: DISABLED         │◄─── Trusts backend
└──────────────────────────┘
```

### Security Model

✅ **Your setup is secure because:**

1. Frontend never accesses database directly
2. All requests go through Spring Boot backend
3. Spring Security validates JWT tokens
4. Service layer enforces user ownership:
   ```java
   // TodoService checks user owns the todo
   if (!todo.getUser().getId().equals(userId)) {
       throw new UnauthorizedException("Not authorized");
   }
   ```
5. Database connection is server-side only (not exposed)
6. Supabase is not publicly accessible via Data API

---

## 📁 Project Files

- `SETUP_CHECKLIST.md` - Quick checklist version of this guide
- `SUPABASE_RLS_SETUP.md` - Detailed RLS explanation and alternatives
- `SMTP_SETUP.md` - Comprehensive SMTP configuration guide
- `supabase-rls-policies.sql` - SQL scripts for RLS configuration
- `application-local.properties.template` - Template for local config

---

## 🎓 Key Concepts

### Why Disable RLS?

**Supabase RLS is designed for:**
- Client-side apps using Supabase Auth
- Direct browser access to Supabase Data API

**Your Spring Boot backend:**
- Uses its own authentication (Spring Security + JWT)
- Acts as trusted service layer
- Connects via JDBC (not Supabase Auth)
- **Therefore: RLS should be disabled**

### Email Verification Flow

1. User signs up → Account created (email_verified = false)
2. Verification token generated (30 min expiry)
3. Email