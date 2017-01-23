-- Grant select like privs on all tables to pahma
-- Grant insert, delete and update on specified tables.
-- This will have to be adjusted to reflect the deployment

-- You must set the password for your installation!

DROP User '${db.bps.user}';
FLUSH PRIVILEGES;
CREATE USER '${db.bps.user}'@'%' IDENTIFIED BY '${db.bps.user.password}';
FLUSH PRIVILEGES;

GRANT SELECT,INSERT,UPDATE,DELETE ON ${db.name}.* TO '${db.bps.user}'@'%';
FLUSH PRIVILEGES;

