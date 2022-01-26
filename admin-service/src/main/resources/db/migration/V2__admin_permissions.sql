INSERT INTO spacebeaver_admin.resource_permission
(resource, permission)
VALUES('admin_permissions', 'get-user-details,create-user,update-user,disable-user');

INSERT INTO spacebeaver_admin.resource_permission
(resource, permission)
VALUES('super_admin_permissions', 'delete-user,create-admin-user');


INSERT INTO spacebeaver_admin.roles_resource_access
(id,user_type, resource)
VALUES(1,'admin', 'admin_permissions');

INSERT INTO spacebeaver_admin.roles_resource_access
(id,user_type, resource)
VALUES(2,'super_admin', 'super_admin_permissions,admin_permissions');