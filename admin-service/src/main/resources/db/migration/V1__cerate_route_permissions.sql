
CREATE SCHEMA IF NOT EXISTS admin;

CREATE TABLE admin.resource_permission (
	resource varchar NULL,
	"permission" _text NULL
);

CREATE TABLE admin.roles_resource_access (
    id VARCHAR NOT NULL,
	user_type varchar NULL,
	resource _text NULL
);


CREATE TABLE IF NOT EXISTS admin.admin_login (
	email VARCHAR NOT NULL PRIMARY KEY,
	password VARCHAR NOT NULL,
	role VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS admin.users_mapping (
	email VARCHAR NOT NULL,
	device_id INT NOT NULL,
	name VARCHAR NOT NULL,
	is_disabled BOOLEAN default false,
	primary key (device_id)
);
