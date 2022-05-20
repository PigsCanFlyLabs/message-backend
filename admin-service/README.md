## Admin Service

This module is developed using Akka HTTP with Slick intergration to execute database queries.
This project handles authorization using JWT HS256 encoding for multiple roles i.e. each route is accessible by specified role only.

### clean

This command cleans the sbt project by deleting the target directory. The command output relevant messages.
````
sbt clean
````

### compile

This command compiles the scala source classes of the sbt project.
````
sbt compile
````
### run

Enter the project folder and enter sbt run command:
````
sbt "project adminService" run
````

More details about project libraraies (e.g. version etc..) can be found in files:

**build.sbt**

**Dependencies**

**CommonSettings**

**plugins.sbt**


## Routes

###### Note: json file (Admin Service.postman_collection) for the routes is added in the repository.


````
URL <- localhost:8081
````


#### Admin/ Super Admin Login
###### Request: POST <- /admin/login
###### Body: raw JSON

###### Admin Login:

````
{
    "email": "admin@admin.com",
    "password": "C+/:~Bp75bU?ays:",
    "role": "admin"
}
````

###### Super Admin Login:

````
{
    "email": "admin@admin.com",
    "password": "C+/:~Bp75bU?ays:",
    "role": "admin"
}
````

#### Create Admin User  (accessible by admin/ super admin)
###### Request: POST <- /admin/create-admin-user
###### Header: requires admin/ super admin bearer token
###### Body: raw JSON
````
{
    "email": "",
    "password": "",
    "role": "admin"
}
````

#### Create new User  (accessible by admin/ super admin)
###### Request: POST <- /admin/create-user
###### Header: requires admin/ super admin bearer token
###### Body: raw JSON
````
{
    "deviceId": "",
    "name": "",
    "email": "",
    "isDisabled": false
}
````

##### Update user details based on email and device id (accessible by admin/ super admin)
###### Request: POST <- /admin/update-user
###### Header: requires admin/ super admin bearer token
###### Body: raw JSON
````
{
    "deviceId": "",
    "name": "",
    "email": "",
    "isDisabled": true
}
````

##### Enable/ disable user  (accessible by admin/ super admin)
###### Request: POST <- /admin/update-user
###### Header: requires admin/ super admin bearer token
###### Body: raw JSON

````
{
    "deviceId": "",
    "email": "",
    "isDisabled":true
}
````

##### Get subject marks  (accessible by admin/ super admin)
###### Header: requires admin/ super admin bearer token
````
Request: GET <- /admin/get-user-details?emailId=email@email.com&deviceId=123
````

##### Delete User  (accessible by super admin)
###### Request: DELETE <- /admin/delete-user
###### Header: requires super admin bearer token
###### Body: raw JSON

````
{
    "deviceId": "",
    "email": ""
}
````

##### Source files that are implementing this functionality are in packages:
* actor
* cache
* flyway
* handler
* models
* routes
* persistence module

## END
