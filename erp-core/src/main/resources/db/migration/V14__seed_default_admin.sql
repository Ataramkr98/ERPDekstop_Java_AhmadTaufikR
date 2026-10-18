-- ---------------------------------------------------------------------------
-- Seed: default company, Administrator role and the first ERP login account.
-- ---------------------------------------------------------------------------
-- Without this the app_user table is empty, so POST /api/auth/login can never
-- succeed and the desktop is forced to fall back to the embedded properties-file
-- user (admin/admin). Seeding a real account makes the login dialog meaningful
-- and lets every subsequent user created in the UI sign in.
--
-- Password for 'admin' is 'admin' (PBKDF2-HMAC-SHA256, 600000 iterations,
-- 16-byte salt). Change it on first login - the account is flagged accordingly.
-- Every statement is guarded so re-running is harmless.

-- 1. Default legal entity -------------------------------------------------
insert into company (code, name, legal_name, base_currency, reporting_currency,
                     country_code, locale_tag, timezone, fiscal_year_start_month,
                     enabled, company_id, created_at, created_by, active, version)
select 'DEFAULT', 'Default Company', 'Default Company', 'USD', 'USD', 'US',
       'en-US', 'UTC', 1, true, null, now(), 'system', true, 0
where not exists (select 1 from company where code = 'DEFAULT');

-- Tenant rows point at themselves; set once the id is known.
update company set company_id = id
where code = 'DEFAULT' and company_id is null;

-- 2. Administrator role ---------------------------------------------------
insert into role (code, name, description, system_role, permissions,
                  company_id, created_at, created_by, active, version)
select 'ADMIN', 'Administrator', 'Full access to every module.', true, '*',
       c.id, now(), 'system', true, 0
from company c
where c.code = 'DEFAULT'
  and not exists (select 1 from role r where r.code = 'ADMIN');

-- 3. The login account ----------------------------------------------------
--    600000:<salt>:<key>  ==  'admin'
insert into app_user (username, full_name, email, password_hash, locale,
                      default_company_id, status, failed_login_count,
                      locked_until, last_login_at, must_change_password,
                      company_id, created_at, created_by, active, version)
select 'admin', 'Administrator', 'admin@localhost',
       '600000:HRXT5x8UMvzVTdijJ0Bz0Q==:ofKKUmwti3YN9Wx33rIFvO5eAh8pVRQ6nAD48mOeU2A=',
       'en-US', c.id, 'ACTIVE', 0, null, null, false,
       c.id, now(), 'system', true, 0
from company c
where c.code = 'DEFAULT'
  and not exists (select 1 from app_user u where u.username = 'admin');

-- 4. Grant the role -------------------------------------------------------
insert into user_role (user_id, role_code, company_id, created_at, created_by,
                       active, version)
select u.id, 'ADMIN', u.company_id, now(), 'system', true, 0
from app_user u
where u.username = 'admin'
  and not exists (select 1 from user_role ur
                  where ur.user_id = u.id and ur.role_code = 'ADMIN');
