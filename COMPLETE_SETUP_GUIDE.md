# Complete Setup Guide - Spring Boot Todo App

## 🎯 Quick Start: Get Everything Running in 30 Minutes

This comprehensive guide will help you set up your complete Spring Boot Todo App with:
- ✅ **Supabase Database** - PostgreSQL with RLS disabled
- ✅ **Railway Backend** - Spring Boot API with email verification
- ✅ **Resend Email Service** - Modern email API for verification emails
- ✅ **Vercel Frontend** - Next.js application

---

## 📊 Architecture Overview

```
┌──────────────────────────────────────┐
│   Frontend (Vercel)                  │
│   https://your-app.vercel.app        │
│   - Next.js + TypeScript             │
│   - Signup/Login UI                  │
│   - Todo Management                  │
└────────────┬─────────────────────────┘
             │ HTTPS + JWT Auth
             ▼
┌──────────────────────────────────────┐
│   Backend (Railway)                  │
│   https://your-backend.railway.app   │
│   - Spring Boot + Java 21            │
│   - REST API                         │
│   - JWT Authentication               │
│   - Email Service                    │◄──── Resend API
└────────────┬─────────────────────────┘     (Sends verification emails)
             │ JDBC
             ▼
┌──────────────────────────────────────┐
│   Database (Supabase)                │
│   PostgreSQL                         │
│   - users, todos, verification_tokens│
│   - RLS: DISABLED                    │
└──────────────────────────────────────┘
```

---

## ⚠️ CRITICAL SECURITY WARNING

**Your Resend API key was exposed in your message:**
```
re_BoUe6YwQ_GfQ5jU9C8PhiNuocfg9WL2UH
```

**You MUST immediately:**
1. Go to https://resend.com/api-keys
2. Delete this exposed key
3. Create a new API key
4. Use the new key in the setup below

---

## 🚀 Part 1: Supabase Database Setup (5 minutes)

### The Problem
You enabled Row Level Security (RLS) on your tables, but your Spring Boot backend can't access the data because no policies exist.

### The Solution
**Disable RLS** - Your Spring Boot backend handles authentication, not Supabase Auth.

### Steps:

1. **Open Supabase Dashboard**
   - Go to https://supabase.com/dashboard
   - Select your project

2. **Open SQL Editor**
   - Click **SQL Editor** in the left sidebar
   - Click **New Query**

3. **Run This SQL Script:**

```sql
-- Disable RLS for Spring Boot backend access
ALTER TABLE public.users DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.todos DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.verification_tokens DISABLE ROW LEVEL SECURITY;

-- Verify RLS is disabled (all should show 'false')
SELECT
    schemaname as "Schema",
    tablename as "Table Name",
    rowsecurity as "RLS Enabled"
FROM pg_tables
WHERE schemaname = 'public'
    AND tablename IN ('users', 'todos', 'verification_tokens')
ORDER BY tablename;
```

4. **Click Run** (or press `Ctrl/Cmd + Enter`)

5. **Verify Results:**

Expected output:
| Schema | Table Name          | RLS Enabled |
|--------|---------------------|-------------|
| public | todos               | false       |
| public | users               | false       |
| public | verification_tokens | false       |

✅ **Done!** Your backend can now access the database.

**Note:** You may see `auth.users` table with RLS enabled - that's Supabase's internal table. Leave it alone.

---

## 📧 Part 2: Resend Email Service Setup (5 minutes)

### Step 1: Sign Up for Resend

1. Go to **https://resend.com**
2. Click **Start Building** or **Sign Up**
3. Sign up with GitHub, Google, or Email

**Free Tier:** 3,000 emails/month, 100 emails/day

### Step 2: Create a NEW API Key

⚠️ **Important:** Since your old key was exposed, create a fresh one.

1. In Resend Dashboard, click **API Keys** (left sidebar)
2. **First, DELETE the old exposed key:**
   - Find key ending in `...WL2UH`
   - Click **Delete**
   - Confirm deletion

3. **Create a new API key:**
   - Click **Create API Key**
   - Name: `Todo App Production`
   - Permission: **Sending access**
   - Domain: **All domains**
   - Click **Add**

4. **Copy the new API key** (starts with `re_`)
   - You'll only see it once!
   - Save it somewhere safe
   - Example format: `re_XyZ123AbC456DeF789GhI012JkL345MnO678`

✅ **Done!** You now have a secure API key.

---

## ☁️ Part 3: Railway Backend Setup (10 minutes)

### Step 1: Access Railway Dashboard

1. Go to **https://railway.app/dashboard**
2. Select your **spring-boot-backend** project
3. Click on your service
4. Go to **Variables** tab

### Step 2: Configure Environment Variables

Click **RAW Editor** and add/update these variables:

```bash
# ============================================
# Database Configuration
# ============================================
DATABASE_URL=your-supabase-connection-string
DATABASE_USER=postgres
DATABASE_PASSWORD=your-supabase-database-password

# ============================================
# JWT Secret
# ============================================
# Generate with: openssl rand -base64 32
JWT_SECRET=your-production-jwt-secret-minimum-256-bits

# ============================================
# Resend Email Configuration
# ============================================
# Use your NEW API key (not the exposed one!)
RESEND_API_KEY=re_XyZ123AbC456DeF789GhI012JkL345MnO678
RESEND_FROM_EMAIL=onboarding@resend.dev

# ============================================
# Frontend Configuration
# ============================================
FRONTEND_URL=https://your-app-name.vercel.app
CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app
```

### Step 3: Replace Values

**You need to replace:**

1. **`DATABASE_URL`**: Your Supabase connection string
   - Format: `jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres`
   - Find in Supabase → Settings → Database → Connection String (JDBC)

2. **`DATABASE_PASSWORD`**: Your Supabase database password
   - Same password you set when creating Supabase project

3. **`JWT_SECRET`**: A secure random string
   - Generate with: `openssl rand -base64 32`
   - Or use any secure 256-bit key

4. **`RESEND_API_KEY`**: Your NEW Resend API key from Part 2
   - Example: `re_XyZ123AbC456DeF789GhI012JkL345MnO678`

5. **`FRONTEND_URL`**: Your Vercel deployment URL
   - Format: `https://your-app-name.vercel.app`
   - ⚠️ NO trailing slash!

6. **`CORS_ALLOWED_ORIGINS`**: Same as your frontend URL
   - Format: `https://your-app-name.vercel.app`
   - ⚠️ NO trailing slash!

### Step 4: Save and Deploy

1. Click **Save** (or variables auto-save)
2. Railway will **automatically redeploy**
3. Go to **Deployments** tab
4. Wait for deployment to complete (2-5 minutes)
5. Check for errors in deployment logs

### Step 5: Verify Deployment

**Check health endpoint:**
```bash
curl https://your-backend.railway.app/actuator/health
```

**Expected response:**
```json
{"status":"UP"}
```

✅ **Done!** Your backend is deployed and configured.

---

## 🌐 Part 4: Vercel Frontend Setup (5 minutes)

### Step 1: Get Your Railway Backend URL

1. In Railway dashboard, go to your service
2. Go to **Settings** tab
3. Find **Public Networking** section
4. Copy your public URL
   - Example: `https://spring-boot-backend-production-xxxx.up.railway.app`

### Step 2: Configure Vercel Environment Variables

1. Go to **https://vercel.com/dashboard**
2. Select your **frontend** project
3. Go to **Settings** → **Environment Variables**
4. Add these variables:

**For Production Environment:**

| Name | Value |
|------|-------|
| `NEXT_PUBLIC_API_URL` | `https://your-backend.railway.app` |
| `NEXT_PUBLIC_APP_URL` | `https://your-app-name.vercel.app` |

**Replace with your actual URLs:**
- `NEXT_PUBLIC_API_URL`: Your Railway backend URL from Step 1
- `NEXT_PUBLIC_APP_URL`: Your Vercel deployment URL

⚠️ **Important:** 
- NO trailing slashes!
- Variables must start with `NEXT_PUBLIC_` to be accessible in browser

### Step 3: Redeploy Frontend

1. Go to **Deployments** tab
2. Click **Redeploy** on the latest deployment
3. Wait for deployment to complete (1-2 minutes)

✅ **Done!** Your frontend is configured.

---

## ✅ Part 5: Test Everything (10 minutes)

### Test 1: Backend Health Check

```bash
curl https://your-backend.railway.app/actuator/health
```

**Expected:** `{"status":"UP"}`

### Test 2: Complete User Flow

#### 2.1: Sign Up

1. Go to **your Vercel URL**: `https://your-app-name.vercel.app`
2. Click **Sign Up**
3. Fill in the form:
   - **Email:** `stefanroetsprograming@gmail.com`
   - **Password:** `TestPass123!`
   - **Name:** `Stefan Roets`
4. Click **Submit**

**Expected:**
- ✅ Success message appears
- ✅ No errors in browser console
- ✅ Redirected to verification pending or login page

#### 2.2: Check Email

1. Open your email inbox (`stefanroetsprograming@gmail.com`)
2. Check for email from `onboarding@resend.dev`
   - Subject: **"Verify your email - ToDo App"**
   - ⚠️ Check spam folder if not in inbox!

**Expected:**
- ✅ Email arrives within 1 minute
- ✅ Email contains "Welcome to ToDo App!" message
- ✅ Email has "Verify Email Address" button

#### 2.3: Verify Email

1. Click **"Verify Email Address"** button in email
2. Browser opens your Vercel app

**Expected:**
- ✅ Opens: `https://your-app-name.vercel.app/verify-email?token=...`
- ✅ Success message: "Email verified successfully!"
- ✅ Redirected to login page

#### 2.4: Login

1. On login page, enter:
   - **Email:** `stefanroetsprograming@gmail.com`
   - **Password:** `TestPass123!`
2. Click **Login**

**Expected:**
- ✅ Successfully logged in
- ✅ JWT token stored (check browser dev tools → Application → Local Storage)
- ✅ Redirected to dashboard/todos page

#### 2.5: Create a Todo

1. Click **Add Todo** or **New Todo**
2. Fill in:
   - **Title:** `My First Todo`
   - **Description:** `Testing the application`
   - **Priority:** `High`
3. Click **Save**

**Expected:**
- ✅ Todo created successfully
- ✅ Todo appears in the list
- ✅ Can edit/delete the todo

### Test 3: Check Logs

#### Railway Logs:

1. In Railway dashboard → Deployments → Latest deployment → **View Logs**
2. Look for:

**✅ Success:**
```
INFO c.t.service.EmailService - Verification email sent to stefanroetsprograming@gmail.com — Resend ID: abc123
```

**❌ Failure (if you see this, check your API key):**
```
WARN c.t.service.EmailService - Failed to send verification email to ... — Resend error: Invalid API key
```

#### Resend Dashboard:

1. Go to **https://resend.com/emails**
2. You should see your sent email
3. Status should be **Delivered**

---

## 🐛 Troubleshooting

### Issue 1: "No data returned" from Database

**Solution:**
- Re-run the SQL from Part 1 to disable RLS
- Verify all three tables show `RLS Enabled = false`

### Issue 2: "Invalid API key" Error

**Solutions:**
- Make sure you're using the NEW API key (not the exposed one)
- Check for spaces before/after the key in Railway
- Verify the key starts with `re_`
- Redeploy Railway after updating the key

### Issue 3: Emails Not Arriving

**Solutions:**
- ✅ Check spam/junk folder
- ✅ Verify `RESEND_API_KEY` is set correctly in Railway
- ✅ Check Resend dashboard (https://resend.com/emails) for delivery status
- ✅ Try a different email address
- ✅ Check Railway logs for SMTP errors

### Issue 4: CORS Errors

**Error:**
```
Access to fetch has been blocked by CORS policy
```

**Solutions:**
- Verify `CORS_ALLOWED_ORIGINS` in Railway matches your Vercel URL exactly
- Ensure NO trailing slash: `https://your-app.vercel.app` (not `https://your-app.vercel.app/`)
- Redeploy Railway after changing CORS settings
- Check browser console for the exact origin being blocked

### Issue 5: Verification Link Points to Localhost

**Problem:** Email link shows `http://localhost:3000` instead of your Vercel URL

**Solution:**
- Update `FRONTEND_URL` in Railway to: `https://your-app-name.vercel.app`
- Ensure NO trailing slash
- Redeploy Railway
- Test with a NEW signup (old emails will still have old URL)

### Issue 6: 404 Not Found on API Calls

**Solutions:**
- Check `NEXT_PUBLIC_API_URL` in Vercel is correct
- Should be: `https://your-backend.railway.app` (no `/api` at the end)
- Verify backend is running: `curl https://your-backend.railway.app/actuator/health`
- Check frontend code uses correct endpoint format: `${API_URL}/api/auth/signup`

### Issue 7: JWT Token Not Working

**Solutions:**
- Check token is being sent in Authorization header: `Bearer ${token}`
- Verify token is stored after login: `localStorage.getItem('accessToken')`
- Check token hasn't expired (24 hours default)
- Try logging out and logging in again

---

## 📋 Complete Configuration Checklist

### Supabase Database
- [ ] RLS disabled on `public.users`
- [ ] RLS disabled on `public.todos`
- [ ] RLS disabled on `public.verification_tokens`
- [ ] SQL query shows all three as `false`

### Resend Email Service
- [ ] Signed up for Resend account
- [ ] Deleted old exposed API key
- [ ] Created new API key
- [ ] API key saved securely

### Railway Backend
- [ ] `DATABASE_URL` configured
- [ ] `DATABASE_USER` set to `postgres`
- [ ] `DATABASE_PASSWORD` configured
- [ ] `JWT_SECRET` set (256-bit minimum)
- [ ] `RESEND_API_KEY` set to NEW key
- [ ] `RESEND_FROM_EMAIL` set (or using default)
- [ ] `FRONTEND_URL` set to Vercel URL (no trailing slash)
- [ ] `CORS_ALLOWED_ORIGINS` set to Vercel URL (no trailing slash)
- [ ] Deployment successful
- [ ] Health endpoint returns `{"status":"UP"}`

### Vercel Frontend
- [ ] `NEXT_PUBLIC_API_URL` set to Railway backend URL
- [ ] `NEXT_PUBLIC_APP_URL` set to Vercel frontend URL
- [ ] No trailing slashes in URLs
- [ ] Redeployed after adding variables
- [ ] Deployment successful

### Testing
- [ ] Can sign up from frontend
- [ ] Verification email received
- [ ] Email not in spam folder
- [ ] Verification link works and redirects to Vercel
- [ ] Email marked as verified
- [ ] Can login after verification
- [ ] Login requires verified email (can't login before verification)
- [ ] Can create todos after login
- [ ] Todos saved to database
- [ ] No CORS errors in browser console
- [ ] No errors in Railway logs
- [ ] Email shows as "Delivered" in Resend dashboard

---

## 🎯 Quick Reference

### Important URLs

| Service | URL |
|---------|-----|
| Supabase Dashboard | https://supabase.com/dashboard |
| Resend Dashboard | https://resend.com/overview |
| Resend Emails Log | https://resend.com/emails |
| Resend API Keys | https://resend.com/api-keys |
| Railway Dashboard | https://railway.app/dashboard |
| Vercel Dashboard | https://vercel.com/dashboard |

### Environment Variables Summary

**Railway Backend:**
```bash
DATABASE_URL=jdbc:postgresql://...
DATABASE_USER=postgres
DATABASE_PASSWORD=...
JWT_SECRET=...
RESEND_API_KEY=re_...
RESEND_FROM_EMAIL=onboarding@resend.dev
FRONTEND_URL=https://your-app.vercel.app
CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
```

**Vercel Frontend:**
```bash
NEXT_PUBLIC_API_URL=https://your-backend.railway.app
NEXT_PUBLIC_APP_URL=https://your-app.vercel.app
```

### Testing Commands

```bash
# Test backend health
curl https://your-backend.railway.app/actuator/health

# Test signup
curl -X POST https://your-backend.railway.app/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!","name":"Test User"}'

# Test login
curl -X POST https://your-backend.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'
```

---

## 📚 Detailed Documentation

For more in-depth information, see these guides:

- **`SUPABASE_RLS_SETUP.md`** - Detailed RLS explanation
- **`RESEND_SETUP.md`** - Complete Resend configuration guide
- **`RAILWAY_RESEND_SETUP.md`** - Railway-specific Resend setup
- **`VERCEL_FRONTEND_SETUP.md`** - Frontend configuration details
- **`SETUP_CHECKLIST.md`** - Step-by-step checklist
- **`supabase-debug-queries.sql`** - Debugging SQL queries

---

## 🔒 Security Best Practices

### ✅ Do's

- ✅ Use environment variables for all secrets
- ✅ Never commit API keys or passwords to Git
- ✅ Rotate API keys regularly
- ✅ Use HTTPS in production (always)
- ✅ Implement rate limiting on signup endpoint
- ✅ Validate email addresses
- ✅ Use strong JWT secrets (256-bit minimum)
- ✅ Set appropriate CORS origins (not `*`)

### ❌ Don'ts

- ❌ Don't hardcode API keys in code
- ❌ Don't commit `.env` or `application-local.properties`
- ❌ Don't use `CORS_ALLOWED_ORIGINS=*` in production
- ❌ Don't share API keys in public messages/forums
- ❌ Don't reuse the exposed API key
- ❌ Don't use weak passwords
- ❌ Don't skip email verification

---

## 🎉 Success! You're Ready to Go!

Your complete Spring Boot Todo App is now fully configured and deployed:

✅ **Database** - Supabase PostgreSQL with RLS properly configured  
✅ **Backend** - Spring Boot on Railway with JWT authentication  
✅ **Email** - Resend API for verification emails  
✅ **Frontend** - Next.js on Vercel with full integration  

### What You Can Do Now:

1. **Users can sign up** with email verification
2. **Verification emails** are sent automatically via Resend
3. **Users must verify email** before they can login
4. **Authenticated users** can create, read, update, and delete todos
5. **Everything is deployed** and working in production

### Next Steps:

- 🎨 Customize the email template
- 🌐 Add your own domain to Resend
- 📊 Set up monitoring and alerts
- 🚀 Add more features to your app
- 🔐 Implement additional security measures

---

## 🆘 Still Need Help?

### Debugging Steps:

1. **Check Railway logs** for backend errors
2. **Check Vercel logs** for frontend errors
3. **Check browser console** for JavaScript errors
4. **Check Resend dashboard** for email delivery status
5. **Run SQL queries** from `supabase-debug-queries.sql`
6. **Test each component** individually

### Support Resources:

- **Supabase Support:** https://supabase.com/support
- **Resend Support:** hello@resend.com
- **Railway Support:** https://railway.app/help
- **Vercel Support:** https://vercel.com/help

---

**Estimated Total Setup Time:** 30-40 minutes

**Difficulty Level:** Beginner-Friendly

**Last Updated:** 2024

---

**Congratulations! You've successfully deployed your full-stack Spring Boot Todo App! 🚀**