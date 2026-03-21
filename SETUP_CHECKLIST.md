# Quick Setup Checklist: RLS & SMTP Configuration

This checklist will help you quickly set up Supabase RLS policies and SMTP email verification for your Spring Boot Todo App.

---

## 🎯 Part 1: Fix Supabase RLS (5 minutes)

### Problem
You enabled RLS on `users`, `todos`, and `verification_tokens` tables but no policies exist, so your Spring Boot backend cannot access the data.

### Solution: Disable RLS

Since your Spring Boot backend handles authentication/authorization (not Supabase Auth), you should disable RLS.

### Steps:

- [ ] **Step 1:** Go to [Supabase Dashboard](https://supabase.com/dashboard) → Your Project
- [ ] **Step 2:** Click **SQL Editor** in the left sidebar
- [ ] **Step 3:** Click **New Query**
- [ ] **Step 4:** Copy and paste this SQL:

```sql
-- Disable RLS for Spring Boot backend access
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE todos DISABLE ROW LEVEL SECURITY;
ALTER TABLE verification_tokens DISABLE ROW LEVEL SECURITY;

-- Verify RLS is disabled (should all show 'false')
SELECT tablename, rowsecurity as "RLS Enabled"
FROM pg_tables
WHERE tablename IN ('users', 'todos', 'verification_tokens')
ORDER BY tablename;
```

- [ ] **Step 5:** Click **Run** (or press `Ctrl/Cmd + Enter`)
- [ ] **Step 6:** Verify all three tables show `RLS Enabled = false`

✅ **Done!** Your Spring Boot backend can now access the database.

---

## 📧 Part 2: Set Up Resend for Email Verification (5 minutes)

### Step 1: Sign Up for Resend

- [ ] Go to https://resend.com
- [ ] Click **Start Building** or **Sign Up**
- [ ] Sign up with GitHub, Google, or Email (GitHub is fastest)

**Free Tier:** 3,000 emails/month, 100 emails/day (no credit card required)

#### Step 2: Create API Key

- [ ] In the Resend Dashboard, click **API Keys** in the left sidebar
- [ ] Click **Create API Key** button
- [ ] Name: `Todo App Backend`
- [ ] Permission: `Sending access` (default)
- [ ] Domain: Leave as `All domains`
- [ ] Click **Add**

#### Step 3: Copy Your API Key

⚠️ **IMPORTANT:** You'll only see the API key once!

Your API key will look like:
```
re_123abc456def789ghi012jkl345mno678
```

- [ ] **Copy it immediately** and save it somewhere safe

#### Step 4: Update Local Configuration (For Testing)

**⚠️ WARNING: Never commit API keys to Git!**

**Option 1:** Create `application-local.properties` (add to `.gitignore`):

```properties
# Database
DATABASE_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres
DATABASE_USER=postgres
DATABASE_PASSWORD=your-supabase-password

# JWT
JWT_SECRET=your-jwt-secret-key

# Resend API (REPLACE re_xxxxxxxxx WITH YOUR ACTUAL API KEY)
RESEND_API_KEY=re_xxxxxxxxx
RESEND_FROM_EMAIL=onboarding@resend.dev

# Frontend
FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

**Option 2:** Set environment variables in your IDE:

```
RESEND_API_KEY=re_123abc456def789ghi012jkl345mno678
RESEND_FROM_EMAIL=onboarding@resend.dev
FRONTEND_URL=http://localhost:3000
```

- [ ] Choose one option above and configure it
- [ ] **Replace `re_xxxxxxxxx` with your actual Resend API key**
- [ ] Keep `RESEND_FROM_EMAIL=onboarding@resend.dev` for testing

---

## 🚀 Part 3: Configure Railway (Production) (5 minutes)

### Add Environment Variables

- [ ] Go to https://railway.app/dashboard
- [ ] Select your `spring-boot-backend` project
- [ ] Click on your service
- [ ] Go to **Variables** tab
- [ ] Click **RAW Editor** for easier editing
- [ ] Add these variables:

```bash
# Resend API Configuration
RESEND_API_KEY=re_123abc456def789ghi012jkl345mno678
RESEND_FROM_EMAIL=onboarding@resend.dev

# Frontend URL (your Vercel deployment)
FRONTEND_URL=https://your-app-name.vercel.app

# Make sure these exist too:
DATABASE_URL=your-supabase-connection-string
DATABASE_USER=postgres
DATABASE_PASSWORD=your-db-password
JWT_SECRET=your-jwt-secret-key
CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app
```

- [ ] **Replace `re_123abc456def789ghi012jkl345mno678` with your actual Resend API key**
- [ ] Keep `RESEND_FROM_EMAIL=onboarding@resend.dev` for testing (or use your verified domain)
- [ ] Replace `your-app-name.vercel.app` with your actual Vercel URL
- [ ] Click **Save** or the variables will auto-save
- [ ] Wait for Railway to redeploy (automatic)

---

## ✅ Part 4: Test Everything (10 minutes)

### Test 1: Local Backend

- [ ] Start your Spring Boot application locally
- [ ] Check logs for startup errors
- [ ] Open http://localhost:8080/actuator/health (should return OK)

### Test 2: Signup & Email Verification (Local)

Using Postman, curl, or your frontend:

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "your-test-email@gmail.com",
    "password": "TestPassword123!",
    "name": "Test User"
  }'
```

- [ ] API returns 200 OK with user data
- [ ] Check your email inbox (might be in spam first time)
- [ ] You receive "Welcome to ToDo App!" verification email
- [ ] Click the verification link
- [ ] You're redirected to your frontend with success message

### Test 3: Check Logs

Look for this in your application logs:

✅ Success:
```
INFO  c.t.service.EmailService - Verification email sent to your-test-email@gmail.com — Resend ID: abc123def456
```

❌ Failure:
```
WARN  c.t.service.EmailService - Failed to send verification email to ... — Resend error: ...
```

- [ ] Verification email sent successfully (check logs)
- [ ] Email sent successfully with Resend ID in logs

### Test 4: Complete Flow

- [ ] Signup creates unverified account
- [ ] Email is sent and received
- [ ] Verification link works and marks email as verified
- [ ] Login fails BEFORE email verification (should see: "Please verify your email")
- [ ] Login succeeds AFTER email verification
- [ ] Can create todos after login

### Test 5: Production (Railway + Vercel)

- [ ] Visit your Vercel frontend URL
- [ ] Click "Sign Up"
- [ ] Fill out the form and submit
- [ ] Check email for verification link
- [ ] Click verification link
- [ ] Should redirect to frontend with success message
- [ ] Login with verified account
- [ ] Create a todo to confirm everything works

---

## 🐛 Troubleshooting

### Issue: "Invalid API key" Error

**Cause:** Wrong API key or missing environment variable

**Fix:**
- [ ] Make sure you copied the full API key (starts with `re_`)
- [ ] Check for extra spaces before/after the API key
- [ ] Verify the environment variable is `RESEND_API_KEY` (not `SMTP_PASSWORD`)
- [ ] If you lost the key, create a new one in Resend dashboard
- [ ] Restart your Spring Boot application after updating the key

### Issue: Emails not arriving

**Fix:**
- [ ] Check spam/junk folder
- [ ] Check Railway logs for Resend errors: `railway logs`
- [ ] Check Resend dashboard for email logs: https://resend.com/emails
- [ ] Verify `RESEND_API_KEY` is set correctly
- [ ] Check if you've hit rate limits (100/day, 3,000/month)

### Issue: "Please verify your email" even after clicking link

**Fix:**
- [ ] Check the verification token hasn't expired (30 min limit)
- [ ] Check Railway logs for database errors
- [ ] Verify RLS is disabled on `verification_tokens` table
- [ ] Try the "Resend verification email" feature

### Issue: RLS errors in database

**Fix:**
- [ ] Re-run the SQL script from Part 1
- [ ] Verify all three tables show `RLS Enabled = false`
- [ ] Check Spring Boot user has proper database permissions:

```sql
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
```

### Issue: Rate limit exceeded

**Fix:**
- [ ] Check your usage in Resend dashboard: https://resend.com/settings/usage
- [ ] Free tier limits: 100 emails/day, 3,000 emails/month
- [ ] Wait for the limit to reset (daily limit resets at midnight UTC)
- [ ] Consider upgrading to a paid plan if needed
- [ ] Implement rate limiting on your signup endpoint

---

## 📋 Why Resend?

**Advantages over traditional SMTP:**

✅ **No 2FA or App Passwords** - Just an API key  
✅ **Better Deliverability** - Professional email infrastructure  
✅ **Higher Rate Limits** - 3,000/month vs Gmail's 500/day  
✅ **Simple API** - No complex SMTP configuration  
✅ **Developer-Friendly** - Built for modern applications  
✅ **Free Tier** - Perfect for small projects  

**Optional: Verify Your Domain**

For production, use your own domain instead of `onboarding@resend.dev`:

1. In Resend dashboard, click **Domains** → **Add Domain**
2. Add DNS records (SPF, DKIM) to your domain provider
3. Wait for verification (usually < 1 hour)
4. Update: `RESEND_FROM_EMAIL=noreply@yourdomain.com`

See `RESEND_SETUP.md` for detailed instructions.

---

## 🎉 Success Criteria

You're all set when:

- ✅ Supabase RLS is disabled on all three tables
- ✅ Spring Boot can read/write to database without errors
- ✅ User signup sends verification email
- ✅ Email arrives in inbox (check spam first time)
- ✅ Verification link works and marks email as verified
- ✅ Login requires verified email
- ✅ Complete signup → verify → login → create todo flow works
- ✅ Production (Railway + Vercel) works end-to-end

---

## 📚 Additional Resources

- **Detailed RLS Guide:** See `SUPABASE_RLS_SETUP.md`
- **Detailed Resend Guide:** See `RESEND_SETUP.md`
- **SQL Scripts:** See `supabase-rls-policies.sql`
- **Alternative SMTP Guide:** See `SMTP_SETUP.md` (if not using Resend)

---

## 🆘 Still Having Issues?

1. Check Railway logs: `railway logs --tail`
2. Check Spring Boot logs for stack traces
3. Test each component separately (DB → Backend → SMTP)
4. Verify all environment variables are set correctly
5. Try testing locally first before deploying

---

**Estimated Total Time:** 30 minutes

**Last Updated:** 2024

Good luck! 🚀