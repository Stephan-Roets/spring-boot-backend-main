# Quick Fix: Email Verification Not Working

## Problem
Users signing up don't receive verification emails, and `email_verified` stays `false`.

## Root Cause
Railway environment variables are not configured for Resend email service.

---

## ⚡ Quick Fix (3 Steps)

### Step 1: Set Railway Environment Variables

Go to your Railway dashboard and add these variables:

1. **RESEND_API_KEY**
   ```
   re_9a5UMfAL_8FMjqzpbHetLrTxeerHfGtQj
   ```

2. **FRONTEND_URL**
   ```
   http://localhost:3000
   ```
   *(Change to your production URL when deploying)*

3. **RESEND_FROM_EMAIL**
   ```
   onboarding@resend.dev
   ```

### Step 2: Update Frontend .env.local

In your frontend project, update `.env.local`:

```bash
NEXT_PUBLIC_API_URL=https://spring-boot-backend-main-production.up.railway.app
```

### Step 3: Restart Railway Service

Railway should auto-deploy after adding variables. If not:
- Go to Railway dashboard
- Click "Deploy" or "Redeploy"

---

## How to Set Variables in Railway

1. Go to https://railway.app/
2. Find project: `spring-boot-backend-main-production`
3. Click on your service
4. Go to **Variables** tab
5. Click **+ New Variable**
6. Add each variable (name and value)
7. Save (Railway will auto-deploy)

---

## Testing

After Railway redeploys:

1. Go to your frontend signup page
2. Sign up with a new email
3. Check your inbox for verification email
4. Click "Verify Email Address" button
5. Login with your credentials

---

## What This Fixes

✅ Sends verification emails via Resend  
✅ Sets `email_verified` to `true` when user clicks link  
✅ Allows users to login after verification  
✅ Shows verification emails in Resend dashboard  

---

## Verification

Check Railway logs after signup to confirm:
```
INFO c.t.service.EmailService - Verification email sent to user@example.com — Resend ID: abc123
```

---

## Need More Help?

See detailed documentation:
- `RAILWAY_RESEND_SETUP.md` - Complete Railway setup guide
- `RESEND_SETUP.md` - Resend configuration details
- `SETUP_CHECKLIST.md` - Full setup checklist
