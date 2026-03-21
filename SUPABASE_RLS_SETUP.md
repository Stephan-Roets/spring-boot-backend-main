# Supabase RLS Setup for Spring Boot Backend

## Understanding the Issue

When you enable RLS (Row Level Security) on tables in Supabase, the warning **"This table can be accessed via the Data API but no RLS policies exist so no data will be returned"** appears because:

1. **RLS is enabled** on your tables (users, todos, verification_tokens)
2. **No policies exist** to allow access to the data
3. **Your Spring Boot backend connects via JDBC**, not Supabase Auth

## Important: RLS vs Backend Architecture

### Supabase RLS is designed for:
- **Client-side applications** that use Supabase Auth
- **Direct browser/mobile access** to Supabase Data API
- **User-specific data access** based on `auth.uid()`

### Your Spring Boot Setup:
- **Backend-controlled access** via JDBC connection
- **Spring Security handles authentication** (JWT tokens)
- **Backend is the trusted layer** (not end users)
- **RLS should be DISABLED** for backend database connections

## Solution: Disable RLS for Backend Access

Since your Spring Boot backend is the trusted service layer and handles all authentication/authorization, you should **disable RLS** on these tables.

### Step 1: Open Supabase SQL Editor

1. Go to your Supabase Dashboard
2. Navigate to **SQL Editor** (left sidebar)
3. Click **New Query**

### Step 2: Run This SQL Script

Copy and paste this entire script into the SQL Editor:

```sql
-- ============================================
-- DISABLE RLS FOR SPRING BOOT BACKEND
-- ============================================
-- This allows your Spring Boot backend to access
-- the database without RLS restrictions.
-- Your Spring Security layer handles authorization.
-- ============================================

-- Disable RLS on all tables
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE todos DISABLE ROW LEVEL SECURITY;
ALTER TABLE verification_tokens DISABLE ROW LEVEL SECURITY;

-- Verify RLS is disabled
SELECT 
    schemaname,
    tablename,
    rowsecurity as "RLS Enabled"
FROM pg_tables
WHERE tablename IN ('users', 'todos', 'verification_tokens')
ORDER BY tablename;
```

### Step 3: Click "Run" or Press `Ctrl/Cmd + Enter`

You should see output like:

| schemaname | tablename            | RLS Enabled |
|------------|---------------------|-------------|
| public     | todos               | false       |
| public     | users               | false       |
| public     | verification_tokens | false       |

✅ **Success!** All tables now show `false` for RLS Enabled.

### Step 4: Verify Access

Test that your backend can now access the data:

```sql
-- Test query to verify access
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM todos;
SELECT COUNT(*) FROM verification_tokens;
```

These should return results without errors.

## Alternative: Keep RLS Enabled (Not Recommended for Your Setup)

If you still want to keep RLS enabled (not recommended for Spring Boot backends), you would need to:

### Option A: Create Policies for Service Role

```sql
-- Re-enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE todos ENABLE ROW LEVEL SECURITY;
ALTER TABLE verification_tokens ENABLE ROW LEVEL SECURITY;

-- Create permissive policies for service role
CREATE POLICY "Allow service role full access to users"
ON users FOR ALL
TO postgres, authenticator
USING (true)
WITH CHECK (true);

CREATE POLICY "Allow service role full access to todos"
ON todos FOR ALL
TO postgres, authenticator
USING (true)
WITH CHECK (true);

CREATE POLICY "Allow service role full access to verification_tokens"
ON verification_tokens FOR ALL
TO postgres, authenticator
USING (true)
WITH CHECK (true);

-- Grant necessary permissions
GRANT ALL ON users TO postgres;
GRANT ALL ON todos TO postgres;
GRANT ALL ON verification_tokens TO postgres;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO postgres;
```

**Note:** This adds unnecessary complexity. Since your Spring Boot backend already handles security, disabling RLS is the simpler and recommended approach.

## Security Considerations

### "Isn't disabling RLS insecure?"

**No, because:**

1. ✅ Your Spring Boot backend uses **Spring Security** with JWT authentication
2. ✅ The database is **not exposed to the internet** directly
3. ✅ All requests go through your **backend API** which enforces authorization
4. ✅ Users cannot access Supabase directly - only your backend can
5. ✅ Your `TodoService` and `AuthService` enforce user ownership:
   ```java
   // Example from your code
   Todo todo = todoRepository.findById(id)
       .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));
   
   if (!todo.getUser().getId().equals(userId)) {
       throw new UnauthorizedException("Not authorized");
   }
   ```

### When to Use RLS:

- ✅ Client-side apps using Supabase Auth (JavaScript, Flutter, etc.)
- ✅ Direct browser access to Supabase Data API
- ✅ Serverless functions that use Supabase Auth context

### When to Disable RLS:

- ✅ **Spring Boot backend** (your case)
- ✅ **Node.js backend** with custom auth
- ✅ **Django/Rails backend** with ORM
- ✅ Any backend service that manages its own authentication

## Architecture Overview

```
┌─────────────────┐
│   Frontend      │
│  (Next.js)      │
└────────┬────────┘
         │ JWT Token in Header
         ▼
┌─────────────────────────┐
│   Spring Boot Backend   │
│   - Spring Security     │◄─── Handles Authentication
│   - JWT Validation      │◄─── Handles Authorization
│   - User Ownership      │◄─── Enforces Data Access
└────────┬────────────────┘
         │ JDBC Connection (postgres user)
         ▼
┌─────────────────────────┐
│   Supabase PostgreSQL   │
│   - RLS: DISABLED       │◄─── Trusts backend connection
│   - Backend has full    │
│     access to data      │
└─────────────────────────┘
```

## Troubleshooting

### Issue: "No data returned" even after disabling RLS

**Solution 1:** Check the database connection user

```sql
-- Run this in Supabase SQL Editor
SELECT current_user, session_user;
```

This should return `postgres` or your configured database user.

**Solution 2:** Verify your Spring Boot connection

Check your `application.properties`:
```properties
spring.datasource.username=${DATABASE_USER:postgres}
```

Make sure `DATABASE_USER` environment variable matches the user with proper grants.

**Solution 3:** Grant explicit permissions

```sql
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
```

### Issue: RLS re-enables after migrations

If you use migration tools (Flyway, Liquibase), add a migration to disable RLS:

```sql
-- V999__disable_rls.sql
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE todos DISABLE ROW LEVEL SECURITY;
ALTER TABLE verification_tokens DISABLE ROW LEVEL SECURITY;
```

## Quick Reference Commands

### Check RLS Status
```sql
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public';
```

### Disable RLS (Recommended)
```sql
ALTER TABLE table_name DISABLE ROW LEVEL SECURITY;
```

### Enable RLS
```sql
ALTER TABLE table_name ENABLE ROW LEVEL SECURITY;
```

### Drop All Policies on a Table
```sql
DROP POLICY IF EXISTS "policy_name" ON table_name;
```

### List All Policies
```sql
SELECT schemaname, tablename, policyname, permissive, roles, cmd
FROM pg_policies
WHERE tablename IN ('users', 'todos', 'verification_tokens');
```

## Summary

For your Spring Boot backend:

1. ✅ **Disable RLS** on users, todos, and verification_tokens tables
2. ✅ **Let Spring Security handle authentication** (JWT tokens)
3. ✅ **Let your service layer handle authorization** (user ownership checks)
4. ✅ **Keep the database connection trusted** (backend-only access)

This is the standard approach for backend-driven applications and is **secure** when properly implemented.

---

## Next Steps

After disabling RLS:

1. ✅ Verify your Spring Boot app can read/write data
2. ✅ Test user signup and email verification
3. ✅ Test todo CRUD operations
4. ✅ Check Railway logs for any database errors
5. ✅ Test the complete authentication flow

Need help with SMTP setup? Check `SMTP_SETUP.md` in this directory.