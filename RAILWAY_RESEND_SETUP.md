# Railway Setup Guide for Resend Email Service

This guide will help you configure your Spring Boot backend on Railway to use Resend for email verification.

---

## ⚠️ IMPORTANT SECURITY NOTE

**Your API key `re_BoUe6YwQ_GfQ5jU9C8PhiNuocfg9WL2UH` was exposed in a public message.**

You must **immediately revoke this key** and create a new one:

1. Go to https://resend.com/api-keys
2. Find the exposed key and click **Delete**
3. Create a new API key (follow steps below)
4. Use the new key in Railway

---

## 🚀 Quick Setup (5 Minutes)

### Step 1: Get a Fresh Resend API Key

1. Go to **https://resend.com/api-keys**
2. Click **Create API Key**
3. Name: `Todo App Production` (or similar)
4. Permission: **Sending access**
5. Domain: **All domains**
6. Click **Add**
7. **Copy the new API key** (starts with `re_`)
   - You'll only see it once!
   - Example: `re_XyZ123AbC456DeF789GhI012JkL345`

---

### Step 2: Configure Railway Environment Variables

#### Option A: Using Railway Web Dashboard (Recommended)

1. Go to **https://railway.app/dashboard**
2. Select your project: **spring-boot-backend**
3. Click on your service
4. Navigate to **Variables** tab
5. Click **RAW Editor** for easier editing
6. Add/update these variables:

```bash
# ============================================
# Database Configuration (Should Already Exist)
# ============================================
DATABASE_URL=your-supabase-connection-string
DATABASE_USER=postgres
DATABASE_PASSWORD=your-supabase-password

# ============================================
# JWT Configuration
# ============================================
JWT_SECRET=your-production-jwt-secret-256-bits-minimum

# ============================================
# Resend Email Configuration
# ============================================
RESEND_API_KEY=re_XyZ123AbC456DeF789GhI012JkL345
RESEND_FROM_EMAIL=onboarding@resend.dev

# ============================================
# Frontend Configuration
# ============================================
FRONTEND_URL=https://your-app-name.vercel.app
CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app
```

7. **Replace the values:**
   - `RESEND_API_KEY`: Your NEW Resend API key (from Step 1)
   - `FRONTEND_URL`: Your actual Vercel deployment URL
   - `CORS_ALLOWED_ORIGINS`: Same as your frontend URL

8. Railway will **automatically redeploy** when you save

#### Option B: Using Railway CLI

```bash
# Install Railway CLI if you haven't already
npm install -g @railway/cli

# Login to Railway
railway login

# Link to your project
railway link

# Set environment variables
railway variables set RESEND_API_KEY=re_XyZ123AbC456DeF789GhI012JkL345
railway variables set RESEND_FROM_EMAIL=onboarding@resend.dev
railway variables set FRONTEND_URL=https://your-app-name.vercel.app
railway variables set CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app

# Trigger a new deployment
railway up
```

---

### Step 3: Verify Deployment

1. Go to **Deployments** tab in Railway
2. Wait for the deployment to complete (usually 2-5 minutes)
3. Check the deployment logs for errors
4. Look for successful startup message

**Successful startup log:**
```
Started TodoAppApplication in X.XXX seconds
```

**No Resend errors:**
```
✅ No "Failed to create bean 'emailService'" errors
✅ No "Invalid API key" errors
```

---

### Step 4: Test Email Sending

#### Test from your deployed frontend:

1. Go to your Vercel URL: `https://your-app-name.vercel.app`
2. Click **Sign Up**
3. Fill in the form with your email: `stefanroetsprograming@gmail.com`
4. Submit the form
5. **Check your email inbox** (might be in spam first time)
6. You should receive: **"Verify your email - ToDo App"**

#### Test with curl:

```bash
curl -X POST https://your-backend.railway.app/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "stefanroetsprograming@gmail.com",
    "password": "TestPass123!",
    "name": "Stefan Roets"
  }'
```

**Expected response:**
```json
{
  "id": "uuid-here",
  "email": "stefanroetsprograming@gmail.com",
  "name": "Stefan Roets",
  "emailVerified": false,
  ...
}
```

---

### Step 5: Check Railway Logs

View logs to verify email was sent:

1. In Railway dashboard, go to **Deployments**
2. Click on the latest deployment
3. Click **View Logs**
4. Look for:

**✅ Success:**
```
INFO c.t.service.EmailService - Verification email sent to stefanroetsprograming@gmail.com — Resend ID: abc123
```

**❌ Failure (check your API key):**
```
WARN c.t.service.EmailService - Failed to send verification email to ... — Resend error: Invalid API key
```

---

## 📊 Monitor Email Delivery

### Resend Dashboard

Check email status in real-time:

1. Go to **https://resend.com/emails**
2. You'll see all emails sent
3. Check delivery status:
   - ✅ **Delivered** - Email successfully sent
   - ❌ **Bounced** - Invalid email address
   - ⏳ **Queued** - Being processed
   - 📧 **Sent** - In transit

### View Email Details

Click on any email to see:
- Full email content (HTML preview)
- Delivery status
- Timestamps
- Recipient information
- Error messages (if any)

---

## 🔧 Environment Variables Reference

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `RESEND_API_KEY` | Your Resend API key | `re_XyZ123AbC456...` |
| `FRONTEND_URL` | Your Vercel frontend URL | `https://your-app.vercel.app` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `RESEND_FROM_EMAIL` | Sender email address | `onboarding@resend.dev` |

### From Email Options

**For Testing:**
```bash
RESEND_FROM_EMAIL=onboarding@resend.dev
```
✅ Works immediately, no setup needed

**For Production (Recommended):**
```bash
RESEND_FROM_EMAIL=noreply@yourdomain.com
```
⚠️ Requires domain verification in Resend dashboard

---

## 🌐 Using Your Own Domain (Optional)

For a professional email address (`noreply@yourdomain.com`):

### Step 1: Add Domain in Resend

1. Go to **https://resend.com/domains**
2. Click **Add Domain**
3. Enter your domain: `yourdomain.com`

### Step 2: Add DNS Records

Resend will provide DNS records. Add them to your domain provider:

**If using Vercel for your domain:**
1. Go to Vercel dashboard
2. Select your project → Settings → Domains
3. Click on your domain → DNS Records
4. Add the records from Resend

**Common DNS Records:**
- **MX Record**: For receiving emails (optional)
- **TXT Record (SPF)**: Sender authentication
- **TXT Records (DKIM)**: Email signing
- **TXT Record (DMARC)**: Email policy (optional)

### Step 3: Wait for Verification

- Usually takes **5-30 minutes**
- Can take up to 72 hours
- Check status in Resend dashboard

### Step 4: Update Railway Variables

```bash
RESEND_FROM_EMAIL=noreply@yourdomain.com
```

Or use other addresses:
- `hello@yourdomain.com`
- `support@yourdomain.com`
- `team@yourdomain.com`

---

## 🐛 Troubleshooting

### Issue 1: "Invalid API key" Error

**Symptoms:**
- Emails not sending
- Log shows: `Resend error: Invalid API key`

**Solutions:**
1. ✅ Verify the API key in Railway matches the one in Resend dashboard
2. ✅ Check for extra spaces before/after the key
3. ✅ Make sure you didn't accidentally delete the key in Resend
4. ✅ Create a new API key and update Railway
5. ✅ Restart the Railway deployment

### Issue 2: Emails Not Arriving

**Symptoms:**
- No errors in logs
- Email shows as sent in Resend dashboard
- Email not in inbox

**Solutions:**
1. ✅ **Check spam/junk folder**
2. ✅ Verify email address is correct
3. ✅ Check Resend dashboard for delivery status
4. ✅ Try a different email address (Gmail, Outlook, etc.)
5. ✅ Wait 5-10 minutes (sometimes delayed)

### Issue 3: Rate Limit Exceeded

**Symptoms:**
- Log shows: `Resend error: Rate limit exceeded`

**Solutions:**
1. Check Resend usage: https://resend.com/settings/usage
2. Free tier limits:
   - **100 emails per day**
   - **3,000 emails per month**
3. Wait until tomorrow or upgrade plan
4. Implement rate limiting in your signup endpoint

### Issue 4: Application Won't Start

**Symptoms:**
- Railway deployment fails
- Error: `Required a bean of type 'String' that could not be found`

**Solutions:**
1. ✅ Ensure `RESEND_API_KEY` is set in Railway variables
2. ✅ Check for typos in variable name (case-sensitive)
3. ✅ Verify the variable is in the correct service (if you have multiple)
4. ✅ Trigger a manual redeploy

### Issue 5: Wrong Frontend URL in Emails

**Symptoms:**
- Verification link points to wrong domain
- Link shows `http://localhost:3000` instead of production URL

**Solutions:**
1. ✅ Update `FRONTEND_URL` in Railway to your Vercel URL:
   ```bash
   FRONTEND_URL=https://your-app-name.vercel.app
   ```
2. ✅ Make sure there's no trailing slash
3. ✅ Redeploy Railway service
4. ✅ Test with a new signup

---

## ✅ Complete Setup Checklist

Use this checklist to verify everything is configured correctly:

### Resend Setup
- [ ] Signed up for Resend account
- [ ] Created API key in Resend dashboard
- [ ] API key starts with `re_`
- [ ] Old exposed API key has been deleted

### Railway Configuration
- [ ] Added `RESEND_API_KEY` to Railway variables
- [ ] Added `RESEND_FROM_EMAIL` (or using default)
- [ ] Updated `FRONTEND_URL` to Vercel URL
- [ ] Updated `CORS_ALLOWED_ORIGINS` to match frontend
- [ ] Railway deployment completed successfully
- [ ] No errors in deployment logs

### Testing
- [ ] Signed up a test user from frontend
- [ ] Received verification email
- [ ] Email not in spam folder
- [ ] Verification link works
- [ ] Can login after email verification
- [ ] Checked Resend dashboard shows email as delivered
- [ ] Checked Railway logs show success message

### Production Ready (Optional)
- [ ] Added and verified custom domain in Resend
- [ ] Updated `RESEND_FROM_EMAIL` to custom domain
- [ ] Tested with custom domain email
- [ ] Set up email monitoring/alerts
- [ ] Implemented rate limiting on signup

---

## 📚 Additional Resources

### Documentation
- **Resend Setup Guide**: See `RESEND_SETUP.md` in this directory
- **Complete Setup Checklist**: See `SETUP_CHECKLIST.md`
- **Supabase RLS Setup**: See `SUPABASE_RLS_SETUP.md`

### Links
- **Resend Dashboard**: https://resend.com/overview
- **Resend Emails Log**: https://resend.com/emails
- **Resend API Keys**: https://resend.com/api-keys
- **Resend Domains**: https://resend.com/domains
- **Railway Dashboard**: https://railway.app/dashboard

### Support
- **Resend Support**: hello@resend.com
- **Railway Support**: https://railway.app/help

---

## 🎉 You're All Set!

Your Spring Boot backend on Railway is now configured to send emails via Resend!

### What's Working:
✅ User signup creates account  
✅ Verification email sent automatically  
✅ Email arrives in user's inbox  
✅ User clicks verification link  
✅ Email marked as verified  
✅ User can login  

### Next Steps:
1. Test the complete flow end-to-end
2. Monitor email deliverability in Resend dashboard
3. Consider adding your own domain for production
4. Set up error monitoring and alerts

**Happy coding! 🚀**