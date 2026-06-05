-- Seed admin user: admin@familyleague.com / Admin@1234
-- BCrypt hash of "Admin@1234"
INSERT INTO users (id, username, email, password_hash, display_name, role, is_active, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin',
    'admin@familyleague.com',
    '$2a$10$RmZb8HPReIDtNWqgV9UaNuXFwOqhwG.f9HXWy7Nxs5fRNkmrGC.GC',
    'Admin',
    'ADMIN',
    TRUE,
    now()
);
