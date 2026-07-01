INSERT INTO permissions (code, description) VALUES
('identity.users.read', 'Read users'),
('identity.users.write', 'Create and update users'),
('identity.roles.manage', 'Manage roles and permissions');

INSERT INTO roles (code, name) VALUES
('ADMIN', 'Administrator'),
('STAFF', 'Staff member');

INSERT INTO profiles (code, name) VALUES
('ADMINISTRATION', 'Administration'),
('TEACHING_STAFF', 'Teaching staff');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'identity.users.read'
WHERE r.code = 'STAFF';

INSERT INTO profile_roles (profile_id, role_id)
SELECT p.id, r.id
FROM profiles p
JOIN roles r ON r.code = 'ADMIN'
WHERE p.code = 'ADMINISTRATION';

INSERT INTO profile_roles (profile_id, role_id)
SELECT p.id, r.id
FROM profiles p
JOIN roles r ON r.code = 'STAFF'
WHERE p.code = 'TEACHING_STAFF';
