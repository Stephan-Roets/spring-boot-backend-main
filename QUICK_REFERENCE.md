# Quick Reference Card - Environment Variables

## 🚀 Railway Backend Environment Variables

Copy this into Railway → Variables → RAW Editor:

```bash
# Database
DATABASE_URL=jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:5432/postgres
DATABASE_USER=postgres
DATABASE_PASSWORD=your-supabase-password

# JWT
JWT_SECRET=your-256-bit-secret-key-minimum

# Resend Email
RESEND_API_KEY=re_YourActualResendAPIKey
RESEND_FROM_EMAIL=onboarding@resend.dev

# Frontend
FRONTEND_URL=https://your-app-name.vercel.app
CORS_ALLOWED_ORIGINS=https://your-app-name.vercel.app
```

**⚠️ Important:**
- Replace ALL placeholder values with actual values
- NO trailing slashes on URLs
- `RESEND_API_KEY` must start with `re_`
- `JWT_SECRET` minimum 256 bits (32 characters)

---

## 🌐 Vercel Frontend Environment Variables

Add in Vercel → Settings → Environment Variables:

| Variable | Value | Environment |
|----------|-------|-------------|
| `NEXT_PUBLIC_API_URL` | `https://your-backend.railway.app` | Production |
| `NEXT_PUBLIC_APP_URL` | `https://your-app-name.vercel.app` | Production |

**⚠️ Important:**
- Variables MUST start with `NEXT_PUBLIC_` to be accessible in browser
- NO trailing slashes on URLs
- Set for **Production** environment

---

## 💻 Local Development (application-local.properties)

Create `src/main/resources/application-local.properties`:

```properties
# Database
DATABASE_URL=jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:5432/postgres
DATABASE_USER=postgres
DATABASE_PASSWORD=your-supabase-password

# JWT
JWT_SECRET=local-development-secret-key-change-me

# Resend
RESEND_API_KEY=re_YourActualResendAPIKey
RESEND_FROM_EMAIL=onboarding@resend.dev

# Local URLs
FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

**⚠️ Important:**
- File is in `.gitignore` - safe to commit template only
- Use same Resend key as production or create separate one
- Can use same Supabase database or local PostgreSQL

---

## 📋 Where to Get Values

| Variable | Where to Find It |
|----------|------------------|
| `DATABASE_URL` | Supabase → Settings → Database → Connection String (JDBC) |
| `DATABASE_PASSWORD` | Your Supabase database password (set during project creation) |
| `JWT_SECRET` | Generate with: `openssl rand -base64 32` |
| `RESEND_API_KEY` | Resend → API Keys → Create API Key |
| `FRONTEND_URL` | Your Vercel deployment URL |
| Railway Backend URL | Railway → Service → Settings → Public Networking |

---

## ✅ Quick Validation Checklist

### Before Deployment:

- [ ] All variables set (no `your-xxx-here` placeholders)
- [ ] URLs have NO trailing slashes
- [ ] `RESEND_API_KEY` starts with `re_`
- [ ] `FRONTEND_URL` matches `CORS_ALLOWED_ORIGINS`
- [ ] `NEXT_PUBLIC_API_URL` points to Railway backend
- [ ] `JWT_SECRET` is at least 32 characters

### After Deployment:

- [ ] Railway deployment successful
- [ ] Vercel deployment successful
- [ ] Backend health check: `curl https://your-backend.railway.app/actuator/health`
- [ ] Can sign up from frontend
- [ ] Verification email received
- [ ] Can login after verification

---

## 🐛 Common Mistakes

### ❌ Wrong:
```bash
FRONTEND_URL=https://your-app.vercel.app/  # Trailing slash!
CORS_ALLOWED_ORIGINS=http://localhost:3000  # Wrong protocol for production
RESEND_API_KEY=your-api-key  # Not actual API key
NEXT_PUBLIC_API_URL=https://your-backend.railway.app/api  # Don't include /api
```

### ✅ Correct:
```bash
FRONTEND_URL=https://your-app.vercel.app
CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
RESEND_API_KEY=re_XyZ123AbC456DeF789GhI012JkL345
NEXT_PUBLIC_API_URL=https://your-backend.railway.app
```

---

## 🔗 Quick Links

| Service | Dashboard |
|---------|-----------|
| Supabase | https://supabase.com/dashboard |
| Resend | https://resend.com/overview |
| Railway | https://railway.app/dashboard |
| Vercel | https://vercel.com/dashboard |

---

## 📞 Support

- **Detailed Docs:** See `COMPLETE_SETUP_GUIDE.md`
- **RLS Setup:** See `SUPABASE_RLS_SETUP.md`
- **Email Setup:** See `RESEND_SETUP.md`
- **Debugging:** See `supabase-debug-queries.sql`
