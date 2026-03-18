-- ============================================
-- Fix Duplicate Users Table RLS Issue
-- ============================================
-- This script identifies and fixes the issue where multiple 'users' tables
-- exist in different schemas, causing confusion with RLS settings.
-- ============================================

-- ============================================
-- STEP 1: Identify All Users Tables
-- ============================================
-- This shows ALL users tables across all schemas
SELECT
    schemaname as "Schema",
    tablename as "Table Name",
    CASE
        WHEN rowsecurity THEN 'ENABLED ⚠️'
        ELSE 'DISABLED ✅'
    END as "RLS Status",
    tableowner as "Owner"
FROM pg_tables
WHERE tablename = 'users'
ORDER BY schemaname;

-- Expected Result:
-- You likely see:
-- 1. auth.users (Supabase internal - RLS ENABLED) - DON'T TOUCH THIS
-- 2. public.users (Your Spring Boot app - should be DISABLED)

-- ============================================
-- STEP 2: Verify Which Schema Your App Uses
-- ============================================
-- Check what schema your Spring Boot app is currently using
SELECT current_schema() as "Current Schema";

-- Expected Result: Should be 'public'

-- ============================================
-- STEP 3: Check Your Application's Users Table
-- ============================================
-- Verify the structure of YOUR users table (in public schema)
SELECT
    column_name as "Column",
    data_type as "Type",
    is_nullable as "Nullable"
FROM information_schema.columns
WHERE table_schema = 'public'
    AND table_name = 'users'
ORDER BY ordinal_position;

-- Expected columns: id, email, password_hash, name, phone, address,
-- department, role, profile_picture_url, bio, email_verified, created_at, updated_at

-- ============================================
-- STEP 4: Disable RLS on PUBLIC.USERS Only
-- ============================================
-- This disables RLS ONLY on your application's users table
-- (NOT on Supabase's internal auth.users table)

ALTER TABLE public.users DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.todos DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.verification_tokens DISABLE ROW LEVEL SECURITY;

-- ============================================
-- STEP 5: Verify RLS Is Disabled Correctly
-- ============================================
-- Check RLS status on YOUR tables (public schema only)
SELECT
    schemaname as "Schema",
    tablename as "Table Name",
    CASE
        WHEN rowsecurity THEN 'ENABLED ⚠️'
        ELSE 'DISABLED ✅'
    END as "RLS Status"
FROM pg_tables
WHERE schemaname = 'public'
    AND tablename IN ('users', 'todos', 'verification_tokens')
ORDER BY tablename;

-- Expected Result:
-- All three tables in 'public' schema should show 'DISABLED ✅'

-- ============================================
-- STEP 6: Verify Supabase Auth Schema Is Untouched
-- ============================================
-- Check that Supabase's internal auth.users table still has RLS enabled
-- (This is IMPORTANT - don't disable RLS on Supabase's internal tables!)
SELECT
    schemaname as "Schema",
    tablename as "Table Name",
    CASE
        WHEN rowsecurity THEN 'ENABLED ✅'
        ELSE 'DISABLED ⚠️'
    END as "RLS Status"
FROM pg_tables
WHERE schemaname = 'auth'
    AND tablename = 'users';

-- Expected Result:
-- auth.users should show 'ENABLED ✅' (this is correct and expected)

-- ============================================
-- STEP 7: Final Verification - All Tables
-- ============================================
-- Complete overview of all users tables
SELECT
    schemaname as "Schema",
    tablename as "Table",
    rowsecurity as "RLS Enabled",
    CASE
        WHEN schemaname = 'public' AND rowsecurity = false THEN '✅ CORRECT'
        WHEN schemaname = 'auth' AND rowsecurity = true THEN '✅ CORRECT'
        ELSE '⚠️ NEEDS ATTENTION'
    END as "Status"
FROM pg_tables
WHERE tablename = 'users'
ORDER BY schemaname;

-- ============================================
-- STEP 8: Test Data Access
-- ============================================
-- Verify you can access YOUR users table
SELECT COUNT(*) as "Total Users in public.users" FROM public.users;

-- ============================================
-- UNDERSTANDING THE TWO USERS TABLES
-- ============================================
-- 1. auth.users (Supabase Internal)
--    - Used by Supabase Auth (if you were using it)
--    - Managed by Supabase
--    - RLS should stay ENABLED
--    - DON'T modify this table
--
-- 2. public.users (Your Application)
--    - Used by your Spring Boot backend
--    - Your application manages this
--    - RLS should be DISABLED (Spring Boot handles auth)
--    - This is the one you work with
--
-- These are SEPARATE tables and don't conflict with each other.
-- Your Spring Boot app only uses public.users.
-- ============================================

-- ============================================
-- TROUBLESHOOTING
-- ============================================
-- If you still see issues, check which schema Spring Boot connects to:

SHOW search_path;

-- Default should be: "$user", public
-- This means it searches 'public' schema first, which is correct.

-- If needed, you can explicitly set the search path in your
-- Spring Boot application.properties:
-- spring.datasource.url=jdbc:postgresql://host:5432/postgres?currentSchema=public

-- ============================================
-- SUMMARY
-- ============================================
-- After running this script:
-- ✅ public.users (your app) - RLS DISABLED
-- ✅ public.todos (your app) - RLS DISABLED
-- ✅ public.verification_tokens (your app) - RLS DISABLED
-- ✅ auth.users (Supabase internal) - RLS ENABLED (don't touch)
--
-- Your Spring Boot backend should now work correctly!
-- ============================================
