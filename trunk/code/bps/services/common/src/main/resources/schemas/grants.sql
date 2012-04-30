-- Grant select like privs on all tables to pahma
-- Grant insert, delete and update on specified tables.
-- This will have to be adjusted to reflect the deployment

-- You must set the password for your installation!

DROP User '@DB_USER_BPS@';
FLUSH PRIVILEGES;
CREATE USER '@DB_USER_BPS@'@'%' IDENTIFIED BY '@DB_PASSWORD_BPS@';
FLUSH PRIVILEGES;

GRANT SELECT,INSERT,UPDATE,DELETE ON @DB_NAME@.* TO '@DB_USER_BPS@'@'%';
FLUSH PRIVILEGES;

