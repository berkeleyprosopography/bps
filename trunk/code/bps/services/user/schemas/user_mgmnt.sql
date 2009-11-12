--
-- Table structure for table `user`
-- Should the user table have the core role to denormalize and save a join?
-- How often will users have multiple roles?
--
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`            int(10) unsigned NOT NULL auto_increment,
  `username`      varchar(40) NOT NULL UNIQUE,
  `passwdmd5`     varchar(32) NOT NULL,        -- MD5 of the pw
  `email`         varchar(80) NOT NULL,           -- allow for very long email addresses
  `real_name`     varchar(100) NULL,
  `website_url`   varchar(255) NULL,
  `affiliation`   varchar(255) NULL,
  `about`         text NULL,
  `pending`       boolean NOT NULL default true, -- on creation, is unverified
  `blocked`       boolean NOT NULL default false,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP 
        on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`)
) ENGINE=MyIsam DEFAULT CHARSET=latin1;
SHOW WARNINGS;

--
-- Table structure for table `role`
--
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id`            int(10) unsigned NOT NULL auto_increment,
  `name`          varchar(40) NOT NULL UNIQUE,
  `wksp_role`     boolean NOT NULL default false, -- Workspace specific
  `description`   text,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`)
) ENGINE=MyIsam DEFAULT CHARSET=latin1;
SHOW WARNINGS;

--
-- Table structure for table `permission`
--
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id`            int(10) unsigned NOT NULL auto_increment,
  `name`          varchar(40) NOT NULL UNIQUE,
  `description`   text,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`)
) ENGINE=MyIsam DEFAULT CHARSET=latin1;
SHOW WARNINGS;

--
-- Table structure for table `user_roles`
-- Associates roles to users for a workspace,
-- or globally if workspace context is -1.
--
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
  `user_id`       int(10) unsigned NOT NULL,
  `role_id`       int(10) unsigned NOT NULL,
  `workspace_id`  int(10) NOT NULL default -1,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`user_id`, `role_id`, `workspace_id`),
  CONSTRAINT `ur_ibfk_1` FOREIGN KEY (`user_id`)
        REFERENCES `user` (`id`),
  CONSTRAINT `ur_ibfk_2` FOREIGN KEY (`role_id`)
        REFERENCES `role` (`id`)
  -- Do we need to index the roles? Will we ask "who all has role X"?
) ENGINE=MyIsam DEFAULT CHARSET=latin1;
SHOW WARNINGS;

--
-- Table structure for table `role_perms`
-- Associates permissions to roles
-- Another possibility would be to have a mask or two of perms
-- that are stored in the role table, again to save joins.
--
DROP TABLE IF EXISTS `role_perms`;
CREATE TABLE `role_perms` (
  `role_id`       int(10) unsigned NOT NULL,
  `perm_id`       int(10) unsigned NOT NULL,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  UNIQUE KEY  (`role_id`,`perm_id`),
  CONSTRAINT `rp_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `rp_ibfk_2` FOREIGN KEY (`perm_id`) REFERENCES `permission` (`id`)
  -- Do we need to index the roles? Will we ask "who all has role X"?
) ENGINE=MyIsam DEFAULT CHARSET=latin1;
SHOW WARNINGS;
