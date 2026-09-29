-- V4 seeded admin@admin.com with a publicly known password in every environment.
-- Remove it, but only if the password was never changed; an account whose owner
-- set a new password is left alone. Demo data now comes from DevDataSeeder
-- (Spring profile "dev"). Related expenses/tokens are removed via ON DELETE CASCADE.
DELETE FROM users
WHERE email = 'admin@admin.com'
  AND password = '$2a$10$g1VifdOoYk7J7pkE2ETpd.p7C8baBOXO4RunNjgqbrJgbPbouadNK';
