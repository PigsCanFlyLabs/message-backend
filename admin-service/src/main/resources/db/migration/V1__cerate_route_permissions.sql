
CREATE SCHEMA IF NOT EXISTS spacebeaver_admin;

CREATE TABLE spacebeaver_admin.resource_permission (
	resource varchar(100),
	permission varchar(500)
);

CREATE TABLE spacebeaver_admin.roles_resource_access (
    id VARCHAR(100) PRIMARY KEY,
	user_type varchar(100) NULL,
	resource VARCHAR(500) NULL
);


CREATE TABLE IF NOT EXISTS spacebeaver_admin.admin_login (
	email VARCHAR(50) NOT NULL PRIMARY KEY,
	password VARCHAR(50) NOT NULL,
	role VARCHAR(50) NOT NULL
);


CREATE SCHEMA IF NOT EXISTS spacebeaver;

CREATE TABLE IF NOT EXISTS spacebeaver.users_mapping (
    customer_id VARCHAR,
	email VARCHAR(50) NOT NULL UNIQUE,
	device_id BIGINT PRIMARY KEY,
	phone_number VARCHAR(50) NOT NULL UNIQUE,
	is_disabled BOOLEAN DEFAULT FALSE NOT NULL
);
