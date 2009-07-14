-- ----------------------------------------------------------------------
-- SQL create script for BPS corpus info tables
-- ----------------------------------------------------------------------

-- Define the main corpus table
DROP TABLE IF EXISTS `corpus`;
CREATE TABLE `corpus` (
  `id`            INT(10) UNSIGNED PRIMARY KEY NOT NULL,
  `name`          VARCHAR(255) NOT NULL,
  `description`   text NULL,
  `owner`         INT(10) UNSIGNED NOT NULL,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP
)ENGINE=MyIsam;
SHOW WARNINGS;

-- Define the Document table
-- The normalized date is a signed count of days from the system origin.
-- ?? Should we keep the TEI for this doc in the DB?
DROP TABLE IF EXISTS `document`;
CREATE TABLE `document` (
  `id`            INT(10) UNSIGNED PRIMARY KEY auto_increment NOT NULL,
  `alt_id`        VARCHAR(255) NULL,
  `sourceURL`     VARCHAR(255) NULL,
  `xml_id`        VARCHAR(255) NULL,
  `notes`         text NULL,
  `date_str`      VARCHAR(255) NULL,
  `date_norm`     INT(8) NULL,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP
)ENGINE=MyIsam;
SHOW WARNINGS;

-- Activities may be 1:1 in some corpora, but not in general.
-- These are the basis for establish social networks.
-- Some are symmetric (e.g., 'co-owned') and others directed (e.g., 'sold', 'inherited').
-- None are defined explicitly in the DB definition, but are rather configured
-- or defined implicitly by the data.
-- Activities can be nested, if there is a non-null parent.
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id`             int(10) unsigned PRIMARY KEY NOT NULL auto_increment,
  `name`           VARCHAR(255) NOT NULL,
  `description`    text NULL,
  `parent`         INT(10) UNSIGNED default NULL,
  `creation_time`  timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`       timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP
)ENGINE=MyIsam;
SHOW WARNINGS;

-- Define activity roles
-- These are important for defining social networks.
-- These are not defined explicitly in the DB definition, but are rather configured
-- or defined implicitly by the data.
-- We do not currently constrain roles to certain activities.
DROP TABLE IF EXISTS `act_role`;
CREATE TABLE `act_role` (
  `id`             int(10) unsigned PRIMARY KEY NOT NULL auto_increment,
  `name`           VARCHAR(255) NOT NULL,
  `description`    text NULL,
  `creation_time`  timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`       timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP
)ENGINE=MyIsam;
SHOW WARNINGS;

-- Names are just the string variants, with linkages to normal forms
-- If 'normal' is null, this is the normal form.
-- The are not people (individuals), nor are they citations.
DROP TABLE IF EXISTS `name`;
CREATE TABLE `name` (
  `id`             int(10) unsigned PRIMARY KEY NOT NULL auto_increment,
  `name`           VARCHAR(255) NOT NULL,
  `notes`          text NULL,
  `normal`         INT(10) UNSIGNED default NULL,
  `creation_time`  timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`       timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP,
	CONSTRAINT `name_ibfk_1` FOREIGN KEY (`normal`)
      REFERENCES `name` (`id`)
)ENGINE=MyIsam;
SHOW WARNINGS;

-- Define the table that collects all instances of names in documents
-- with associated roles in activities.
-- The xml_idref provides a link into the original TEI document, but
-- may be needed for disambiguation if one name is involved in multiple
-- activities in a given document.
DROP TABLE IF EXISTS `name_role_activity_doc`;
CREATE TABLE `name_role_activity_doc` (
  `id`          int(10) unsigned PRIMARY KEY NOT NULL auto_increment,
  `name`        int(10) unsigned NOT NULL,
  `act_role`    int(10) unsigned NOT NULL,
  `activity`    int(10) unsigned NOT NULL,
  `document`    int(10) unsigned NOT NULL,
  `xml_idref`   VARCHAR(255) NULL,   -- ref into XML for document.
  INDEX `nrad_nrad_index` (`name`,`act_role`,`activity`,`document`),
  INDEX `nrad_r_index` (`act_role`),
  INDEX `nrad_a_index` (`activity`),
  INDEX `nrad_d_index` (`document`),
	CONSTRAINT `nrad_ibfk_1` FOREIGN KEY (`name`)
      REFERENCES `name` (`id`),
	CONSTRAINT `nrad_ibfk_2` FOREIGN KEY (`act_role`)
      REFERENCES `act_role` (`id`),
	CONSTRAINT `nrad_ibfk_3` FOREIGN KEY (`activity`)
      REFERENCES `activity` (`id`),
	CONSTRAINT `nrad_ibfk_4` FOREIGN KEY (`document`)
      REFERENCES `document` (`id`)
)ENGINE=MyIsam;
SHOW WARNINGS;

-- Define the table that collects all instances of familial relation
-- declarations in a document.
-- These link instances of name_role_activity_doc, to patronyms and ancestor names.
-- May have to change the link_type to be normalized and dynamic from corpus.
DROP TABLE IF EXISTS `familylink`;
CREATE TABLE `familylink` (
  `id`          int(10) unsigned PRIMARY KEY NOT NULL auto_increment,
  `nrad`        int(10) unsigned NOT NULL,
  `name`        int(10) unsigned NOT NULL,
  `link_type`   ENUM ('father', 'grandfather', 'mother', 'ancestor' ) NOT NULL DEFAULT 'father',
  `xml_idref`   VARCHAR(255) NULL,   -- ref into XML for document.
  INDEX `fl_nrad_index` (`nrad`),
	CONSTRAINT `fl_ibfk_1` FOREIGN KEY (`nrad`)
      REFERENCES `name_role_activity_doc` (`id`),
	CONSTRAINT `fl_ibfk_2` FOREIGN KEY (`name`)
      REFERENCES `name` (`id`)
)ENGINE=MyIsam;
SHOW WARNINGS;
