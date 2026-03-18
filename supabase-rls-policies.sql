-- ============================================
-- Supabase RLS Policies for Spring Boot Backend
-- ============================================
-- This script sets up Row Level Security policies for the todos, users,
-- and verification_tokens tables to work with a Spring Boot backend.
--
-- IMPORTANT: Since Spring Boot connects directly via JDBC (not Supabase Auth),
-- we need to allow the database service role to bypass RLS or grant full access.
-- ============================================

-- First, check what role your Spring Boot app uses to connect
-- Run this in Supabase SQL Editor to see the current role:
-- SELECT current_user, session_user;

-- ============================================
-- Option 1: Grant bypass RLS to service role (RECOMMENDED for Spring Boot)
-- ============================================
-- This allows your backend service to bypass RLS entirely.
-- Replace 'postgres' with your actual database role if different.

ALTER TABLE users FORCE ROW LEVEL SECURITY;
ALTER TABLE todos FORCE ROW LEVEL SECURITY;
ALTER TABLE verification_tokens FORCE ROW LEVEL SECURITY;

-- Grant bypass to the postgres/service role
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE todos DISABLE ROW LEVEL SECURITY;
ALTER TABLE verification_tokens DISABLE ROW LEVEL SECURITY;

-- ============================================
-- Option 2: Create policies for service role (if you want RLS enabled)
-- ============================================
-- Uncomment and use this if you want RLS enabled but allow service role access
-- This is more secure but requires the service role to be properly configured

/*
-- Re-enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE todos ENABLE ROW LEVEL SECURITY;
ALTER TABLE verification_tokens ENABLE ROW LEVEL SECURITY;

-- ============================================
-- USERS TABLE POLICIES
-- ============================================

-- Allow service role full access to users
CREATE POLICY "Service role has full access to users"
ON users
FOR ALL
TO postgres, authenticator, service_role
USING (true)
WITH CHECK (true);

-- Alternative: If you want to use authenticated users in the future
-- CREATE POLICY "Users can read their own data"
-- ON users FOR SELECT
-- TO authenticated
-- USING (auth.uid() = id);

-- CREATE POLICY "Users can update their own data"
-- ON users FOR UPDATE
-- TO authenticated
-- USING (auth.uid() = id)
-- WITH CHECK (auth.uid() = id);

-- ============================================
-- TODOS TABLE POLICIES
-- ============================================

-- Allow service role full access to todos
CREATE POLICY "Service role has full access to todos"
ON todos
FOR ALL
TO postgres, authenticator, service_role
USING (true)
WITH CHECK (true);

-- Alternative: If you want to use authenticated users in the future
-- CREATE POLICY "Users can read their own todos"
-- ON todos FOR SELECT
-- TO authenticated
-- USING (user_id = auth.uid());

-- CREATE POLICY "Users can insert their own todos"
-- ON todos FOR INSERT
-- TO authenticated
-- WITH CHECK (user_id = auth.uid());

-- CREATE POLICY "Users can update their own todos"
-- ON todos FOR UPDATE
-- TO authenticated
-- USING (user_id = auth.uid())
-- WITH CHECK (user_id = auth.uid());

-- CREATE POLICY "Users can delete their own todos"
-- ON todos FOR DELETE
-- TO authenticated
-- USING (user_id = auth.uid());

-- ============================================
-- VERIFICATION_TOKENS TABLE POLICIES
-- ============================================

-- Allow service role full access to verification_tokens
CREATE POLICY "Service role has full access to verification_tokens"
ON verification_tokens
FOR ALL
TO postgres, authenticator, service_role
USING (true)
WITH CHECK (true);

-- ============================================
-- Grant table permissions to service roles
-- ============================================

-- Grant all privileges to postgres role (your Spring Boot connection)
GRANT ALL ON users TO postgres;
GRANT ALL ON todos TO postgres;
GRANT ALL ON verification_tokens TO postgres;

-- Grant usage on sequences (for ID generation)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- If using authenticator or service_role:
-- GRANT ALL ON users TO authenticator;
-- GRANT ALL ON todos TO authenticator;
-- GRANT ALL ON verification_tokens TO authenticator;
-- GRANT ALL ON users TO service_role;
-- GRANT ALL ON todos TO service_role;
-- GRANT ALL ON verification_tokens TO service_role;
*/

-- ============================================
-- Verify RLS status (run this to check)
-- ============================================
-- SELECT schemaname, tablename, rowsecurity
-- FROM pg_tables
-- WHERE tablename IN ('users', 'todos', 'verification_tokens');

-- ============================================
-- NOTES:
-- ============================================
-- 1. Since you're using Spring Boot with direct JDBC connection,
--    the simplest approach is to DISABLE RLS (Option 1 above).
--
-- 2. Supabase RLS is designed for client-side access using Supabase Auth.
--    With Spring Boot, your backend is trusted and acts as the service layer.
--
-- 3. Your Spring Boot security handles authentication/authorization,
--    not Supabase RLS.
--
-- 4. If you still see "no data returned" errors, check:
--    - The database role in spring.datasource.username
--    - Run: SELECT current_user; in SQL Editor while connected as that user
--    - Ensure that role has proper grants
--
-- 5. To completely disable RLS and allow all access:
--    ALTER TABLE users DISABLE ROW LEVEL SECURITY;
--    ALTER TABLE todos DISABLE ROW LEVEL SECURITY;
--    ALTER TABLE verification_tokens DISABLE ROW LEVEL SECURITY;
