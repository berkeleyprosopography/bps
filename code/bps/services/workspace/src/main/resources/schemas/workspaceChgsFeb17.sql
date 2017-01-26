-- ----------------------------------------------------------------------
-- SQL changes scripts to update an existing DB for persistence of collapser rules
-- This assumes there is an existing workspace schema, and will not disturb existing data.
-- ----------------------------------------------------------------------

-- Add columns to the workspace table
ALTER TABLE `workspace` ADD `activeLifeWindow`  DOUBLE NOT NULL default 0.0 AFTER `owner_id`;
ALTER TABLE `workspace` ADD `activeLifeStdDev`  DOUBLE NOT NULL default 0.0 AFTER `activeLifeWindow`;
ALTER TABLE `workspace` ADD `generationOffset`  BIGINT NOT NULL default 0 AFTER `activeLifeStdDev`;

-- Define the workspace collapser rule table
-- We have a joint index on the id+name+item, and since we use Unicode, MySQL assumes
-- up to 3 bytes per char, and allows a total of 1000 bytes for a key. This is why
-- we tighten up the max lengths for the name and item (these are still pretty liberal).
-- 'item' field is (only) for matrix rules, and uses row-col name-pairs as the values
-- Note that this just holds the weights, and not the full definition.
DROP TABLE IF EXISTS `wksp_collapser_rule`;
CREATE TABLE `wksp_collapser_rule` (
  `wksp_id`       INT(10) UNSIGNED NOT NULL,
  `name`          VARCHAR(120) NOT NULL,
  `item`          VARCHAR(200) NOT NULL default '.',
  `weight`        DOUBLE NOT NULL default 1.0,
  `creation_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mod_time`      timestamp NOT NULL default CURRENT_TIMESTAMP
        on update CURRENT_TIMESTAMP,
	PRIMARY KEY(`wksp_id`, `name`, `item`),
	CONSTRAINT `wcr_ibfk_3` FOREIGN KEY (`wksp_id`)
      REFERENCES `workspace` (`id`)
)ENGINE=MyIsam;
SHOW WARNINGS;

