--
-- Table structure for table core_admin_auth_db_module
--
DROP TABLE IF EXISTS core_admin_auth_db_module;
CREATE TABLE core_admin_auth_db_module (
	access_code varchar(16) default '' NOT NULL,
	password varchar(16) default '' NOT NULL,
	date_valid_password date default '0000-00-00' NOT NULL,
	last_password varchar(16) default '' NOT NULL,
	last_name varchar(100) default '' NOT NULL,
	first_name varchar(100) default '' NOT NULL,
	email varchar(100) default '' NOT NULL,
	PRIMARY KEY (access_code)
);