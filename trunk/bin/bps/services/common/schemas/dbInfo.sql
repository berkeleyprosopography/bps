-- ----------------------------------------------------------------------
-- SQL create script for BPS main object info tables
-- ----------------------------------------------------------------------

-- The DBInfo table has a single row and is just used to hold system-wide
-- parameters such as the sizes of alternate image sizes, the version of this
-- DB schema, etc.
DROP TABLE IF EXISTS DBInfo;
CREATE TABLE DBInfo (
  `version`           VARCHAR(16) NOT NULL,
  `lockoutActive`     boolean NOT NULL default false, -- Allows for maintenance lockout
  `creation_time`     timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`          timestamp NOT NULL default CURRENT_TIMESTAMP 
        on update CURRENT_TIMESTAMP
); 
SHOW WARNINGS;

INSERT INTO DBInfo( version, creation_time ) 
  VALUES( '0.1 alpha', now() );
SHOW WARNINGS;

