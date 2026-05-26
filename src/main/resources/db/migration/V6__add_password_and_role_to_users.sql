ALTER TABLE users
    ADD COLUMN password VARCHAR(255);

ALTER TABLE users
    ADD COLUMN role VARCHAR(255);

-- Заполняем уже существующие записи
UPDATE users
SET password = '$2a$10$7EqJtq98hPqEX7fNZaFWoOaS6Pz9Q7sKx2f8kH8M8a3m5cW8bX9aG',
    role = 'USER'
WHERE password IS NULL OR role IS NULL;

ALTER TABLE users
    ALTER COLUMN password SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN role SET NOT NULL;
