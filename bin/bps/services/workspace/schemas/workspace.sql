-- ----------------------------------------------------------------------
-- SQL create script for BPS corpus info tables
-- ----------------------------------------------------------------------

-- Define the main workspace table
DROP TABLE IF EXISTS `workspace`;
CREATE TABLE `workspace` (
  `id`            INT(10) UNSIGNED PRIMARY KEY NOT NULL,
  `name`          VARCHAR(255) NOT NULL default 'My Workspace',
  `description`   text NULL,
  `owner_id`      INT(10) UNSIGNED NOT NULL,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP,
	CONSTRAINT `wksp_ibfk_1` FOREIGN KEY (`owner_id`)
      REFERENCES `user` (`id`)
))ENGINE=MyIsam;
SHOW WARNINGS;

-- Define the configuration parameter table
DROP TABLE IF EXISTS `cfg_param`;
CREATE TABLE `cfg_param` (
  `id`            INT(10) UNSIGNED PRIMARY KEY auto_increment NOT NULL,
  `name`          VARCHAR(255) NOT NULL,
  `description`   text NULL,
  `scalar_type`   ENUM ('int', 'double') NOT NULL DEFAULT 'double',
  `int_default`   INT(10) not NULL default 0,
  `int_min`       INT(10) not NULL default -2147483648,
  `int_max`       INT(10) not NULL default 2147483647,
  `flt_default`   FLOAT(4,3) not NULL default 0.0,
  `flt_min`       FLOAT(4,3) not NULL default 0.0,
  `flt_max`       FLOAT(4,3) not NULL default 1.0,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP
)ENGINE=MyIsam;
SHOW WARNINGS;

-- Define the workspace configuration parameter instance table
DROP TABLE IF EXISTS `wksp_cfg_param`;
CREATE TABLE `wksp_cfg_param` (
  `id`            INT(10) UNSIGNED PRIMARY KEY auto_increment NOT NULL,
  `cfgp_id`       INT(10) UNSIGNED NOT NULL,
  `wksp_id`       INT(10) UNSIGNED NOT NULL,
  `int_value`     INT(10) not NULL default 0,
  `flt_value`     FLOAT(4,3) not NULL default 0.0,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP,
	CONSTRAINT `ucp_ibfk_1` FOREIGN KEY (`cfgp_id`)
      REFERENCES `cfg_param` (`id`),
	CONSTRAINT `ucp_ibfk_3` FOREIGN KEY (`wksp_id`)
      REFERENCES `workspace` (`id`)
)ENGINE=MyIsam;
SHOW WARNINGS;

-- Add Filter definition

-- Need to think abotu actions. Should ideally be a REST call
-- URI, payload, text description.
-- Could approximate this and interpret for now, but nice to model ideal.
-- CUD operations (READ not meaningful), individual
-- What does this mean, however, e.g., to map a nameref to an individual? CREATE/UPDATE? 
-- Need to define the resource model more clearly!!!
