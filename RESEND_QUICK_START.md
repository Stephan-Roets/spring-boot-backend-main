# Resend Quick Start - 5 Minute Setup

## 🚀 Get Resend Working in 5 Minutes

### Step 1: Get Your API Key (2 minutes)

1. Go to **https://resend.com** and sign up (GitHub login is fastest)
2. Click **API Keys** → **Create API Key**
3. Name: `Todo App Backend`
4. Click **Add**
5. **Copy your API key** (looks like: `re_123abc456def789ghi012jkl345mno678`)

⚠️ **You'll only see this once!** Save it immediately.

---

### Step 2: Local Development (2 minutes)

**Option A:** Create `src/main/resources/application-local.properties`:

```properties
# Replace re_xxxxxxxxx with your actual API key
RESEND_API_KEY=re_xxxxxxxxx
RESEND_FROM_EMAIL=onboarding@resend.dev
FRONTEND_URL=http://localhost:3000
```

**Option B:** Set environment variable in your IDE:

```
RESEND_API_KEY=re_123abc456def789ghi012jkl345mno678
```

---

### Step 3: Deploy to Railway (1 minute)

1. Go to **Railway Dashboard** → Your Project → **Variables**
2. Add:
```
RESEND_API_KEY=re_123abc456def789ghi012jkl345mno678
RESEND_FROM_EMAIL=onboarding@resend.dev
```
3. Save (auto-deploys)

---

### Step 4: Test It! (1 minute)

```bash
# Sign up a new user
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!",
    "name": "Test User"
  }'
```

**Check:**
- ✅ Email arrives in inbox (might be in spam first time)
- ✅ Logs show: `Verification email sent to ... — Resend ID: abc123`
- ✅ Click verification link works
- ✅ Can login after verification

---

## ✅ Done!

Your app is now sending emails with Resend!

**Free Tier:**
- 3,000 emails/month
- 100 emails/day
- No credit card required

---

## 🐛 Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| "Invalid API key" | Check you copied the full key (starts with `re_`) |
| No emails arriving | Check spam folder, verify API key is set |
| Rate limit error | Free tier: 100/day, 3,000/month - check usage |
| Application won't start | Ensure `RESEND_API_KEY` environment variable is set |

---

## 📚 Need More Details?

See **RESEND_SETUP.md** for:
- Domain verification (production)
- Email templates
- Monitoring & analytics
- Advanced configuration

---

## 🔗 Quick Links

- **Resend Dashboard:** https://resend.com/overview
- **Email Logs:** https://resend.com/emails
- **API Keys:** https://resend.com/api-keys
- **Documentation:** https://resend.com/docs

---

## ⚙️ What Changed in Your Code?

### 1. Added Dependency (pom.xml)
```xml
<dependency>
    <groupId>com.resend</groupId>
    <artifactId>resend-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 2. Updated EmailService
- Replaced `JavaMailSender` with `Resend` client
- Using Resend API instead of SMTP
- Simpler, more reliable email sending

### 3. Updated Configuration
```properties
# Old (SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.username=...
spring.mail.password=...

# New (Resend)
resend.api.key=re_xxxxxxxxx
resend.from.email=onboarding@resend.dev
```

---

## 💡 Pro Tips

1. **Testing:** Use `onboarding@resend.dev` as from email
2. **Production:** Verify your own domain for better deliverability
3. **Monitoring:** Check email logs in Resend dashboard
4. **Security:** Never commit API keys - use environment variables
5. **Limits:** Free tier is generous (3,000/month) but monitor usage

---

**That's it!** You're now using Resend for reliable email delivery. 🎉