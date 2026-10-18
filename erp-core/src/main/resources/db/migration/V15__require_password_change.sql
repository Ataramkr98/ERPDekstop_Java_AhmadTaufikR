-- Flag the seeded default administrator as needing a password change on first sign-in.
--
-- V14 inserted a known password (admin/admin) so a fresh checkout is usable. Leaving
-- must_change_password false meant the UI never prompted anyone to replace it, so the
-- documented credential stayed valid indefinitely on any deployment that forgot to
-- change it. This migration closes that gap.
--
-- Scope: only accounts that have never signed in. Once someone has logged in they have
-- almost certainly replaced the password, and re-flagging it would be a false warning.
update app_user
   set must_change_password = true
 where last_login_at is null
   and username = 'admin';

insert into audit_log (company_id, entity_type, entity_id, action, username, after_value, occurred_at)
select id, 'Company', id, 'SECURITY',
       'migration',
       'Default administrator flagged for a mandatory password change on first sign-in.',
       now()
  from company
 order by id
 limit 1;