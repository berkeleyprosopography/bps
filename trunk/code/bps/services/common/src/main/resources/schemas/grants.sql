-- Grant select like privs on all tables to pahma
-- Grant insert, delete and update on specified tables.
-- This will have to be adjusted to reflect the deployment

-- You must set the password for your installation!

GRANT SELECT ON pahma_dev.* TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';

GRANT INSERT,UPDATE,DELETE ON pahma_dev.user
TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';
GRANT INSERT,UPDATE,DELETE ON pahma_dev.permission 
TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';
GRANT INSERT,UPDATE,DELETE ON pahma_dev.role 
TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';
GRANT INSERT,UPDATE,DELETE ON pahma_dev.user_roles
TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';
GRANT INSERT,UPDATE,DELETE ON pahma_dev.role_perms 
TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';
GRANT INSERT,UPDATE,DELETE ON pahma_dev.sets 
TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';
GRANT INSERT,UPDATE,DELETE ON pahma_dev.set_objs 
TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';
GRANT INSERT,UPDATE,DELETE ON pahma_dev.tags 
TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';
GRANT INSERT,UPDATE,DELETE ON pahma_dev.tag_user_object
TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';

