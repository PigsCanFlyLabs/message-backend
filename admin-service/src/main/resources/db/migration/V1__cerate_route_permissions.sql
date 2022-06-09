
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
    customer_id VARCHAR(50) NOT NULL UNIQUE,
	email VARCHAR(50) UNIQUE,
	device_id BIGINT PRIMARY KEY,
	phone_number VARCHAR(50) UNIQUE,
	is_disabled BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE IF NOT EXISTS spacebeaver.message_logs (
    device_id VARCHAR(50),
    customer_id VARCHAR(50),
	receiver VARCHAR(50),
	source_destination VARCHAR(50),
	request_type VARCHAR(50),
	message_id VARCHAR(50) UNIQUE,
	date_time TIMESTAMP DEFAULT now(),
	FOREIGN KEY(customer_id) REFERENCES spacebeaver.users_mapping(customer_id),
	PRIMARY KEY(customer_id, date_time)
);