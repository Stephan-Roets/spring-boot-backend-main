# SMTP Setup Guide for Email Verification

This guide will help you configure SMTP for sending email verification emails in your Spring Boot Todo App.

## Table of Contents
1. [Gmail SMTP Setup (Recommended for Testing)](#gmail-smtp-setup)
2. [Alternative SMTP Providers](#alternative-smtp-providers)
3. [Railway Environment Variables](#railway-environment-variables)
4. [Testing Email Functionality](#testing-email-functionality)
5. [Troubleshooting](#troubleshooting)

---

## Gmail SMTP Setup (Recommended for Testing)

### Step 1: Create a Google App Password

Since May 30, 2022, Google no longer allows "less secure apps" to access Gmail. You **must** use an App Password.

1. **Enable 2-Factor Authentication** on your Google Account:
   - Go to https://myaccount.google.com/security
   - Under "How you sign in to Google", enable **2-Step Verification**
   - Follow the prompts to set it up (you'll need your phone)

2. **Generate an App Password**:
   - Go to https://myaccount.google.com/apppasswords
   - Sign in if prompted
   - Under "App name", type: `Todo App Backend` (or any name you prefer)
   - Click **Create**
   - Google will generate a 16-character password like: `abcd efgh ijkl mnop`
   - **Copy this password** (remove spaces when using it)

3. **Important Notes**:
   - The app password is a 16-character code (ignore the spaces)
   - You can only see this password once - copy it immediately
   - This password is specific to the app and doesn't affect your Google account password
   - You can revoke it anytime from the same page

### Step 2: Update Your Configuration

#### Local Development (application.properties)

Update your `src/main/resources/application.properties`:

```properties
# SMTP Configuration for Gmail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=abcdefghijklmnop  # Your 16-char app password (no spaces)
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Frontend URL for verification links
app.frontend.url=http://localhost:3000
```

**⚠️ SECURITY WARNING:** 
- **Never commit actual passwords to Git!**
- Use environment variables instead (see below)
- Add `application-local.properties` to your `.gitignore`

#### Using Environment Variables (Recommended)

Create a file `application-local.properties` (add to `.gitignore`):

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=abcdefghijklmnop
```

Or set environment variables in your IDE:
```
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=abcdefghijklmnop
```

---

## Alternative SMTP Providers

### Option 1: SendGrid (Recommended for Production)

**Free Tier:** 100 emails/day forever

1. Sign up at https://sendgrid.com
2. Create an API Key in Settings > API Keys
3. Configure:

```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_SENDGRID_API_KEY
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Option 2: Mailgun

**Free Tier:** 5,000 emails/month for 3 months

1. Sign up at https://www.mailgun.com
2. Verify your domain or use sandbox
3. Get SMTP credentials from Sending > Domain Settings > SMTP

```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=postmaster@your-domain.mailgun.org
spring.mail.password=YOUR_MAILGUN_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Option 3: Amazon SES

**Free Tier:** 62,000 emails/month (when hosted on AWS)

1. Sign up for AWS SES
2. Verify your email address
3. Create SMTP credentials in SES Console

```properties
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=YOUR_AWS_ACCESS_KEY
spring.mail.password=YOUR_AWS_SECRET_KEY
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Option 4: Resend (Modern Alternative)

**Free Tier:** 3,000 emails/month, 100 emails/day

1. Sign up at https://resend.com
2. Get API key
3. Use their REST API instead of SMTP (more modern)

---

## Railway Environment Variables

### Step 1: Access Railway Dashboard

1. Go to https://railway.app
2. Select your `spring-boot-backend` project
3. Click on the service
4. Go to **Variables** tab

### Step 2: Add Environment Variables

Add the following variables (use the **RAW Editor** for easier copy-paste):

```bash
# SMTP Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-16-char-app-password

# Frontend URL (your Vercel deployment)
FRONTEND_URL=https://your-app-name.vercel.app

# Other existing variables (keep these)
DATABASE_URL=your-supabase-connection-string
DATABASE_USER=postgres
DATABASE_PASSWORD=your-db-password
JWT_SECRET=your-jwt-secret
CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app
```

### Step 3: Deploy

Railway will automatically redeploy your app with the new environment variables.

---

## Testing Email Functionality

### Test 1: Local Testing

1. Start your Spring Boot application locally
2. Use Postman or curl to signup:

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!",
    "name": "Test User"
  }'
```

3. Check your email inbox for the verification email
4. Check application logs for any SMTP errors

### Test 2: Check Application Logs

Look for these log messages:

✅ **Success:**
```
INFO  c.t.service.EmailService - Verification email sent to test@example.com
```

❌ **Failure:**
```
WARN  c.t.service.EmailService - Failed to send verification email to test@example.com — SMTP error: ...
```

### Test 3: Verify Email Flow

1. Signup creates account and sends email ✓
2. Email contains verification link ✓
3. Click link redirects to frontend `/verify-email?token=...` ✓
4. Frontend calls backend `/api/auth/verify-email` ✓
5. User can login after verification ✓

---

## Troubleshooting

### Issue 1: "Authentication failed" Error

**Symptom:**
```
SMTP error: AuthenticationFailedException
```

**Solutions:**
- Ensure 2-Factor Authentication is enabled on your Google account
- Use an App Password, not your regular Gmail password
- Remove any spaces from the app password
- Double-check the username is your full email address
- Make sure "Less Secure Apps" is NOT enabled (it's deprecated)

### Issue 2: Emails Not Sending (No Errors)

**Symptom:** No error logs, but emails don't arrive

**Solutions:**
- Check your spam/junk folder
- Verify `spring.mail.username` is correct
- Check that `@Async` is enabled - add `@EnableAsync` to main application class:

```java
@SpringBootApplication
@EnableAsync  // Add this!
public class TodoAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoAppApplication.class, args);
    }
}
```

- Gmail may block emails initially - check your Google account for security alerts

### Issue 3: "Connection timeout" Error

**Symptom:**
```
SMTP error: Could not connect to SMTP host
```

**Solutions:**
- Check your firewall/antivirus isn't blocking port 587
- Verify Railway has network access to smtp.gmail.com
- Try port 465 with SSL instead:

```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.starttls.enable=false
```

### Issue 4: Rate Limiting

**Symptom:** First few emails work, then fail

**Gmail Limits:**
- 500 emails per day for regular accounts
- 2,000 emails per day for Google Workspace accounts
- 100-150 emails per hour

**Solutions:**
- Use a dedicated email service (SendGrid, Mailgun, etc.) for production
- Implement rate limiting in your backend
- Add email queuing for better reliability

### Issue 5: "From" Address Doesn't Match

**Symptom:** Email sent but from address is wrong

**Solution:** Add a from address:

```java
helper.setFrom("noreply@yourdomain.com", "Todo App");
```

Or in properties:
```properties
spring.mail.properties.mail.smtp.from=noreply@yourdomain.com
```

---

## Best Practices

### 1. Use Environment Variables
Never hardcode credentials:
```properties
# ❌ Bad
spring.mail.password=abcdefghijklmnop

# ✅ Good
spring.mail.password=${SMTP_PASSWORD}
```

### 2. Add Email Templates
Consider using Thymeleaf for better email templates:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 3. Add Email Retry Logic
Enhance `EmailService` with retry:

```java
@Retryable(
    value = {MessagingException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000)
)
public void sendVerificationEmail(...) {
    // ... existing code
}
```

### 4. Monitor Email Deliverability
- Check bounce rates
- Monitor spam complaints
- Use proper SPF/DKIM/DMARC records for your domain
- Include unsubscribe links (if sending marketing emails)

### 5. Production Checklist
- [ ] Use a dedicated email service (not Gmail)
- [ ] Set up custom domain for emails
- [ ] Configure SPF, DKIM, and DMARC records
- [ ] Implement email templates
- [ ] Add retry and error handling
- [ ] Monitor delivery rates
- [ ] Set up email logging/tracking
- [ ] Implement rate limiting
- [ ] Add email queue for high volume

---

## Quick Reference

### Gmail SMTP Settings
```
Host: smtp.gmail.com
Port: 587 (TLS) or 465 (SSL)
Username: your-email@gmail.com
Password: 16-character app password
TLS: Required
```

### Testing Commands

**Check if SMTP is reachable:**
```bash
telnet smtp.gmail.com 587
```

**Test with openssl:**
```bash
openssl s_client -starttls smtp -connect smtp.gmail.com:587
```

---

## Additional Resources

- [Gmail SMTP Settings](https://support.google.com/mail/answer/7126229)
- [Spring Boot Email Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- [JavaMailSender API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/mail/javamail/JavaMailSender.html)
- [SendGrid Java Guide](https://docs.sendgrid.com/for-developers/sending-email/v3-java-code-example)

---

## Need Help?

If you're still having issues:
1. Check the application logs in Railway
2. Test locally first before deploying
3. Verify all environment variables are set
4. Try a different SMTP provider
5. Check Gmail security settings and recent activity

Good luck! 🚀