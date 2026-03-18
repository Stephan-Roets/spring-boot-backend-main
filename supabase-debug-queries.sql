-- ============================================
-- Supabase Debugging and Verification Queries
-- ============================================
-- Use these queries in the Supabase SQL Editor to diagnose issues
-- with your Spring Boot backend connection and RLS configuration.
-- ============================================

-- ============================================
-- 1. Check Current User and Session
-- ============================================
-- Shows which database role you're currently connected as
SELECT
    current_user as "Current User",
    session_user as "Session User",
    current_database() as "Database",
    inet_client_addr() as "Client IP",
    inet_server_addr() as "Server IP";

-- ============================================
-- 2. Verify RLS Status
-- ============================================
-- Shows if RLS is enabled or disabled on each table
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

-- Expected Result for Spring Boot:
-- All tables should show "DISABLED ✅"

-- ============================================
-- 3. List All RLS Policies
-- ============================================
-- Shows all existing policies (should be empty if RLS is disabled)
SELECT
    schemaname as "Schema",
    tablename as "Table",
    policyname as "Policy Name",
    permissive as "Type",
    roles as "Roles",
    cmd as "Command",
    qual as "USING Expression",
    with_check as "WITH CHECK Expression"
FROM pg_policies
WHERE schemaname = 'public'
    AND tablename IN ('users', 'todos', 'verification_tokens')
ORDER BY tablename, policyname;

-- Expected Result: No rows (no policies needed when RLS disabled)

-- ============================================
-- 4. Check Table Permissions
-- ============================================
-- Shows what permissions the postgres role has on each table
SELECT
    grantee as "Role",
    table_schema as "Schema",
    table_name as "Table",
    string_agg(privilege_type, ', ') as "Privileges"
FROM information_schema.table_privileges
WHERE table_schema = 'public'
    AND table_name IN ('users', 'todos', 'verification_tokens')
    AND grantee IN ('postgres', 'authenticator', 'anon', 'authenticated')
GROUP BY grantee, table_schema, table_name
ORDER BY table_name, grantee;

-- Expected Result: postgres should have all privileges

-- ============================================
-- 5. Check Sequence Permissions (for ID generation)
-- ============================================
-- Ensures your backend can generate UUIDs/IDs
SELECT
    grantee as "Role",
    sequence_schema as "Schema",
    sequence_name as "Sequence",
    string_agg(privilege_type, ', ') as "Privileges"
FROM information_schema.usage_privileges
WHERE sequence_schema = 'public'
    AND grantee IN ('postgres', 'authenticator')
GROUP BY grantee, sequence_schema, sequence_name
ORDER BY sequence_name, grantee;

-- ============================================
-- 6. Verify Table Structure
-- ============================================
-- Shows columns and data types for users table
SELECT
    column_name as "Column",
    data_type as "Type",
    is_nullable as "Nullable",
    column_default as "Default"
FROM information_schema.columns
WHERE table_schema = 'public'
    AND table_name = 'users'
ORDER BY ordinal_position;

-- Shows columns and data types for todos table
SELECT
    column_name as "Column",
    data_type as "Type",
    is_nullable as "Nullable",
    column_default as "Default"
FROM information_schema.columns
WHERE table_schema = 'public'
    AND table_name = 'todos'
ORDER BY ordinal_position;

-- Shows columns and data types for verification_tokens table
SELECT
    column_name as "Column",
    data_type as "Type",
    is_nullable as "Nullable",
    column_default as "Default"
FROM information_schema.columns
WHERE table_schema = 'public'
    AND table_name = 'verification_tokens'
ORDER BY ordinal_position;

-- ============================================
-- 7. Check Foreign Key Constraints
-- ============================================
-- Verifies relationships between tables
SELECT
    tc.table_name as "Table",
    kcu.column_name as "Column",
    ccu.table_name AS "References Table",
    ccu.column_name AS "References Column",
    tc.constraint_name as "Constraint Name"
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_name IN ('users', 'todos', 'verification_tokens')
ORDER BY tc.table_name;

-- ============================================
-- 8. Count Records in Each Table
-- ============================================
-- Shows how many records exist in each table
SELECT 'users' as "Table", COUNT(*) as "Count" FROM users
UNION ALL
SELECT 'todos' as "Table", COUNT(*) as "Count" FROM todos
UNION ALL
SELECT 'verification_tokens' as "Table", COUNT(*) as "Count" FROM verification_tokens;

-- ============================================
-- 9. Check Recent Users
-- ============================================
-- Shows last 5 users created
SELECT
    id,
    email,
    name,
    email_verified,
    role,
    created_at,
    updated_at
FROM users
ORDER BY created_at DESC
LIMIT 5;

-- ============================================
-- 10. Check Verification Tokens Status
-- ============================================
-- Shows verification tokens and their status
SELECT
    vt.id,
    u.email as "User Email",
    u.name as "User Name",
    vt.used as "Used",
    vt.expires_at as "Expires At",
    CASE
        WHEN vt.used THEN 'Already Used ✅'
        WHEN vt.expires_at < NOW() THEN 'Expired ⚠️'
        ELSE 'Valid ✅'
    END as "Status",
    vt.created_at as "Created At"
FROM verification_tokens vt
JOIN users u ON vt.user_id = u.id
ORDER BY vt.created_at DESC
LIMIT 10;

-- ============================================
-- 11. Find Unverified Users
-- ============================================
-- Shows users who haven't verified their email
SELECT
    id,
    email,
    name,
    created_at,
    EXTRACT(EPOCH FROM (NOW() - created_at))/3600 as "Hours Since Signup"
FROM users
WHERE email_verified = false
ORDER BY created_at DESC;

-- ============================================
-- 12. Check Todos by User
-- ============================================
-- Shows todos grouped by user (helps verify user ownership)
SELECT
    u.email as "User",
    COUNT(t.id) as "Todo Count",
    COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) as "Completed",
    COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) as "In Progress",
    COUNT(CASE WHEN t.status = 'PENDING' THEN 1 END) as "Pending"
FROM users u
LEFT JOIN todos t ON u.id = t.user_id
GROUP BY u.id, u.email
ORDER BY "Todo Count" DESC;

-- ============================================
-- 13. Check Database Roles
-- ============================================
-- Shows all database roles and their privileges
SELECT
    rolname as "Role Name",
    rolsuper as "Superuser",
    rolcreaterole as "Can Create Roles",
    rolcreatedb as "Can Create DB",
    rolcanlogin as "Can Login"
FROM pg_roles
WHERE rolname IN ('postgres', 'authenticator', 'anon', 'authenticated', 'service_role')
ORDER BY rolname;

-- ============================================
-- 14. Check Active Connections
-- ============================================
-- Shows active database connections (useful for debugging connection pool)
SELECT
    datname as "Database",
    usename as "User",
    application_name as "Application",
    client_addr as "Client IP",
    state as "State",
    query_start as "Query Started",
    state_change as "State Changed"
FROM pg_stat_activity
WHERE datname = current_database()
    AND usename != 'supabase_admin'
ORDER BY query_start DESC;

-- ============================================
-- 15. Grant Full Permissions (if needed)
-- ============================================
-- Run this ONLY if you're getting permission errors after disabling RLS
-- Uncomment to use:

/*
-- Grant all table privileges
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;

-- Grant all sequence privileges (for ID generation)
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- Grant schema usage
GRANT USAGE ON SCHEMA public TO postgres;

-- If using authenticator role:
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO authenticator;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO authenticator;
*/

-- ============================================
-- 16. Test Data Access (Insert/Select/Update/Delete)
-- ============================================
-- Test if you can perform basic CRUD operations
-- Uncomment to test:

/*
-- Test INSERT
INSERT INTO users (email, password_hash, name, role, email_verified)
VALUES ('test@example.com', 'test_hash', 'Test User', 'USER', false)
RETURNING id, email, name;

-- Test SELECT
SELECT * FROM users WHERE email = 'test@example.com';

-- Test UPDATE
UPDATE users
SET email_verified = true
WHERE email = 'test@example.com'
RETURNING id, email, email_verified;

-- Test DELETE
DELETE FROM users WHERE email = 'test@example.com'
RETURNING email;
*/

-- ============================================
-- 17. Check Email Verification Flow
-- ============================================
-- Verify complete flow for a specific user
-- Replace 'user@example.com' with actual email

/*
-- Check user verification status
SELECT
    u.email,
    u.name,
    u.email_verified,
    u.created_at as "User Created",
    vt.token as "Latest Token",
    vt.expires_at as "Token Expires",
    vt.used as "Token Used",
    CASE
        WHEN u.email_verified THEN 'Email Verified ✅'
        WHEN vt.used THEN 'Token Used but Email Not Verified ⚠️'
        WHEN vt.expires_at < NOW() THEN 'Token Expired ⚠️'
        ELSE 'Pending Verification ⏳'
    END as "Status"
FROM users u
LEFT JOIN verification_tokens vt ON u.id = vt.user_id
WHERE u.email = 'user@example.com'
ORDER BY vt.created_at DESC
LIMIT 1;
*/

-- ============================================
-- 18. Clean Up Expired Tokens
-- ============================================
-- Remove old expired verification tokens (good for maintenance)
-- Uncomment to use:

/*
DELETE FROM verification_tokens
WHERE expires_at < NOW() - INTERVAL '7 days'
RETURNING id, created_at, expires_at;
*/

-- ============================================
-- 19. Database Health Check
-- ============================================
-- Overall database statistics
SELECT
    pg_database.datname as "Database",
    pg_database_size(pg_database.datname) / 1024 / 1024 as "Size (MB)",
    (SELECT COUNT(*) FROM users) as "Total Users",
    (SELECT COUNT(*) FROM todos) as "Total Todos",
    (SELECT COUNT(*) FROM verification_tokens) as "Total Tokens",
    NOW() as "Current Time"
FROM pg_database
WHERE datname = current_database();

-- ============================================
-- 20. Quick Troubleshooting Summary
-- ============================================
-- One query to check everything important
SELECT
    'Database' as "Check",
    current_database() as "Value",
    '✅' as "Status"
UNION ALL
SELECT
    'Current User' as "Check",
    current_user as "Value",
    CASE WHEN current_user = 'postgres' THEN '✅' ELSE '⚠️' END as "Status"
UNION ALL
SELECT
    'Users Table RLS' as "Check",
    CASE WHEN rowsecurity THEN 'ENABLED' ELSE 'DISABLED' END as "Value",
    CASE WHEN rowsecurity THEN '⚠️' ELSE '✅' END as "Status"
FROM pg_tables WHERE tablename = 'users'
UNION ALL
SELECT
    'Todos Table RLS' as "Check",
    CASE WHEN rowsecurity THEN 'ENABLED' ELSE 'DISABLED' END as "Value",
    CASE WHEN rowsecurity THEN '⚠️' ELSE '✅' END as "Status"
FROM pg_tables WHERE tablename = 'todos'
UNION ALL
SELECT
    'Tokens Table RLS' as "Check",
    CASE WHEN rowsecurity THEN 'ENABLED' ELSE 'DISABLED' END as "Value",
    CASE WHEN rowsecurity THEN '⚠️' ELSE '✅' END as "Status"
FROM pg_tables WHERE tablename = 'verification_tokens'
UNION ALL
SELECT
    'Total Users' as "Check",
    COUNT(*)::text as "Value",
    '✅' as "Status"
FROM users
UNION ALL
SELECT
    'Verified Users' as "Check",
    COUNT(*)::text as "Value",
    '✅' as "Status"
FROM users WHERE email_verified = true;

-- ============================================
-- NOTES:
-- ============================================
-- 1. Run queries individually or in groups
-- 2. Use Supabase SQL Editor for best experience
-- 3. For Spring Boot backends, all RLS should be DISABLED
-- 4. postgres role should have all privileges
-- 5. If you see permission errors, run query #15
-- ============================================
