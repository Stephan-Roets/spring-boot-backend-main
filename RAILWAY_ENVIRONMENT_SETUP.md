# Railway Environment Variables Setup

## Required Environment Variables

Set these in your Railway project dashboard (Settings → Variables):

### 1. RESEND_API_KEY
```
re_9a5UMfAL_8FMjqzpbHetLrTxeerHfGtQj
```

### 2. FRONTEND_URL
```
http://localhost:3000
```
**Note:** Change this to your production frontend URL when deploying (e.g., `https://your-app.vercel.app`)

### 3. RESEND_FROM_EMAIL
```
onboarding@resend.dev
```
**Note:** This is Resend's test email. For production, you should:
- Add and verify your own domain in Resend dashboard
- Use your custom email like `noreply@yourdomain.com`

### 4. DATABASE_URL (should already be set)
Your Supabase PostgreSQL connection string

### 5. DATABASE_USER (should already be set)
Your database username

### 6. DATABASE_PASSWORD (should already be set)
Your database password

### 7. JWT_SECRET (should already be set)
Your JWT secret key for token generation

### 8. CORS_ALLOWED_ORIGINS
```
http://localhost:3000,https://your-app.vercel.app
```
**Note:** Add all frontend URLs that need to access your API

## How to Set Environment Variables in Railway

1. Go to https://railway.app/
2. Select your project: `spring-boot-backend-main-production`
3. Click on your service
4. Go to **Variables** tab
5. Click **+ New Variable**
6. Add each variable name and value
7. Click **Deploy** to restart with new variables

## Verification

After setting variables, your backend will:
- ✅ Send verification emails via Resend
- ✅ Set `email_verified` to `true` when users click the link
- ✅ Allow users to login after email verification

## Testing

1. Sign up with a new email address
2. Check your inbox for verification email
3. Click the "Verify Email Address" button
4. You should be redirected to login page
5. Login with your credentials
