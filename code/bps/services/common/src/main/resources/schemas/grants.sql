-- Grant select like privs on all tables to pahma
-- Grant insert, delete and update on specified tables.
-- This will have to be adjusted to reflect the deployment

-- You must set the password for your installation!

CREATE USER 'bpsdev'@'%' identified by 'G0Names!';

GRANT SELECT,INSERT,UPDATE,DELETE ON bpsdev.* TO 'bpsdev'@'%';

