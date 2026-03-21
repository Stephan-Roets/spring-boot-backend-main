# Resend API Setup Guide

## 🎯 Quick Start: Get Resend Working in 5 Minutes

Resend is a modern email API built for developers. It's easier to set up than traditional SMTP and has excellent deliverability.

**Free Tier:**
- 3,000 emails/month
- 100 emails/day
- No credit card required

---

## 📋 Table of Contents

1. [Why Resend?](#why-resend)
2. [Getting Your API Key](#getting-your-api-key)
3. [Local Development Setup](#local-development-setup)
4. [Railway Production Setup](#railway-production-setup)
5. [Domain Verification (Optional)](#domain-verification-optional)
6. [Testing Email Sending](#testing-email-sending)
7. [Troubleshooting](#troubleshooting)
8. [Best Practices](#best-practices)

---

## Why Resend?

### Advantages over Gmail SMTP:

✅ **No 2FA or App Passwords Required** - Just an API key  
✅ **Better Deliverability** - Professional email infrastructure  
✅ **Higher Rate Limits** - 3,000 emails/month vs Gmail's 500/day  
✅ **Simple API** - No complex SMTP configuration  
✅ **Developer-Friendly** - Built for modern applications  
✅ **Free Tier** - Perfect for small projects  

### Comparison with Alternatives:

| Provider | Free Tier | Setup Difficulty | Deliverability |
|----------|-----------|------------------|----------------|
| **Resend** | 3,000/month | ⭐ Easy | ⭐⭐⭐ Excellent |
| Gmail SMTP | 500/day | ⭐⭐ Medium | ⭐⭐ Good |
| SendGrid | 100/day | ⭐⭐ Medium | ⭐⭐⭐ Excellent |
| Mailgun | 5,000/month* | ⭐⭐⭐ Hard | ⭐⭐⭐ Excellent |

*Free for 3 months only

---

## Getting Your API Key

### Step 1: Sign Up for Resend

1. Go to **https://resend.com**
2. Click **Start Building** or **Sign Up**
3. Sign up with:
   - GitHub (recommended - fastest)
   - Google
   - Email

### Step 2: Create an API Key

1. After signing up, you'll be in the **Resend Dashboard**
2. Click **API Keys** in the left sidebar
3. Click **Create API Key** button
4. Configure your API key:
   - **Name:** `Todo App Backend` (or any descriptive name)
   - **Permission:** `Sending access` (default)
   - **Domain:** Leave as `All domains` for now
5. Click **Add**

### Step 3: Copy Your API Key

⚠️ **IMPORTANT:** You'll only see the API key once!

Your API key will look like:
```
re_123abc456def789ghi012jkl345mno678
```

**Copy it immediately** and save it somewhere safe (you'll use it in the next steps).

If you lose it, you'll need to create a new one.

---

## Local Development Setup

### Option 1: Using `application-local.properties` (Recommended)

1. **Copy the template file:**

```bash
cd src/main/resources
cp application-local.properties.template application-local.properties
```

2. **Edit `application-local.properties`:**

```properties
# Database
DATABASE_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres
DATABASE_USER=postgres
DATABASE_PASSWORD=your-supabase-password

# JWT
JWT_SECRET=your-jwt-secret-key

# Resend API
RESEND_API_KEY=re_123abc456def789ghi012jkl345mno678
RESEND_FROM_EMAIL=onboarding@resend.dev

# Frontend
FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

3. **Replace values:**
   - `RESEND_API_KEY`: Your actual Resend API key from Step 2
   - Keep `RESEND_FROM_EMAIL=onboarding@resend.dev` for testing

### Option 2: Using Environment Variables

Set environment variables in your IDE or terminal:

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. Select your Spring Boot application
3. Environment Variables → Add:
   ```
   RESEND_API_KEY=re_123abc456def789ghi012jkl345mno678
   RESEND_FROM_EMAIL=onboarding@resend.dev
   ```

**Terminal (macOS/Linux):**
```bash
export RESEND_API_KEY=re_123abc456def789ghi012jkl345mno678
export RESEND_FROM_EMAIL=onboarding@resend.dev
./mvnw spring-boot:run
```

**Windows (PowerShell):**
```powershell
$env:RESEND_API_KEY="re_123abc456def789ghi012jkl345mno678"
$env:RESEND_FROM_EMAIL="onboarding@resend.dev"
./mvnw spring-boot:run
```

---

## Railway Production Setup

### Step 1: Open Railway Dashboard

1. Go to **https://railway.app/dashboard**
2. Select your **spring-boot-backend** project
3. Click on your service
4. Navigate to **Variables** tab

### Step 2: Add Environment Variables

Click **RAW Editor** and add/update:

```bash
# Database (should already exist)
DATABASE_URL=your-supabase-connection-string
DATABASE_USER=postgres
DATABASE_PASSWORD=your-supabase-password

# JWT
JWT_SECRET=your-production-jwt-secret

# Resend API
RESEND_API_KEY=re_123abc456def789ghi012jkl345mno678
RESEND_FROM_EMAIL=onboarding@resend.dev

# Frontend
FRONTEND_URL=https://your-app-name.vercel.app
CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app
```

### Step 3: Deploy

- Railway will **automatically redeploy** when you save the variables
- Wait for the deployment to complete (check the **Deployments** tab)
- Verify the deployment is successful

---

## Domain Verification (Optional)

Using `onboarding@resend.dev` is perfect for **testing**, but for **production**, you should use your own domain.

### Benefits of Your Own Domain:

✅ Professional appearance (`noreply@yourdomain.com`)  
✅ Better email deliverability  
✅ Custom branding  
✅ Reduced spam risk  

### Steps to Add Your Domain:

1. **In Resend Dashboard**, click **Domains** in sidebar
2. Click **Add Domain**
3. Enter your domain: `yourdomain.com`
4. Resend will provide DNS records to add:
   - SPF record
   - DKIM records
   - DMARC record (optional but recommended)

5. **Add DNS records** to your domain provider:
   - If using **Vercel**: Add records in your domain settings
   - If using **Cloudflare**: Add in DNS management
   - If using **GoDaddy/Namecheap**: Add in DNS/Zone File editor

6. **Wait for verification** (can take up to 72 hours, usually < 1 hour)

7. **Update your config:**
   ```bash
   RESEND_FROM_EMAIL=noreply@yourdomain.com
   ```

### Common From Email Addresses:

- `noreply@yourdomain.com` - Standard for transactional emails
- `hello@yourdomain.com` - Friendly, encourages replies
- `support@yourdomain.com` - For support-related emails
- `team@yourdomain.com` - Personal touch

---

## Testing Email Sending

### Test 1: Local Development

1. **Start your Spring Boot application:**

```bash
./mvnw spring-boot:run
```

2. **Check the startup logs** for any Resend-related errors

3. **Sign up a new user** using Postman, curl, or your frontend:

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "stefanroetsprograming@gmail.com",
    "password": "Test1234!",
    "name": "Stefan Roets"
  }'
```

4. **Expected Response:**

```json
{
  "id": "uuid-here",
  "email": "stefanroetsprograming@gmail.com",
  "name": "Stefan Roets",
  "emailVerified": false,
  ...
}
```

5. **Check application logs:**

✅ **Success:**
```
INFO c.t.service.EmailService - Verification email sent to stefanroetsprograming@gmail.com — Resend ID: abc123def456
```

❌ **Failure:**
```
WARN c.t.service.EmailService - Failed to send verification email to ... — Resend error: ...
```

### Test 2: Check Your Email Inbox

1. Open your email inbox (check spam folder too!)
2. You should see an email from `onboarding@resend.dev`
3. Subject: **"Verify your email - ToDo App"**
4. Click the **"Verify Email Address"** button
5. You should be redirected to your frontend

### Test 3: Verify Email Flow

1. Click the verification link
2. You're redirected to: `http://localhost:3000/verify-email?token=...`
3. Frontend calls backend: `GET /api/auth/verify-email?token=...`
4. Response: `"Email verified successfully. You can now log in."`

### Test 4: Login with Verified Account

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "stefanroetsprograming@gmail.com",
    "password": "Test1234!"
  }'
```

✅ **Should return:** Access token and user data

### Test 5: Production Testing

1. Go to your **Vercel frontend**: `https://your-app-name.vercel.app`
2. Sign up with a real email address
3. Check your inbox
4. Click verification link
5. Login successfully
6. Create a todo to verify everything works

---

## Troubleshooting

### Issue 1: "Invalid API key" Error

**Error Message:**
```
Resend error: Invalid API key
```

**Solutions:**

- ✅ Verify you copied the full API key (starts with `re_`)
- ✅ Check for extra spaces before/after the API key
- ✅ Ensure the API key is set in the correct environment variable: `RESEND_API_KEY`
- ✅ If you lost the key, create a new one in Resend dashboard
- ✅ Restart your Spring Boot application after updating the key

### Issue 2: "Emails not sending" (No errors)

**Symptoms:** No errors in logs, but emails don't arrive

**Solutions:**

1. **Check Resend Dashboard:**
   - Go to https://resend.com/emails
   - Check if emails appear in the logs
   - Look for delivery status (Delivered, Bounced, etc.)

2. **Verify API key permissions:**
   - Make sure the API key has `Sending access`
   - Check it's not restricted to a specific domain

3. **Check email address:**
   - Ensure the recipient email is valid
   - Try sending to a different email address
   - Check spam/junk folder

4. **Check free tier limits:**
   - 100 emails per day
   - 3,000 emails per month
   - Check usage in Resend dashboard

### Issue 3: "Rate limit exceeded"

**Error Message:**
```
Resend error: Rate limit exceeded
```

**Solutions:**

- Check your usage in Resend dashboard
- Free tier limits:
  - 100 emails/day
  - 3,000 emails/month
- Consider upgrading to a paid plan if needed
- Implement rate limiting in your signup endpoint

### Issue 4: Emails going to spam

**Solutions:**

1. **Verify your domain** (see Domain Verification section)
2. **Set up SPF, DKIM, and DMARC** records
3. **Use a custom from email** (not onboarding@resend.dev)
4. **Include an unsubscribe link** (if sending marketing emails)
5. **Avoid spam trigger words** in subject/body
6. **Check email content:**
   - Include both HTML and plain text versions
   - Avoid too many links
   - Have a good text-to-image ratio

### Issue 5: "Domain not verified"

**Error Message:**
```
Resend error: Domain not verified
```

**Solutions:**

- If using `onboarding@resend.dev`: This should work without verification
- If using your own domain:
  - Check DNS records are added correctly
  - Wait for DNS propagation (up to 72 hours)
  - Verify in Resend dashboard under **Domains**
  - Try using `onboarding@resend.dev` temporarily

### Issue 6: Application won't start

**Error Message:**
```
Error creating bean with name 'emailService'
Parameter 0 of constructor in com.todoapp.service.EmailService required a bean...
```

**Solutions:**

- Ensure `RESEND_API_KEY` environment variable is set
- Check for typos in property name: `resend.api.key`
- If using `application-local.properties`, make sure it exists
- Try setting a default in `application.properties`:
  ```properties
  resend.api.key=${RESEND_API_KEY:re_test_key_placeholder}
  ```

---

## Best Practices

### 1. Security

✅ **Never hardcode API keys** in your code  
✅ **Use environment variables** for all sensitive data  
✅ **Add `application-local.properties` to `.gitignore`**  
✅ **Rotate API keys** periodically  
✅ **Use different API keys** for development and production  

### 2. Error Handling

✅ **Log all email send attempts** (success and failure)  
✅ **Don't fail user registration** if email fails to send  
✅ **Implement retry logic** for failed emails  
✅ **Monitor email delivery rates** in Resend dashboard  

### 3. Email Content

✅ **Include both HTML and plain text** versions  
✅ **Make emails mobile-responsive**  
✅ **Include your company name** and contact info  
✅ **Add an unsubscribe link** (for marketing emails)  
✅ **Keep subject lines** under 50 characters  
✅ **Test emails** on multiple email clients  

### 4. Rate Limiting

✅ **Implement rate limiting** on signup endpoint  
✅ **Monitor daily/monthly usage** in Resend dashboard  
✅ **Set up alerts** for high usage  
✅ **Consider upgrading** if you exceed free tier  

### 5. Production Checklist

- [ ] Use your own verified domain
- [ ] Set up SPF, DKIM, and DMARC records
- [ ] Use a professional from email address
- [ ] Monitor email delivery rates
- [ ] Set up error alerts
- [ ] Test the complete email flow
- [ ] Check spam folder placement
- [ ] Implement retry logic
- [ ] Add email templates
- [ ] Monitor API usage

---

## Advanced Configuration

### Adding Email Templates

For better email management, consider using Resend's template feature:

```java
// Future enhancement - use Resend templates
SendEmailRequest emailRequest = SendEmailRequest.builder()
    .from(fromEmail)
    .to(to)
    .subject(subject)
    .template("verification-email") // Template name
    .templateData(Map.of(
        "name", name,
        "verifyUrl", verifyUrl
    ))
    .build();
```

### Tracking Email Opens

Enable email tracking in Resend dashboard:

1. Go to **Settings** → **Email Tracking**
2. Enable **Open Tracking**
3. Enable **Click Tracking**
4. Resend will automatically track opens and clicks

### Webhooks (Optional)

Set up webhooks to receive real-time notifications:

1. Go to **Webhooks** in Resend dashboard
2. Add your webhook endpoint: `https://your-backend.railway.app/api/webhooks/resend`
3. Subscribe to events: `email.delivered`, `email.bounced`, etc.
4. Implement webhook handler in your Spring Boot app

---

## Monitoring & Analytics

### Resend Dashboard

Monitor your emails at: **https://resend.com/emails**

You can see:
- ✅ Total emails sent
- ✅ Delivery rate
- ✅ Bounce rate
- ✅ Click-through rate (if tracking enabled)
- ✅ Individual email status
- ✅ Error logs

### Usage Limits

Check your usage at: **https://resend.com/settings/usage**

Free tier limits:
- 3,000 emails/month
- 100 emails/day
- Unlimited domains

### Upgrading

If you need more emails:

| Plan | Price | Emails/Month | Daily Limit |
|------|-------|--------------|-------------|
| Free | $0 | 3,000 | 100 |
| Pro | $20/mo | 50,000 | 2,000 |
| Business | Contact | Custom | Custom |

---

## Quick Reference

### Environment Variables

```bash
# Required
RESEND_API_KEY=re_xxxxxxxxx

# Optional (defaults provided)
RESEND_FROM_EMAIL=onboarding@resend.dev
```

### API Key Format

```
re_[32 alphanumeric characters]
```

Example: `re_123abc456def789ghi012jkl345mno678`

### Testing Commands

**Signup:**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234!","name":"Test User"}'
```

**Check Resend Dashboard:**
```
https://resend.com/emails
```

---

## Resources

- **Resend Website:** https://resend.com
- **Resend Dashboard:** https://resend.com/overview
- **API Documentation:** https://resend.com/docs
- **Java SDK:** https://github.com/resend/resend-java
- **Status Page:** https://status.resend.com
- **Support:** hello@resend.com

---

## Summary

**To get started with Resend:**

1. ✅ Sign up at https://resend.com
2. ✅ Create an API key
3. ✅ Set `RESEND_API_KEY` environment variable
4. ✅ Test locally with signup
5. ✅ Deploy to Railway with environment variables
6. ✅ (Optional) Verify your domain for production

**Resend is now configured!** Your Spring Boot app will send beautiful verification emails with excellent deliverability. 🚀

---

**Need Help?**

- Check the Resend dashboard for email logs
- Review application logs for errors
- Test with different email addresses
- Verify API key is set correctly
- Contact Resend support: hello@resend.com

Good luck! 📧