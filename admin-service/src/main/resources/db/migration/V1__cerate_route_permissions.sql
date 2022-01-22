
CREATE SCHEMA IF NOT EXISTS admin;

CREATE TABLE admin.resource_permission (
	resource varchar(100),
	permission varchar(500)
);

CREATE TABLE admin.roles_resource_access (
    id VARCHAR(100) NOT NULL,
	user_type varchar(100) NULL,
	resource VARCHAR(500) NULL
);


CREATE TABLE IF NOT EXISTS admin.admin_login (
	email VARCHAR(50) NOT NULL PRIMARY KEY,
	password VARCHAR(50) NOT NULL,
	role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS admin.users_mapping (
	email VARCHAR(50) NOT NULL,
	device_id VARCHAR(50) NOT NULL,
	name VARCHAR(50) NOT NULL,
	is_disabled BOOLEAN default false,
	primary key (email,device_id)
);
