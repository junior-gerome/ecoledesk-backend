INSERT INTO roles (
    active,
    created_at,
    updated_at,
    code,
    label,
    description
)
SELECT
    TRUE,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    'ADMIN',
    'Administrateur',
    'Administration complète de la plateforme'
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE UPPER(code) = 'ADMIN'
);