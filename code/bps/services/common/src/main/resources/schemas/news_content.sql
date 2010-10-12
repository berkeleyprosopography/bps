-- ----------------------------------------------------------------------
-- SQL create script for delphi news content table
-- ----------------------------------------------------------------------

-- Define the news content table
DROP TABLE IF EXISTS `newsContent`;
CREATE TABLE `newsContent` (
  `id`          INT(10) UNSIGNED PRIMARY KEY auto_increment NOT NULL,
  `header`      VARCHAR(255) NOT NULL,
  `content`     text NOT NULL,
  `start_time`  timestamp NULL,
	`end_time`    timestamp NULL
)ENGINE=MyIsam;
SHOW WARNINGS;

--GRANT INSERT,UPDATE,DELETE ON pahma_dev.newsContent
--TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';

--GRANT INSERT,UPDATE,DELETE ON pahma_prod.newsContent
--TO 'pahma'@'webfarm%.berkeley.edu' IDENTIFIED BY 'password';

INSERT INTO newsContent(id, header, content) VALUES (0, "", "");
