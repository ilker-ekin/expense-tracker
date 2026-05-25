INSERT INTO users (email, password, full_name, role, email_verified)
VALUES (
    'admin@admin.com',
    '$2a$10$g1VifdOoYk7J7pkE2ETpd.p7C8baBOXO4RunNjgqbrJgbPbouadNK',
    'Admin',
    'USER',
    true
)
ON CONFLICT (email) DO NOTHING;
