INSERT INTO roles (
    code,
    label,
    description,
    active,
    created_at,
    updated_at
)
VALUES (
           'ADMIN',
           'Administrateur',
           'Administrateur système',
           true,
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       );